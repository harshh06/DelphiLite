package interpreter;

import grammar.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.*;

public class DelphiInterpreter extends delphiBaseVisitor<Object> {

    private Environment globalEnv = new Environment(null);
    private Environment currentEnv = globalEnv;
    private Map<String, ClassDefinition> classDefinitions = new HashMap<>();
    private Map<String, ParseTree> methodBodies = new HashMap<>();
    private Scanner scanner = new Scanner(System.in);
    private String currentClassName = null;
    private ObjectInstance currentSelf = null;
    private Map<String, delphiParser.FormalParameterListContext> methodParams = new HashMap<>();
    private Map<String, InterfaceDefinition> interfaceDefinitions = new HashMap<>();
    private Map<String, ProcedureDefinition> procedures = new HashMap<>();
    private boolean showConstFold = false;
    private boolean lastExprIsConstant = false;

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private boolean toBoolean(Object val) {
        if (val instanceof Boolean) return (Boolean) val;
        throw new RuntimeException("Expected Boolean but got: " + val);
    }

    private int toInt(Object val) {
        if (val instanceof Integer) return (Integer) val;
        throw new RuntimeException("Expected Integer but got: " + val);
    }

    private void checkAccess(String memberName, String visibility, String ownerClassName) {
        if (visibility.equals("private") && !ownerClassName.equals(currentClassName)) {
            throw new RuntimeException("Cannot access private member '" + memberName + "' from outside class '" + ownerClassName + "'");
        }
        if (visibility.equals("protected") && !ownerClassName.equals(currentClassName)) {
            throw new RuntimeException("Cannot access protected member '" + memberName + "' from outside class '" + ownerClassName + "'");
        }
    }

    private List<String> getParamNames(delphiParser.FormalParameterListContext paramCtx) {
        List<String> names = new ArrayList<>();
        if (paramCtx == null) return names;
        for (delphiParser.FormalParameterContext fp : paramCtx.formalParameter()) {
            for (TerminalNode id : fp.identifierList().IDENTIFIER()) {
                names.add(id.getText());
            }
        }
        return names;
    }

    private List<Object> evalArguments(delphiParser.ArgumentListContext argCtx) {
        List<Object> values = new ArrayList<>();
        if (argCtx == null) return values;
        for (delphiParser.ExpressionContext expr : argCtx.expression()) {
            values.add(visit(expr));
        }
        return values;
    }

    private void bindParameters(String key, delphiParser.ArgumentListContext argCtx) {
        if (methodParams.containsKey(key) && argCtx != null) {
            List<String> paramNames = getParamNames(methodParams.get(key));
            List<Object> argValues = evalArguments(argCtx);
            for (int i = 0; i < paramNames.size() && i < argValues.size(); i++) {
                currentEnv.define(paramNames.get(i), argValues.get(i));
            }
        }
    }

    private String resolveMethodKey(String className, String methodName) {
        String key = className + "." + methodName;
        if (methodBodies.containsKey(key)) return key;
        ClassDefinition classDef = classDefinitions.get(className);
        if (classDef != null && classDef.getParentClassName() != null) {
            return resolveMethodKey(classDef.getParentClassName(), methodName);
        }
        return key;
    }

    // ─── Entry Point ─────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: DelphiInterpreter [--show-const-fold] <file.pas>");
            System.exit(1);
        }
        String filename = args[args.length - 1];
        DelphiInterpreter interpreter = new DelphiInterpreter();
        for (String arg : args) {
            if (arg.equals("--show-const-fold")) interpreter.showConstFold = true;
        }
        CharStream input = CharStreams.fromFileName(filename);
        delphiLexer lexer = new delphiLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        delphiParser parser = new delphiParser(tokens);
        ParseTree tree = parser.program();
        interpreter.visit(tree);
    }

    // ─── Program ─────────────────────────────────────────────────────────────────

    @Override
    public Object visitProgram(delphiParser.ProgramContext ctx) {
        if (ctx.typeSection() != null) visit(ctx.typeSection());
        for (delphiParser.ImplementationSectionContext impl : ctx.implementationSection()) {
            visit(impl);
        }
        for (delphiParser.StandaloneProcDeclContext procCtx : ctx.standaloneProcDecl()) {
            visit(procCtx);
        }
        for (delphiParser.StandaloneFuncDeclContext funcCtx : ctx.standaloneFuncDecl()) {
            visit(funcCtx);
        }
        if (ctx.variableDeclarationSection() != null) visit(ctx.variableDeclarationSection());
        visit(ctx.block());
        return null;
    }

    // ─── Variable Declarations ───────────────────────────────────────────────────

    @Override
    public Object visitVariableDeclaration(delphiParser.VariableDeclarationContext ctx) {
        String typeName = ctx.typeName().getText();
        for (TerminalNode id : ctx.identifierList().IDENTIFIER()) {
            String name = id.getText();
            Object defaultValue;
            switch (typeName) {
                case "Integer": defaultValue = 0; break;
                case "String":  defaultValue = ""; break;
                case "Boolean": defaultValue = false; break;
                default:        defaultValue = null; break;
            }
            currentEnv.define(name, defaultValue);
        }
        return null;
    }

    // ─── Assignment ──────────────────────────────────────────────────────────────

    @Override
    public Object visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {
        List<TerminalNode> ids = ctx.IDENTIFIER();

        if (ids.size() == 2) {
            String objName = ids.get(0).getText();
            String fieldName = ids.get(1).getText();
            Object value = visit(ctx.expression());
            Object objValue = currentEnv.get(objName);
            if (objValue instanceof ObjectInstance) {
                ObjectInstance obj = (ObjectInstance) objValue;
                ClassDefinition classDef = classDefinitions.get(obj.getClassName());
                checkAccess(fieldName, classDef.getFieldVisibility(fieldName), obj.getClassName());
                obj.setField(fieldName, value);
            }
        } else {
            String varName = ids.get(0).getText();
            Object value = visit(ctx.expression());
            if (currentSelf != null) {
                ClassDefinition classDef = classDefinitions.get(currentSelf.getClassName());
                if (classDef.getFields().containsKey(varName)) {
                    currentSelf.setField(varName, value);
                    return null;
                }
            }
            if (currentEnv.exists(varName)) {
                currentEnv.set(varName, value);
            } else {
                currentEnv.define(varName, value);
            }
        }
        return null;
    }

    // ─── Arithmetic Expressions ──────────────────────────────────────────────────

    @Override
    public Object visitMultiplicativeExpr(delphiParser.MultiplicativeExprContext ctx) {
        Object leftObj = visit(ctx.expression(0));
        boolean leftConst = lastExprIsConstant;
        Object rightObj = visit(ctx.expression(1));
        boolean rightConst = lastExprIsConstant;

        int left = toInt(leftObj);
        int right = toInt(rightObj);
        String op = ctx.getChild(1).getText();
        int result;
        if (op.equals("*")) result = left * right;
        else if (op.equals("/")) result = left / right;
        else throw new RuntimeException("Unknown operator: " + op);

        lastExprIsConstant = leftConst && rightConst;
        if (lastExprIsConstant && showConstFold) {
            System.out.println("[ConstFold] " + left + " " + op + " " + right + " -> " + result);
        }
        return result;
    }

    @Override
    public Object visitAdditiveExpr(delphiParser.AdditiveExprContext ctx) {
        Object leftObj = visit(ctx.expression(0));
        boolean leftConst = lastExprIsConstant;
        Object rightObj = visit(ctx.expression(1));
        boolean rightConst = lastExprIsConstant;

        String op = ctx.getChild(1).getText();
        if (leftObj instanceof String || rightObj instanceof String) {
            lastExprIsConstant = false;
            return String.valueOf(leftObj) + String.valueOf(rightObj);
        }
        int left = toInt(leftObj), right = toInt(rightObj);
        int result;
        if (op.equals("+")) result = left + right;
        else if (op.equals("-")) result = left - right;
        else throw new RuntimeException("Unknown operator: " + op);

        lastExprIsConstant = leftConst && rightConst;
        if (lastExprIsConstant && showConstFold) {
            System.out.println("[ConstFold] " + left + " " + op + " " + right + " -> " + result);
        }
        return result;
    }

    // ─── Boolean & Comparison Expressions (NEW) ──────────────────────────────────

    @Override
    public Object visitOrExpr(delphiParser.OrExprContext ctx) {
        boolean left = toBoolean(visit(ctx.expression(0)));
        boolean right = toBoolean(visit(ctx.expression(1)));
        return left || right;
    }

    @Override
    public Object visitAndExpr(delphiParser.AndExprContext ctx) {
        boolean left = toBoolean(visit(ctx.expression(0)));
        boolean right = toBoolean(visit(ctx.expression(1)));
        return left && right;
    }

    @Override
    public Object visitNotExpr(delphiParser.NotExprContext ctx) {
        return !toBoolean(visit(ctx.expression()));
    }

    @Override
    public Object visitCompareExpr(delphiParser.CompareExprContext ctx) {
        Object left = visit(ctx.expression(0));
        Object right = visit(ctx.expression(1));
        String op = ctx.getChild(1).getText();

        if (left instanceof Integer && right instanceof Integer) {
            int l = toInt(left), r = toInt(right);
            switch (op) {
                case "<":  return l < r;
                case ">":  return l > r;
                case "<=": return l <= r;
                case ">=": return l >= r;
                case "=":  return l == r;
                case "<>": return l != r;
            }
        }
        if (left instanceof String && right instanceof String) {
            switch (op) {
                case "=":  return left.equals(right);
                case "<>": return !left.equals(right);
            }
        }
        if (left instanceof Boolean && right instanceof Boolean) {
            switch (op) {
                case "=":  return left.equals(right);
                case "<>": return !left.equals(right);
            }
        }
        throw new RuntimeException("Cannot compare " + left + " " + op + " " + right);
    }

    @Override
    public Object visitTrueExpr(delphiParser.TrueExprContext ctx) {
        return true;
    }

    @Override
    public Object visitFalseExpr(delphiParser.FalseExprContext ctx) {
        return false;
    }

    // ─── Control Flow: if-then-else (NEW) ────────────────────────────────────────

    @Override
    public Object visitIfStatement(delphiParser.IfStatementContext ctx) {
        boolean condition = toBoolean(visit(ctx.expression()));
        if (condition) {
            visit(ctx.statement(0));
        } else if (ctx.statement().size() > 1) {
            visit(ctx.statement(1));
        }
        return null;
    }

    // ─── Control Flow: while-do (NEW) ────────────────────────────────────────────

    @Override
    public Object visitWhileStatement(delphiParser.WhileStatementContext ctx) {
        while (toBoolean(visit(ctx.expression()))) {
            Environment savedEnv = currentEnv;
            currentEnv = new Environment(currentEnv);
            boolean doBreak = false;
            try {
                visit(ctx.statement());
            } catch (BreakException e) {
                doBreak = true;
            } catch (ContinueException e) {
                // fall through — finally restores env, then loop re-checks condition
            } finally {
                currentEnv = savedEnv;
            }
            if (doBreak) break;
        }
        return null;
    }

    // ─── Control Flow: for-do (NEW) ──────────────────────────────────────────────

    @Override
    public Object visitForStatement(delphiParser.ForStatementContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        int start = toInt(visit(ctx.expression(0)));
        int limit = toInt(visit(ctx.expression(1)));
        // child(4) is either 'to' or 'downto'
        boolean isDownTo = ctx.getChild(4).getText().equals("downto");
        int step = isDownTo ? -1 : 1;

        // Set/define the loop variable in the current (outer) scope
        if (currentEnv.exists(varName)) {
            currentEnv.set(varName, start);
        } else {
            currentEnv.define(varName, start);
        }

        for (int i = start; isDownTo ? (i >= limit) : (i <= limit); i += step) {
            currentEnv.set(varName, i);

            Environment savedEnv = currentEnv;
            currentEnv = new Environment(currentEnv);
            boolean doBreak = false;
            try {
                visit(ctx.statement());
            } catch (BreakException e) {
                doBreak = true;
            } catch (ContinueException e) {
                // continue to next iteration
            } finally {
                currentEnv = savedEnv;
            }
            if (doBreak) break;
        }
        return null;
    }

    // ─── break / continue (NEW) ──────────────────────────────────────────────────

    @Override
    public Object visitBreakStatement(delphiParser.BreakStatementContext ctx) {
        throw new BreakException();
    }

    @Override
    public Object visitContinueStatement(delphiParser.ContinueStatementContext ctx) {
        throw new ContinueException();
    }

    // ─── Atom Expressions ────────────────────────────────────────────────────────

    @Override
    public Object visitAtomExpr(delphiParser.AtomExprContext ctx) {
        return visit(ctx.atom());
    }

    @Override
    public Object visitIntegerExpr(delphiParser.IntegerExprContext ctx) {
        lastExprIsConstant = true;
        return Integer.parseInt(ctx.INTEGER_LITERAL().getText());
    }

    @Override
    public Object visitStringExpr(delphiParser.StringExprContext ctx) {
        lastExprIsConstant = false;
        String text = ctx.STRING_LITERAL().getText();
        return text.substring(1, text.length() - 1);
    }

    @Override
    public Object visitIdentifierExpr(delphiParser.IdentifierExprContext ctx) {
        lastExprIsConstant = false;
        String name = ctx.IDENTIFIER().getText();
        if (name.equals("Self") && currentSelf != null) return currentSelf;
        if (currentSelf != null) {
            ClassDefinition classDef = classDefinitions.get(currentSelf.getClassName());
            if (classDef.getFields().containsKey(name)) return currentSelf.getField(name);
        }
        return currentEnv.get(name);
    }

    @Override
    public Object visitParenExpr(delphiParser.ParenExprContext ctx) {
        return visit(ctx.expression());
    }

    // ─── I/O ─────────────────────────────────────────────────────────────────────

    @Override
    public Object visitWritelnStatement(delphiParser.WritelnStatementContext ctx) {
        Object value = visit(ctx.expression());
        System.out.println(value);
        return null;
    }

    @Override
    public Object visitReadlnStatement(delphiParser.ReadlnStatementContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        int value = scanner.nextInt();
        currentEnv.set(varName, value);
        return null;
    }

    // ─── OOP / Types ─────────────────────────────────────────────────────────────

    @Override
    public Object visitTypeSection(delphiParser.TypeSectionContext ctx) {
        for (delphiParser.InterfaceDeclarationContext ifaceCtx : ctx.interfaceDeclaration()) {
            String ifaceName = ifaceCtx.IDENTIFIER().getText();
            InterfaceDefinition ifaceDef = new InterfaceDefinition(ifaceName);
            for (delphiParser.InterfaceMethodDeclarationContext methodCtx : ifaceCtx.interfaceBody().interfaceMethodDeclaration()) {
                if (methodCtx.procedureDeclaration() != null)
                    ifaceDef.addMethod(methodCtx.procedureDeclaration().IDENTIFIER().getText());
                else if (methodCtx.functionDeclaration() != null)
                    ifaceDef.addMethod(methodCtx.functionDeclaration().IDENTIFIER().getText());
            }
            interfaceDefinitions.put(ifaceName, ifaceDef);
        }

        for (delphiParser.ClassDeclarationContext classCtx : ctx.classDeclaration()) {
            String className = classCtx.IDENTIFIER().getText();
            ClassDefinition classDef = new ClassDefinition(className);

            if (classCtx.classParentList() != null) {
                for (TerminalNode parent : classCtx.classParentList().IDENTIFIER()) {
                    String parentName = parent.getText();
                    if (classDefinitions.containsKey(parentName)) {
                        classDef.setParentClassName(parentName);
                        classDef.inheritFrom(classDefinitions.get(parentName));
                    } else if (interfaceDefinitions.containsKey(parentName)) {
                        classDef.addImplementedInterface(parentName);
                    }
                }
            }

            for (delphiParser.ClassVisibilitySectionContext visSection : classCtx.classBody().classVisibilitySection()) {
                String visibility = visSection.visibilityModifier().getText();
                for (delphiParser.ClassMemberContext member : visSection.classMember()) {
                    if (member.fieldDeclaration() != null) {
                        delphiParser.FieldDeclarationContext fieldCtx = member.fieldDeclaration();
                        String typeName = fieldCtx.typeName().getText();
                        for (TerminalNode id : fieldCtx.identifierList().IDENTIFIER()) {
                            classDef.addField(id.getText(), typeName, visibility);
                        }
                    }
                    if (member.methodDeclaration() != null) {
                        if (member.methodDeclaration().procedureDeclaration() != null)
                            classDef.addMethodVisibility(member.methodDeclaration().procedureDeclaration().IDENTIFIER().getText(), visibility);
                        else if (member.methodDeclaration().functionDeclaration() != null)
                            classDef.addMethodVisibility(member.methodDeclaration().functionDeclaration().IDENTIFIER().getText(), visibility);
                    }
                    if (member.constructorDeclaration() != null)
                        classDef.addMethodVisibility(member.constructorDeclaration().IDENTIFIER().getText(), visibility);
                    if (member.destructorDeclaration() != null)
                        classDef.addMethodVisibility(member.destructorDeclaration().IDENTIFIER().getText(), visibility);
                }
            }

            for (String ifaceName : classDef.getImplementedInterfaces()) {
                InterfaceDefinition ifaceDef = interfaceDefinitions.get(ifaceName);
                if (ifaceDef != null) ifaceDef.validate(classDef);
            }

            classDefinitions.put(className, classDef);
        }
        return null;
    }

    @Override
    public Object visitImplementationSection(delphiParser.ImplementationSectionContext ctx) {
        String className = null, methodName = null;
        ParseTree body = null;
        delphiParser.FormalParameterListContext params = null;

        if (ctx.constructorImplementation() != null) {
            className  = ctx.constructorImplementation().IDENTIFIER(0).getText();
            methodName = ctx.constructorImplementation().IDENTIFIER(1).getText();
            body       = ctx.constructorImplementation().block();
            params     = ctx.constructorImplementation().formalParameterList();
        } else if (ctx.destructorImplementation() != null) {
            className  = ctx.destructorImplementation().IDENTIFIER(0).getText();
            methodName = ctx.destructorImplementation().IDENTIFIER(1).getText();
            body       = ctx.destructorImplementation().block();
        } else if (ctx.procedureImplementation() != null) {
            className  = ctx.procedureImplementation().IDENTIFIER(0).getText();
            methodName = ctx.procedureImplementation().IDENTIFIER(1).getText();
            body       = ctx.procedureImplementation().block();
            params     = ctx.procedureImplementation().formalParameterList();
        } else if (ctx.functionImplementation() != null) {
            className  = ctx.functionImplementation().IDENTIFIER(0).getText();
            methodName = ctx.functionImplementation().IDENTIFIER(1).getText();
            body       = ctx.functionImplementation().block();
            params     = ctx.functionImplementation().formalParameterList();
        }

        String key = className + "." + methodName;
        methodBodies.put(key, body);
        if (params != null) methodParams.put(key, params);
        return null;
    }

    // ─── Member Access / Method Calls ────────────────────────────────────────────

    @Override
    public Object visitMemberAccessExpr(delphiParser.MemberAccessExprContext ctx) {
        String firstId = ctx.IDENTIFIER(0).getText();
        String secondId = ctx.IDENTIFIER(1).getText();

        if (classDefinitions.containsKey(firstId)) {
            ObjectInstance obj = new ObjectInstance(firstId, classDefinitions.get(firstId));
            String key = resolveMethodKey(firstId, secondId);
            if (methodBodies.containsKey(key)) {
                Environment savedEnv = currentEnv;
                currentEnv = new Environment(currentEnv);
                ObjectInstance savedSelf = currentSelf;
                currentSelf = obj;
                String savedClassName = currentClassName;
                currentClassName = firstId;
                bindParameters(key, ctx.argumentList());
                visit(methodBodies.get(key));
                currentEnv = savedEnv;
                currentSelf = savedSelf;
                currentClassName = savedClassName;
            }
            return obj;
        }

        Object objValue = currentEnv.get(firstId);
        if (objValue instanceof ObjectInstance) {
            ObjectInstance obj = (ObjectInstance) objValue;
            String className = obj.getClassName();
            ClassDefinition classDef = classDefinitions.get(className);

            if (ctx.argumentList() == null && classDef.getFields().containsKey(secondId)) {
                checkAccess(secondId, classDef.getFieldVisibility(secondId), className);
                return obj.getField(secondId);
            }

            checkAccess(secondId, classDef.getMethodVisibility(secondId), className);
            String key = resolveMethodKey(className, secondId);
            if (methodBodies.containsKey(key)) {
                Environment savedEnv = currentEnv;
                Environment methodEnv = new Environment(currentEnv);
                currentEnv = methodEnv;
                ObjectInstance savedSelf = currentSelf;
                currentSelf = obj;
                String savedClassName = currentClassName;
                currentClassName = className;
                bindParameters(key, ctx.argumentList());
                visit(methodBodies.get(key));
                Object returnValue = methodEnv.exists(secondId) ? methodEnv.get(secondId) : null;
                currentEnv = savedEnv;
                currentSelf = savedSelf;
                currentClassName = savedClassName;
                return returnValue;
            }
        }
        throw new RuntimeException("Unknown member access: " + firstId + "." + secondId);
    }

    @Override
    public Object visitMethodCallStatement(delphiParser.MethodCallStatementContext ctx) {
        String objName = ctx.IDENTIFIER(0).getText();
        String methodName = ctx.IDENTIFIER(1).getText();
        delphiParser.ArgumentListContext argCtx = ctx.argumentList();

        Object objValue = currentEnv.get(objName);
        if (objValue instanceof ObjectInstance) {
            ObjectInstance obj = (ObjectInstance) objValue;
            String className = obj.getClassName();
            ClassDefinition classDef = classDefinitions.get(className);

            if (methodName.equals("Destroy")) {
                String key = resolveMethodKey(className, methodName);
                if (methodBodies.containsKey(key)) {
                    Environment savedEnv = currentEnv;
                    currentEnv = new Environment(currentEnv);
                    ObjectInstance savedSelf = currentSelf;
                    currentSelf = obj;
                    String savedClassName = currentClassName;
                    currentClassName = className;
                    visit(methodBodies.get(key));
                    currentEnv = savedEnv;
                    currentSelf = savedSelf;
                    currentClassName = savedClassName;
                }
                currentEnv.set(objName, null);
                return null;
            }

            checkAccess(methodName, classDef.getMethodVisibility(methodName), className);
            String key = resolveMethodKey(className, methodName);
            if (methodBodies.containsKey(key)) {
                Environment savedEnv = currentEnv;
                currentEnv = new Environment(currentEnv);
                ObjectInstance savedSelf = currentSelf;
                currentSelf = obj;
                String savedClassName = currentClassName;
                currentClassName = className;
                bindParameters(key, argCtx);
                visit(methodBodies.get(key));
                currentEnv = savedEnv;
                currentSelf = savedSelf;
                currentClassName = savedClassName;
            }
        }
        return null;
    }

    // ─── Standalone Procedures & Functions (M3) ───────────────────────────────

    @Override
    public Object visitStandaloneProcDecl(delphiParser.StandaloneProcDeclContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        procedures.put(name, new ProcedureDefinition(
            name, ctx.formalParameterList(), ctx.variableDeclarationSection(), ctx.block(), false
        ));
        return null;
    }

    @Override
    public Object visitStandaloneFuncDecl(delphiParser.StandaloneFuncDeclContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        procedures.put(name, new ProcedureDefinition(
            name, ctx.formalParameterList(), ctx.variableDeclarationSection(), ctx.block(), true
        ));
        return null;
    }

    private Object executeStandaloneProcOrFunc(String name, delphiParser.ArgumentListContext argCtx) {
        ProcedureDefinition procDef = procedures.get(name);
        if (procDef == null) throw new RuntimeException("Undefined procedure/function: " + name);

        // Evaluate arguments in CALLER's environment before switching scope
        List<String> paramNames = (procDef.getParams() != null) ? getParamNames(procDef.getParams()) : new ArrayList<>();
        List<Object> argValues = (argCtx != null) ? evalArguments(argCtx) : new ArrayList<>();

        // Static scoping: parent is global env, NOT caller's env
        Environment savedEnv = currentEnv;
        currentEnv = new Environment(globalEnv);

        // Bind formal parameters
        for (int i = 0; i < paramNames.size() && i < argValues.size(); i++) {
            currentEnv.define(paramNames.get(i), argValues.get(i));
        }

        // Declare local variables
        if (procDef.getLocals() != null) {
            visit(procDef.getLocals());
        }

        // Execute body
        visit(procDef.getBody());

        // For functions, the return value is stored as a variable with the function's name
        Object returnValue = null;
        if (procDef.isFunction() && currentEnv.existsLocally(name)) {
            returnValue = currentEnv.get(name);
        }

        currentEnv = savedEnv;
        return returnValue;
    }

    @Override
    public Object visitFunctionCallExpr(delphiParser.FunctionCallExprContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        if (!procedures.containsKey(name)) {
            throw new RuntimeException("Unknown function: " + name);
        }
        return executeStandaloneProcOrFunc(name, ctx.argumentList());
    }

    @Override
    public Object visitProcedureCallStatement(delphiParser.ProcedureCallStatementContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        if (!procedures.containsKey(name)) {
            throw new RuntimeException("Unknown procedure: " + name);
        }
        executeStandaloneProcOrFunc(name, ctx.argumentList());
        return null;
    }
}