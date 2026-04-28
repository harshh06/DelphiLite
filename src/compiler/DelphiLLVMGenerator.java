package compiler;

import grammar.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.*;

import java.util.*;

/**
 * Emits LLVM IR for a Pascal/Delphi subset: globals, Integer/Boolean, control flow,
 * standalone procedures/functions, writeln/readln, no OOP.
 */
public final class DelphiLLVMGenerator extends delphiBaseVisitor<LLVMValue> {
    public enum Target {
        NATIVE,
        WASM32
    }

    public static final class ProcSpec {
        public final String name;
        public final boolean isFunction;
        public final delphiParser.FormalParameterListContext params;
        public final delphiParser.VariableDeclarationSectionContext locals;
        public final delphiParser.BlockContext body;

        ProcSpec(String name, boolean isFunction,
                 delphiParser.FormalParameterListContext params,
                 delphiParser.VariableDeclarationSectionContext locals,
                 delphiParser.BlockContext body) {
            this.name = name;
            this.isFunction = isFunction;
            this.params = params;
            this.locals = locals;
            this.body = body;
        }
    }

    private final Map<String, ProcSpec> procedures = new LinkedHashMap<>();
    private final Map<String, VarBinding> globalBindings = new HashMap<>();
    private final Deque<Map<String, VarBinding>> scopeStack = new ArrayDeque<>();
    private final IRBuilder ir = new IRBuilder();
    private final Deque<LoopLabels> loopStack = new ArrayDeque<>();

    private final StringBuilder module = new StringBuilder();
    private final Target target;
    /** Pascal string literal content → LLVM global name (@.str.N). */
    private Map<String, String> strLitGlobals = Map.of();

    private String functionReturnSlotPtr;
    /** True if the current basic block already ended with a terminator (br, ret). */
    private boolean currBlockTerminated;

    public DelphiLLVMGenerator() {
        this(Target.NATIVE);
    }

    public DelphiLLVMGenerator(Target target) {
        this.target = target;
    }

    public String generate(delphiParser.ProgramContext prog) {
        procedures.clear();
        globalBindings.clear();
        scopeStack.clear();
        loopStack.clear();
        functionReturnSlotPtr = null;
        module.setLength(0);

        if (prog.typeSection() != null) {
            throw new UnsupportedOperationException("LLVM codegen: type section (classes) not supported");
        }
        if (!prog.implementationSection().isEmpty()) {
            throw new UnsupportedOperationException("LLVM codegen: implementation sections (class methods) not supported");
        }

        for (delphiParser.StandaloneProcDeclContext ctx : prog.standaloneProcDecl()) {
            String name = ctx.IDENTIFIER().getText();
            if (procedures.containsKey(name)) {
                throw new IllegalStateException("Duplicate procedure/function: " + name);
            }
            procedures.put(name, new ProcSpec(name, false, ctx.formalParameterList(),
                    ctx.variableDeclarationSection(), ctx.block()));
        }
        for (delphiParser.StandaloneFuncDeclContext ctx : prog.standaloneFuncDecl()) {
            String name = ctx.IDENTIFIER().getText();
            if (procedures.containsKey(name)) {
                throw new IllegalStateException("Duplicate procedure/function: " + name);
            }
            procedures.put(name, new ProcSpec(name, true, ctx.formalParameterList(),
                    ctx.variableDeclarationSection(), ctx.block()));
        }

        LinkedHashSet<String> strLits = new LinkedHashSet<>();
        for (ProcSpec p : procedures.values()) {
            collectStringLiterals(p.body, strLits);
        }
        collectStringLiterals(prog.block(), strLits);
        Map<String, String> strMap = new LinkedHashMap<>();
        int sid = 0;
        for (String s : strLits) {
            strMap.put(s, "@.str." + (sid++));
        }
        strLitGlobals = strMap;

        emitPreamble();
        for (Map.Entry<String, String> e : strLitGlobals.entrySet()) {
            module.append(renderStringGlobal(e.getValue(), stringToNullTerminatedBytes(e.getKey())));
        }
        if (!strLitGlobals.isEmpty()) {
            module.append("\n");
        }
        module.append("@.fmt_intln = private unnamed_addr constant [4 x i8] c\"%d\\0A\\00\"\n");
        module.append("@.fmt_strln = private unnamed_addr constant [4 x i8] c\"%s\\0A\\00\"\n");
        module.append("@.fmt_scan_int = private unnamed_addr constant [3 x i8] c\"%d\\00\"\n\n");

        registerGlobals(prog.variableDeclarationSection());
        emitGlobalDefinitions();

        for (ProcSpec spec : procedures.values()) {
            if (spec.isFunction) {
                emitUserFunction(spec);
            } else {
                emitUserProcedure(spec);
            }
        }

        emitMain(prog.block());
        return module.toString();
    }

    private static void collectStringLiterals(ParseTree tree, Set<String> out) {
        if (tree instanceof delphiParser.WritelnStatementContext) {
            delphiParser.ExpressionContext ex = ((delphiParser.WritelnStatementContext) tree).expression();
            if (ex instanceof delphiParser.AtomExprContext) {
                delphiParser.AtomContext a = ((delphiParser.AtomExprContext) ex).atom();
                if (a instanceof delphiParser.StringExprContext) {
                    out.add(stringLiteralValue((delphiParser.StringExprContext) a));
                }
            }
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            collectStringLiterals(tree.getChild(i), out);
        }
    }

    private void emitPreamble() {
        module.append("; Generated by DelphiLite compiler\n");
        module.append("source_filename = \"delphi\"\n");
        if (target == Target.WASM32) {
            module.append("target datalayout = \"e-m:e-p:32:32-i64:64-n32:64-S128\"\n");
            module.append("target triple = \"wasm32-unknown-unknown\"\n\n");
        } else {
            module.append("target datalayout = \"e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128\"\n");
            module.append("target triple = \"x86_64-unknown-linux-gnu\"\n\n");
        }

        module.append("declare i32 @printf(ptr noundef, ...)\n");
        module.append("declare i32 @scanf(ptr noundef, ...)\n\n");
    }

    private void registerGlobals(delphiParser.VariableDeclarationSectionContext varSec) {
        if (varSec == null) return;
        for (delphiParser.VariableDeclarationContext vd : varSec.variableDeclarationList().variableDeclaration()) {
            String t = vd.typeName().getText();
            requireScalarType(t, vd);
            for (TerminalNode id : vd.identifierList().IDENTIFIER()) {
                String name = id.getText();
                if (globalBindings.containsKey(name)) {
                    throw new IllegalStateException("Duplicate global variable: " + name);
                }
                globalBindings.put(name, new VarBinding(LLVMValue.I32, "@" + name, true));
            }
        }
    }

    private void emitGlobalDefinitions() {
        for (String name : globalBindings.keySet()) {
            module.append("@").append(name).append(" = global i32 0\n");
        }
        if (!globalBindings.isEmpty()) {
            module.append("\n");
        }
    }

    private void emitUserProcedure(ProcSpec spec) {
        List<String> paramNames = getParamNames(spec.params);
        String sig = buildArgSignature(paramNames.size());

        module.append("define void @").append(spec.name).append("(").append(sig).append(") {\nentry:\n");
        ir.resetForFunction();
        currBlockTerminated = false;
        scopeStack.push(new LinkedHashMap<>());

        bindParameters(paramNames);
        if (spec.locals != null) {
            visit(spec.locals);
        }
        functionReturnSlotPtr = null;
        visit(spec.body);
        if (!currBlockTerminated) {
            ir.instr("ret void");
        }

        module.append(ir.takeBody());
        module.append("}\n\n");
        scopeStack.pop();
        currBlockTerminated = false;
    }

    private void emitUserFunction(ProcSpec spec) {
        List<String> paramNames = getParamNames(spec.params);
        String sig = buildArgSignature(paramNames.size());

        module.append("define i32 @").append(spec.name).append("(").append(sig).append(") {\nentry:\n");
        ir.resetForFunction();
        currBlockTerminated = false;
        scopeStack.push(new LinkedHashMap<>());

        bindParameters(paramNames);

        functionReturnSlotPtr = "%" + sanitize(spec.name) + ".ret";
        ir.instr(functionReturnSlotPtr + " = alloca i32");
        ir.instr("store i32 0, ptr " + functionReturnSlotPtr);
        scopeStack.peek().put(spec.name, new VarBinding(LLVMValue.I32, functionReturnSlotPtr, false));

        if (spec.locals != null) {
            visit(spec.locals);
        }
        visit(spec.body);

        if (!currBlockTerminated) {
            String r = ir.freshTemp();
            ir.instr(r + " = load i32, ptr " + functionReturnSlotPtr);
            ir.instr("ret i32 " + r);
        }

        module.append(ir.takeBody());
        module.append("}\n\n");
        functionReturnSlotPtr = null;
        scopeStack.pop();
        currBlockTerminated = false;
    }

    private void emitMain(delphiParser.BlockContext block) {
        module.append("define i32 @main() {\nentry:\n");
        ir.resetForFunction();
        currBlockTerminated = false;
        visit(block);
        if (!currBlockTerminated) {
            ir.instr("ret i32 0");
        }
        module.append(ir.takeBody());
        module.append("}\n");
        currBlockTerminated = false;
    }

    private static String buildArgSignature(int n) {
        if (n == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(", ");
            sb.append("i32 %arg_").append(i);
        }
        return sb.toString();
    }

    private void bindParameters(List<String> paramNames) {
        Map<String, VarBinding> scope = scopeStack.peek();
        for (int i = 0; i < paramNames.size(); i++) {
            String pname = paramNames.get(i);
            String argReg = "%arg_" + i;
            String slot = "%" + sanitize(pname) + ".addr";
            ir.instr(slot + " = alloca i32");
            ir.instr("store i32 " + argReg + ", ptr " + slot);
            scope.put(pname, new VarBinding(LLVMValue.I32, slot, false));
        }
    }

    private static List<String> getParamNames(delphiParser.FormalParameterListContext paramCtx) {
        List<String> names = new ArrayList<>();
        if (paramCtx == null) return names;
        for (delphiParser.FormalParameterContext fp : paramCtx.formalParameter()) {
            for (TerminalNode id : fp.identifierList().IDENTIFIER()) {
                names.add(id.getText());
            }
        }
        return names;
    }

    private static String sanitize(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            b.append(Character.isLetterOrDigit(c) || c == '_' ? c : '_');
        }
        return b.length() > 0 ? b.toString() : "v";
    }

    private static void requireScalarType(String typeName, ParserRuleContext ctx) {
        switch (typeName) {
            case "Integer":
            case "Boolean":
                return;
            case "String":
                throw new UnsupportedOperationException("LLVM codegen: String variables not supported (" + ctx.getText() + ")");
            default:
                throw new UnsupportedOperationException("LLVM codegen: type '" + typeName + "' not supported");
        }
    }

    private VarBinding lookup(String name) {
        for (Map<String, VarBinding> scope : scopeStack) {
            VarBinding v = scope.get(name);
            if (v != null) return v;
        }
        VarBinding g = globalBindings.get(name);
        if (g != null) return g;
        throw new IllegalArgumentException("Undefined variable: " + name);
    }

    private static boolean exprMightBeString(delphiParser.ExpressionContext ctx) {
        if (ctx instanceof delphiParser.AtomExprContext) {
            delphiParser.AtomContext a = ((delphiParser.AtomExprContext) ctx).atom();
            return a instanceof delphiParser.StringExprContext;
        }
        if (ctx instanceof delphiParser.AdditiveExprContext
                || ctx instanceof delphiParser.MultiplicativeExprContext
                || ctx instanceof delphiParser.CompareExprContext
                || ctx instanceof delphiParser.AndExprContext
                || ctx instanceof delphiParser.OrExprContext
                || ctx instanceof delphiParser.NotExprContext) {
            for (int i = 0; i < ctx.getChildCount(); i++) {
                ParseTree ch = ctx.getChild(i);
                if (ch instanceof delphiParser.ExpressionContext && exprMightBeString((delphiParser.ExpressionContext) ch)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public LLVMValue visitVariableDeclaration(delphiParser.VariableDeclarationContext ctx) {
        if (scopeStack.isEmpty()) {
            return null;
        }
        String t = ctx.typeName().getText();
        requireScalarType(t, ctx);
        Map<String, VarBinding> scope = scopeStack.peek();
        for (TerminalNode id : ctx.identifierList().IDENTIFIER()) {
            String name = id.getText();
            String slot = "%loc_" + sanitize(name);
            ir.instr(slot + " = alloca i32");
            ir.instr("store i32 0, ptr " + slot);
            scope.put(name, new VarBinding(LLVMValue.I32, slot, false));
        }
        return null;
    }

    @Override
    public LLVMValue visitVariableDeclarationSection(delphiParser.VariableDeclarationSectionContext ctx) {
        return visit(ctx.variableDeclarationList());
    }

    @Override
    public LLVMValue visitVariableDeclarationList(delphiParser.VariableDeclarationListContext ctx) {
        for (delphiParser.VariableDeclarationContext vd : ctx.variableDeclaration()) {
            visit(vd);
        }
        return null;
    }

    @Override
    public LLVMValue visitBlock(delphiParser.BlockContext ctx) {
        return visit(ctx.statementList());
    }

    @Override
    public LLVMValue visitStatementList(delphiParser.StatementListContext ctx) {
        List<delphiParser.StatementContext> sts = ctx.statement();
        for (int i = 0; i < sts.size(); i++) {
            if (i > 0) {
                String lbl = ir.freshLabel("seq");
                if (!currBlockTerminated) {
                    ir.instr("br label %" + lbl);
                }
                ir.label(lbl);
            }
            currBlockTerminated = false;
            visit(sts.get(i));
        }
        return null;
    }

    @Override
    public LLVMValue visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {
        List<TerminalNode> ids = ctx.IDENTIFIER();
        if (ids.size() != 1) {
            throw new UnsupportedOperationException("LLVM codegen: field assignment not supported");
        }
        String name = ids.get(0).getText();
        LLVMValue rhs = visit(ctx.expression());
        VarBinding vb = lookup(name);
        ir.instr("store i32 " + rhs.operand + ", ptr " + vb.ptrName);
        currBlockTerminated = false;
        return null;
    }

    @Override
    public LLVMValue visitIfStatement(delphiParser.IfStatementContext ctx) {
        LLVMValue cond = visit(ctx.expression());
        String thenL = ir.freshLabel("if_then");
        String elseL = ir.freshLabel("if_else");
        String endL = ir.freshLabel("if_end");
        String c = ir.freshTemp();
        ir.instr(c + " = icmp ne i32 " + cond.operand + ", 0");
        boolean hasElse = ctx.statement().size() > 1;
        if (hasElse) {
            ir.instr("br i1 " + c + ", label %" + thenL + ", label %" + elseL);
        } else {
            ir.instr("br i1 " + c + ", label %" + thenL + ", label %" + endL);
        }
        ir.label(thenL);
        currBlockTerminated = false;
        visit(ctx.statement(0));
        boolean thenTerm = currBlockTerminated;
        if (!thenTerm) {
            ir.instr("br label %" + endL);
        }
        if (hasElse) {
            ir.label(elseL);
            currBlockTerminated = false;
            visit(ctx.statement(1));
            if (!currBlockTerminated) {
                ir.instr("br label %" + endL);
            }
        }
        ir.label(endL);
        currBlockTerminated = false;
        return null;
    }

    @Override
    public LLVMValue visitWhileStatement(delphiParser.WhileStatementContext ctx) {
        String condL = ir.freshLabel("while_cond");
        String bodyL = ir.freshLabel("while_body");
        String endL = ir.freshLabel("while_end");
        loopStack.push(new LoopLabels(endL, condL));
        ir.instr("br label %" + condL);
        ir.label(condL);
        LLVMValue cond = visit(ctx.expression());
        String c = ir.freshTemp();
        ir.instr(c + " = icmp ne i32 " + cond.operand + ", 0");
        ir.instr("br i1 " + c + ", label %" + bodyL + ", label %" + endL);
        ir.label(bodyL);
        currBlockTerminated = false;
        visit(ctx.statement());
        if (!currBlockTerminated) {
            ir.instr("br label %" + condL);
        }
        ir.label(endL);
        loopStack.pop();
        currBlockTerminated = false;
        return null;
    }

    @Override
    public LLVMValue visitForStatement(delphiParser.ForStatementContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        LLVMValue start = visit(ctx.expression(0));
        LLVMValue limit = visit(ctx.expression(1));
        boolean down = ctx.getChild(4).getText().equals("downto");

        VarBinding vb = lookup(varName);
        ir.instr("store i32 " + start.operand + ", ptr " + vb.ptrName);

        String hdr = ir.freshLabel("for_hdr");
        String body = ir.freshLabel("for_body");
        String step = ir.freshLabel("for_step");
        String end = ir.freshLabel("for_end");
        loopStack.push(new LoopLabels(end, step));

        ir.instr("br label %" + hdr);
        ir.label(hdr);
        String iv = ir.freshTemp();
        ir.instr(iv + " = load i32, ptr " + vb.ptrName);
        String cmp = ir.freshTemp();
        if (down) {
            ir.instr(cmp + " = icmp sge i32 " + iv + ", " + limit.operand);
        } else {
            ir.instr(cmp + " = icmp sle i32 " + iv + ", " + limit.operand);
        }
        ir.instr("br i1 " + cmp + ", label %" + body + ", label %" + end);
        ir.label(body);
        currBlockTerminated = false;
        visit(ctx.statement());
        if (!currBlockTerminated) {
            ir.instr("br label %" + step);
        }
        ir.label(step);
        String iv2 = ir.freshTemp();
        ir.instr(iv2 + " = load i32, ptr " + vb.ptrName);
        String iv3 = ir.freshTemp();
        if (down) {
            ir.instr(iv3 + " = sub nsw i32 " + iv2 + ", 1");
        } else {
            ir.instr(iv3 + " = add nsw i32 " + iv2 + ", 1");
        }
        ir.instr("store i32 " + iv3 + ", ptr " + vb.ptrName);
        ir.instr("br label %" + hdr);
        ir.label(end);
        loopStack.pop();
        currBlockTerminated = false;
        return null;
    }

    @Override
    public LLVMValue visitBreakStatement(delphiParser.BreakStatementContext ctx) {
        if (loopStack.isEmpty()) {
            throw new IllegalStateException("break outside loop");
        }
        ir.instr("br label %" + loopStack.peek().breakLabel);
        currBlockTerminated = true;
        return null;
    }

    @Override
    public LLVMValue visitContinueStatement(delphiParser.ContinueStatementContext ctx) {
        if (loopStack.isEmpty()) {
            throw new IllegalStateException("continue outside loop");
        }
        ir.instr("br label %" + loopStack.peek().continueLabel);
        currBlockTerminated = true;
        return null;
    }

    @Override
    public LLVMValue visitWritelnStatement(delphiParser.WritelnStatementContext ctx) {
        if (target == Target.WASM32) {
            throw new UnsupportedOperationException("writeln is not supported in wasm target; use pure computation and exported function returns");
        }
        delphiParser.ExpressionContext ex = ctx.expression();
        if (exprMightBeString(ex)) {
            if (!(ex instanceof delphiParser.AtomExprContext)
                    || !(((delphiParser.AtomExprContext) ex).atom() instanceof delphiParser.StringExprContext)) {
                throw new UnsupportedOperationException("writeln: only string literals supported, not expressions");
            }
            delphiParser.StringExprContext sctx = (delphiParser.StringExprContext) ((delphiParser.AtomExprContext) ex).atom();
            String lit = stringLiteralValue(sctx);
            String gname = strLitGlobals.get(lit);
            if (gname == null) {
                throw new IllegalStateException("missing string global for: " + lit);
            }
            int n = lit.length() + 1;
            String ptr = ir.freshTemp();
            ir.instr(ptr + " = getelementptr inbounds [" + n + " x i8], ptr " + gname + ", i32 0, i32 0");
            String fptr = ir.freshTemp();
            ir.instr(fptr + " = getelementptr inbounds [4 x i8], ptr @.fmt_strln, i32 0, i32 0");
            ir.instr("call i32 (ptr, ...) @printf(ptr " + fptr + ", ptr " + ptr + ")");
        } else {
            LLVMValue v = visit(ex);
            String fptr = ir.freshTemp();
            ir.instr(fptr + " = getelementptr inbounds [4 x i8], ptr @.fmt_intln, i32 0, i32 0");
            ir.instr("call i32 (ptr, ...) @printf(ptr " + fptr + ", i32 " + v.operand + ")");
        }
        currBlockTerminated = false;
        return null;
    }

    @Override
    public LLVMValue visitReadlnStatement(delphiParser.ReadlnStatementContext ctx) {
        if (target == Target.WASM32) {
            throw new UnsupportedOperationException("readln is not supported in wasm target");
        }
        String name = ctx.IDENTIFIER().getText();
        VarBinding vb = lookup(name);
        String fptr = ir.freshTemp();
        ir.instr(fptr + " = getelementptr inbounds [3 x i8], ptr @.fmt_scan_int, i32 0, i32 0");
        ir.instr("call i32 (ptr, ...) @scanf(ptr " + fptr + ", ptr " + vb.ptrName + ")");
        currBlockTerminated = false;
        return null;
    }

    @Override
    public LLVMValue visitProcedureCallStatement(delphiParser.ProcedureCallStatementContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        ProcSpec spec = procedures.get(name);
        if (spec == null || spec.isFunction) {
            throw new IllegalArgumentException("Not a procedure: " + name);
        }
        List<LLVMValue> args = evalArgs(ctx.argumentList());
        String call = buildCallArgs(args);
        ir.instr("call void @" + name + "(" + call + ")");
        currBlockTerminated = false;
        return null;
    }

    @Override
    public LLVMValue visitMethodCallStatement(delphiParser.MethodCallStatementContext ctx) {
        throw new UnsupportedOperationException("LLVM codegen: method calls not supported");
    }

    @Override
    public LLVMValue visitAtomExpr(delphiParser.AtomExprContext ctx) {
        return visit(ctx.atom());
    }

    @Override
    public LLVMValue visitIntegerExpr(delphiParser.IntegerExprContext ctx) {
        int v = Integer.parseInt(ctx.INTEGER_LITERAL().getText());
        return LLVMValue.i32Const(v);
    }

    @Override
    public LLVMValue visitStringExpr(delphiParser.StringExprContext ctx) {
        throw new UnsupportedOperationException("String expression not in supported context");
    }

    @Override
    public LLVMValue visitTrueExpr(delphiParser.TrueExprContext ctx) {
        return LLVMValue.i32Const(1);
    }

    @Override
    public LLVMValue visitFalseExpr(delphiParser.FalseExprContext ctx) {
        return LLVMValue.i32Const(0);
    }

    @Override
    public LLVMValue visitIdentifierExpr(delphiParser.IdentifierExprContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        VarBinding vb = lookup(name);
        String t = ir.freshTemp();
        ir.instr(t + " = load i32, ptr " + vb.ptrName);
        return new LLVMValue(LLVMValue.I32, t);
    }

    @Override
    public LLVMValue visitParenExpr(delphiParser.ParenExprContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public LLVMValue visitMultiplicativeExpr(delphiParser.MultiplicativeExprContext ctx) {
        if (exprMightBeString(ctx.expression(0)) || exprMightBeString(ctx.expression(1))) {
            throw new UnsupportedOperationException("Arithmetic on strings not supported");
        }
        LLVMValue L = visit(ctx.expression(0));
        LLVMValue R = visit(ctx.expression(1));
        String op = ctx.getChild(1).getText();
        String t = ir.freshTemp();
        if (op.equals("*")) {
            ir.instr(t + " = mul nsw i32 " + L.operand + ", " + R.operand);
        } else if (op.equals("/")) {
            ir.instr(t + " = sdiv i32 " + L.operand + ", " + R.operand);
        } else {
            throw new IllegalStateException("op " + op);
        }
        return new LLVMValue(LLVMValue.I32, t);
    }

    @Override
    public LLVMValue visitAdditiveExpr(delphiParser.AdditiveExprContext ctx) {
        if (exprMightBeString(ctx.expression(0)) || exprMightBeString(ctx.expression(1))) {
            throw new UnsupportedOperationException("String concatenation not supported in LLVM codegen");
        }
        LLVMValue L = visit(ctx.expression(0));
        LLVMValue R = visit(ctx.expression(1));
        String op = ctx.getChild(1).getText();
        String t = ir.freshTemp();
        if (op.equals("+")) {
            ir.instr(t + " = add nsw i32 " + L.operand + ", " + R.operand);
        } else if (op.equals("-")) {
            ir.instr(t + " = sub nsw i32 " + L.operand + ", " + R.operand);
        } else {
            throw new IllegalStateException("op " + op);
        }
        return new LLVMValue(LLVMValue.I32, t);
    }

    @Override
    public LLVMValue visitCompareExpr(delphiParser.CompareExprContext ctx) {
        if (exprMightBeString(ctx.expression(0)) || exprMightBeString(ctx.expression(1))) {
            throw new UnsupportedOperationException("String comparison not supported in LLVM codegen");
        }
        LLVMValue L = visit(ctx.expression(0));
        LLVMValue R = visit(ctx.expression(1));
        String op = ctx.getChild(1).getText();
        String cmp = ir.freshTemp();
        String pred;
        switch (op) {
            case "<":
                pred = "slt";
                break;
            case ">":
                pred = "sgt";
                break;
            case "<=":
                pred = "sle";
                break;
            case ">=":
                pred = "sge";
                break;
            case "=":
                pred = "eq";
                break;
            case "<>":
                pred = "ne";
                break;
            default:
                throw new IllegalStateException(op);
        }
        ir.instr(cmp + " = icmp " + pred + " i32 " + L.operand + ", " + R.operand);
        String z = ir.freshTemp();
        ir.instr(z + " = zext i1 " + cmp + " to i32");
        return new LLVMValue(LLVMValue.I32, z);
    }

    @Override
    public LLVMValue visitNotExpr(delphiParser.NotExprContext ctx) {
        LLVMValue v = visit(ctx.expression());
        String z = ir.freshTemp();
        ir.instr(z + " = icmp eq i32 " + v.operand + ", 0");
        String r = ir.freshTemp();
        ir.instr(r + " = zext i1 " + z + " to i32");
        return new LLVMValue(LLVMValue.I32, r);
    }

    @Override
    public LLVMValue visitAndExpr(delphiParser.AndExprContext ctx) {
        LLVMValue a = visit(ctx.expression(0));
        LLVMValue b = visit(ctx.expression(1));
        String a1 = ir.freshTemp();
        String b1 = ir.freshTemp();
        ir.instr(a1 + " = icmp ne i32 " + a.operand + ", 0");
        ir.instr(b1 + " = icmp ne i32 " + b.operand + ", 0");
        String c = ir.freshTemp();
        ir.instr(c + " = and i1 " + a1 + ", " + b1);
        String r = ir.freshTemp();
        ir.instr(r + " = zext i1 " + c + " to i32");
        return new LLVMValue(LLVMValue.I32, r);
    }

    @Override
    public LLVMValue visitOrExpr(delphiParser.OrExprContext ctx) {
        LLVMValue a = visit(ctx.expression(0));
        LLVMValue b = visit(ctx.expression(1));
        String a1 = ir.freshTemp();
        String b1 = ir.freshTemp();
        ir.instr(a1 + " = icmp ne i32 " + a.operand + ", 0");
        ir.instr(b1 + " = icmp ne i32 " + b.operand + ", 0");
        String c = ir.freshTemp();
        ir.instr(c + " = or i1 " + a1 + ", " + b1);
        String r = ir.freshTemp();
        ir.instr(r + " = zext i1 " + c + " to i32");
        return new LLVMValue(LLVMValue.I32, r);
    }

    @Override
    public LLVMValue visitFunctionCallExpr(delphiParser.FunctionCallExprContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        ProcSpec spec = procedures.get(name);
        if (spec == null || !spec.isFunction) {
            throw new IllegalArgumentException("Not a function: " + name);
        }
        List<LLVMValue> args = evalArgs(ctx.argumentList());
        String call = buildCallArgs(args);
        String t = ir.freshTemp();
        ir.instr(t + " = call i32 @" + name + "(" + call + ")");
        return new LLVMValue(LLVMValue.I32, t);
    }

    @Override
    public LLVMValue visitMemberAccessExpr(delphiParser.MemberAccessExprContext ctx) {
        throw new UnsupportedOperationException("LLVM codegen: member access not supported");
    }

    private List<LLVMValue> evalArgs(delphiParser.ArgumentListContext argCtx) {
        List<LLVMValue> out = new ArrayList<>();
        if (argCtx == null) return out;
        for (delphiParser.ExpressionContext e : argCtx.expression()) {
            out.add(visit(e));
        }
        return out;
    }

    private static String buildCallArgs(List<LLVMValue> args) {
        if (args.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("i32 ").append(args.get(i).operand);
        }
        return sb.toString();
    }

    private static String stringLiteralValue(delphiParser.StringExprContext ctx) {
        String t = ctx.STRING_LITERAL().getText();
        return t.substring(1, t.length() - 1).replace("''", "'");
    }

    private static byte[] stringToNullTerminatedBytes(String content) {
        byte[] bytes = new byte[content.length() + 1];
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c > 255) {
                throw new UnsupportedOperationException("Non-Latin-1 character in string literal");
            }
            bytes[i] = (byte) c;
        }
        bytes[content.length()] = 0;
        return bytes;
    }

    private static String renderStringGlobal(String gname, byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append(gname).append(" = private unnamed_addr constant [").append(bytes.length).append(" x i8] c\"");
        for (byte bb : bytes) {
            int u = bb & 0xff;
            if (u == '\\') sb.append("\\5C");
            else if (u == '"') sb.append("\\22");
            else if (u == '\n') sb.append("\\0A");
            else if (u == '\r') sb.append("\\0D");
            else if (u == '\t') sb.append("\\09");
            else if (u >= 32 && u < 127) sb.append((char) u);
            else sb.append(String.format("\\%02X", u));
        }
        sb.append("\"\n");
        return sb.toString();
    }
}
