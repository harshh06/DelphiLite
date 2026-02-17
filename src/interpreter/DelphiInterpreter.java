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

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: DelphiInterpreter <file.pas>");
            System.exit(1);
        }
        CharStream input = CharStreams.fromFileName(args[0]);
        delphiLexer lexer = new delphiLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        delphiParser parser = new delphiParser(tokens);
        ParseTree tree = parser.program();
        DelphiInterpreter interpreter = new DelphiInterpreter();
        interpreter.visit(tree);
    }

    @Override
    public Object visitProgram(delphiParser.ProgramContext ctx) {
        // Register all class definitions
        if (ctx.typeSection() != null) visit(ctx.typeSection());
        //  Register all method/constructor/destructor implementations
        for (delphiParser.ImplementationSectionContext impl : ctx.implementationSection()) {
            visit(impl);
        }
        //  Declare global variables
        if (ctx.variableDeclarationSection() != null) visit(ctx.variableDeclarationSection());
        //  Execute main block
        visit(ctx.block());
        return null;
    }

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
                default:        defaultValue = null; break; // class type — will be assigned later
            }
            currentEnv.define(name, defaultValue);
        }
        return null;
    }

    @Override
    public Object visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {
        List<TerminalNode> ids = ctx.IDENTIFIER();

        if (ids.size() == 2) {
            // obj.Field := value
            String objName = ids.get(0).getText();
            String fieldName = ids.get(1).getText();
            Object value = visit(ctx.expression());
            ObjectInstance obj = (ObjectInstance) currentEnv.get(objName);
            obj.setField(fieldName, value);
        } else {
            // x := value
            String varName = ids.get(0).getText();
            Object value = visit(ctx.expression());
            currentEnv.set(varName, value);
        }
        return null;
    }

    @Override
    public Object visitMultiplicativeExpr(delphiParser.MultiplicativeExprContext ctx) {
        int left = (int) visit(ctx.expression(0));
        int right = (int) visit(ctx.expression(1));
        String op = ctx.getChild(1).getText(); // the operator token
        if (op.equals("*")) return left * right;
        if (op.equals("/")) return left / right;
        throw new RuntimeException("Unknown operator: " + op);
    }

    @Override
    public Object visitAdditiveExpr(delphiParser.AdditiveExprContext ctx) {
        Object left = visit(ctx.expression(0));
        Object right = visit(ctx.expression(1));
        String op = ctx.getChild(1).getText();

        // String concatenation with +
        if (left instanceof String || right instanceof String) {
            return String.valueOf(left) + String.valueOf(right);
        }
        if (op.equals("+")) return (int) left + (int) right;
        if (op.equals("-")) return (int) left - (int) right;
        throw new RuntimeException("Unknown operator: " + op);
    }

    @Override
    public Object visitAtomExpr(delphiParser.AtomExprContext ctx) {
        return visit(ctx.atom());
    }

    @Override
    public Object visitIntegerExpr(delphiParser.IntegerExprContext ctx) {
        return Integer.parseInt(ctx.INTEGER_LITERAL().getText());
    }

    @Override
    public Object visitStringExpr(delphiParser.StringExprContext ctx) {
        String text = ctx.STRING_LITERAL().getText();
        return text.substring(1, text.length() - 1); // strip surrounding quotes
    }

    @Override
    public Object visitIdentifierExpr(delphiParser.IdentifierExprContext ctx) {
        return currentEnv.get(ctx.IDENTIFIER().getText());
    }

    @Override
    public Object visitParenExpr(delphiParser.ParenExprContext ctx) {
        return visit(ctx.expression());
    }

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
}