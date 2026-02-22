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
    private String currentClassName = null; // Track which class context we're in
    private ObjectInstance currentSelf = null; // Track current 'Self' object

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

            // Get object
            Object objValue = currentEnv.get(objName);
            if (objValue instanceof ObjectInstance) {
                ObjectInstance obj = (ObjectInstance) objValue;

                // Check visibility
                ClassDefinition classDef = classDefinitions.get(obj.getClassName());
                String visibility = classDef.getFieldVisibility(fieldName);
                if (visibility.equals("private") && !obj.getClassName().equals(currentClassName)) {
                    throw new RuntimeException("Cannot access private field '" + fieldName + "' from outside class");
                }

                obj.setField(fieldName, value);
            }
        } else {
            // x := value OR field := value (when inside method)
            String varName = ids.get(0).getText();
            Object value = visit(ctx.expression());

            // Check if it's a field of Self
            if (currentSelf != null) {
                ClassDefinition classDef = classDefinitions.get(currentSelf.getClassName());
                if (classDef.getFields().containsKey(varName)) {
                    currentSelf.setField(varName, value);
                    return null;
                }
            }

            // Try to set in current environment
            if (currentEnv.exists(varName)) {
                currentEnv.set(varName, value);
            } else {
                // Define it if it doesn't exist (for function return values)
                currentEnv.define(varName, value);
            }
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
        String name = ctx.IDENTIFIER().getText();

        // Check if it's 'Self' keyword
        if (name.equals("Self") && currentSelf != null) {
            return currentSelf;
        }

        // Check if it's a field of Self (when inside a method)
        if (currentSelf != null) {
            ClassDefinition classDef = classDefinitions.get(currentSelf.getClassName());
            if (classDef.getFields().containsKey(name)) {
                return currentSelf.getField(name);
            }
        }

        // Otherwise, get from environment
        return currentEnv.get(name);
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

    @Override
    public Object visitTypeSection(delphiParser.TypeSectionContext ctx) {
        // Register all class declarations
        for (delphiParser.ClassDeclarationContext classCtx : ctx.classDeclaration()) {
            String className = classCtx.IDENTIFIER().getText();
            ClassDefinition classDef = new ClassDefinition(className);

            // Process class body
            for (delphiParser.ClassVisibilitySectionContext visSection : classCtx.classBody().classVisibilitySection()) {
                String visibility = visSection.visibilityModifier().getText(); // "private", "public", or "protected"

                // Process each member in this visibility section
                for (delphiParser.ClassMemberContext member : visSection.classMember()) {

                    // Field declaration
                    if (member.fieldDeclaration() != null) {
                        delphiParser.FieldDeclarationContext fieldCtx = member.fieldDeclaration();
                        String typeName = fieldCtx.typeName().getText();
                        for (TerminalNode id : fieldCtx.identifierList().IDENTIFIER()) {
                            String fieldName = id.getText();
                            classDef.addField(fieldName, typeName, visibility);
                        }
                    }

                    // Method declarations (store visibility)
                    if (member.methodDeclaration() != null) {
                        if (member.methodDeclaration().procedureDeclaration() != null) {
                            String methodName = member.methodDeclaration().procedureDeclaration().IDENTIFIER().getText();
                            classDef.addMethodVisibility(methodName, visibility);
                        } else if (member.methodDeclaration().functionDeclaration() != null) {
                            String methodName = member.methodDeclaration().functionDeclaration().IDENTIFIER().getText();
                            classDef.addMethodVisibility(methodName, visibility);
                        }
                    }

                    // Constructor
                    if (member.constructorDeclaration() != null) {
                        String constructorName = member.constructorDeclaration().IDENTIFIER().getText();
                        classDef.addMethodVisibility(constructorName, visibility);
                    }

                    // Destructor
                    if (member.destructorDeclaration() != null) {
                        String destructorName = member.destructorDeclaration().IDENTIFIER().getText();
                        classDef.addMethodVisibility(destructorName, visibility);
                    }
                }
            }

            classDefinitions.put(className, classDef);
        }
        return null;
    }

    @Override
    public Object visitImplementationSection(delphiParser.ImplementationSectionContext ctx) {
        String className = null;
        String methodName = null;
        ParseTree body = null;

        // Constructor implementation
        if (ctx.constructorImplementation() != null) {
            className = ctx.constructorImplementation().IDENTIFIER(0).getText();
            methodName = ctx.constructorImplementation().IDENTIFIER(1).getText();
            body = ctx.constructorImplementation().block();
        }
        // Destructor implementation
        else if (ctx.destructorImplementation() != null) {
            className = ctx.destructorImplementation().IDENTIFIER(0).getText();
            methodName = ctx.destructorImplementation().IDENTIFIER(1).getText();
            body = ctx.destructorImplementation().block();
        }
        // Procedure implementation
        else if (ctx.procedureImplementation() != null) {
            className = ctx.procedureImplementation().IDENTIFIER(0).getText();
            methodName = ctx.procedureImplementation().IDENTIFIER(1).getText();
            body = ctx.procedureImplementation().block();
        }
        // Function implementation
        else if (ctx.functionImplementation() != null) {
            className = ctx.functionImplementation().IDENTIFIER(0).getText();
            methodName = ctx.functionImplementation().IDENTIFIER(1).getText();
            body = ctx.functionImplementation().block();
        }

        // Store method body with key "ClassName.MethodName"
        String key = className + "." + methodName;
        methodBodies.put(key, body);

        return null;
    }
    @Override
    public Object visitMemberAccessExpr(delphiParser.MemberAccessExprContext ctx) {
        String firstId = ctx.IDENTIFIER(0).getText();
        String secondId = ctx.IDENTIFIER(1).getText();

        // Check if this is object instantiation: TClassName.Create
        if (classDefinitions.containsKey(firstId)) {
            // This is a constructor call: TClassName.MethodName()
            String className = firstId;
            String methodName = secondId;

            ClassDefinition classDef = classDefinitions.get(className);

            // Create new object instance
            ObjectInstance obj = new ObjectInstance(className, classDef);

            // Execute constructor body if it exists
            String key = className + "." + methodName;
            if (methodBodies.containsKey(key)) {
                // Create new environment for constructor
                Environment constructorEnv = new Environment(currentEnv);
                Environment savedEnv = currentEnv;
                currentEnv = constructorEnv;

                // Set Self to the new object
                ObjectInstance savedSelf = currentSelf;
                currentSelf = obj;
                String savedClassName = currentClassName;
                currentClassName = className;

                // Execute constructor body
                visit(methodBodies.get(key));

                // Restore context
                currentEnv = savedEnv;
                currentSelf = savedSelf;
                currentClassName = savedClassName;
            }

            return obj;
        }

        // Otherwise, this is either a method call OR field access on an object
        Object objValue = currentEnv.get(firstId);
        if (objValue instanceof ObjectInstance) {
            ObjectInstance obj = (ObjectInstance) objValue;
            String memberName = secondId;
            String className = obj.getClassName();
            ClassDefinition classDef = classDefinitions.get(className);

            // *** NEW: Check if it's a FIELD first ***
            if (classDef.getFields().containsKey(memberName)) {
                // This is field access: obj.FieldName
                String visibility = classDef.getFieldVisibility(memberName);
                if (visibility.equals("private") && !className.equals(currentClassName)) {
                    throw new RuntimeException("Cannot access private field '" + memberName + "' from outside class");
                }
                // Return the field value
                return obj.getField(memberName);
            }

            // Otherwise, it's a method call: obj.Method()
            String methodName = memberName;

            // Check visibility
            String visibility = classDef.getMethodVisibility(methodName);
            if (visibility.equals("private") && !className.equals(currentClassName)) {
                throw new RuntimeException("Cannot access private method '" + methodName + "' from outside class");
            }

            // Execute method body
            String key = className + "." + methodName;
            if (methodBodies.containsKey(key)) {
                // Create new environment for method
                Environment methodEnv = new Environment(currentEnv);
                Environment savedEnv = currentEnv;
                currentEnv = methodEnv;

                // Set Self
                ObjectInstance savedSelf = currentSelf;
                currentSelf = obj;
                String savedClassName = currentClassName;
                currentClassName = className;

                // Execute method body
                visit(methodBodies.get(key));

                // Get function return value
                Object returnValue = null;
                if (methodEnv.exists(methodName)) {
                    returnValue = methodEnv.get(methodName);
                }

                // Restore context
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

        Object objValue = currentEnv.get(objName);
        if (objValue instanceof ObjectInstance) {
            ObjectInstance obj = (ObjectInstance) objValue;
            String className = obj.getClassName();

            // Special handling for Destroy
            if (methodName.equals("Destroy")) {
                // Execute destructor body
                String key = className + "." + methodName;
                if (methodBodies.containsKey(key)) {
                    Environment destructorEnv = new Environment(currentEnv);
                    Environment savedEnv = currentEnv;
                    currentEnv = destructorEnv;

                    ObjectInstance savedSelf = currentSelf;
                    currentSelf = obj;
                    String savedClassName = currentClassName;
                    currentClassName = className;

                    visit(methodBodies.get(key));

                    currentEnv = savedEnv;
                    currentSelf = savedSelf;
                    currentClassName = savedClassName;
                }

                // Set variable to null (object is destroyed)
                currentEnv.set(objName, null);
                return null;
            }

            // Regular method call
            ClassDefinition classDef = classDefinitions.get(className);
            String visibility = classDef.getMethodVisibility(methodName);
            if (visibility.equals("private") && !className.equals(currentClassName)) {
                throw new RuntimeException("Cannot access private method '" + methodName + "' from outside class");
            }

            String key = className + "." + methodName;
            if (methodBodies.containsKey(key)) {
                Environment methodEnv = new Environment(currentEnv);
                Environment savedEnv = currentEnv;
                currentEnv = methodEnv;

                ObjectInstance savedSelf = currentSelf;
                currentSelf = obj;
                String savedClassName = currentClassName;
                currentClassName = className;

                visit(methodBodies.get(key));

                currentEnv = savedEnv;
                currentSelf = savedSelf;
                currentClassName = savedClassName;
            }
        }

        return null;
    }
}