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
    private Map<String, delphiParser.FormalParameterListContext> methodParams = new HashMap<>();
    private Map<String, InterfaceDefinition> interfaceDefinitions = new HashMap<>();

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
                ClassDefinition classDef = classDefinitions.get(obj.getClassName());
                checkAccess(fieldName, classDef.getFieldVisibility(fieldName), obj.getClassName());
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
        // Register interfaces first
        for (delphiParser.InterfaceDeclarationContext ifaceCtx : ctx.interfaceDeclaration()) {
            String ifaceName = ifaceCtx.IDENTIFIER().getText();
            InterfaceDefinition ifaceDef = new InterfaceDefinition(ifaceName);
            for (delphiParser.InterfaceMethodDeclarationContext methodCtx : ifaceCtx.interfaceBody().interfaceMethodDeclaration()) {
                if (methodCtx.procedureDeclaration() != null) {
                    ifaceDef.addMethod(methodCtx.procedureDeclaration().IDENTIFIER().getText());
                } else if (methodCtx.functionDeclaration() != null) {
                    ifaceDef.addMethod(methodCtx.functionDeclaration().IDENTIFIER().getText());
                }
            }
            interfaceDefinitions.put(ifaceName, ifaceDef);
        }

        // Register classes
        for (delphiParser.ClassDeclarationContext classCtx : ctx.classDeclaration()) {
            String className = classCtx.IDENTIFIER().getText();
            ClassDefinition classDef = new ClassDefinition(className);

            // Handle parent class and interfaces
            if (classCtx.classParentList() != null) {
                List<TerminalNode> parents = classCtx.classParentList().IDENTIFIER();
                for (TerminalNode parent : parents) {
                    String parentName = parent.getText();
                    if (classDefinitions.containsKey(parentName)) {
                        classDef.setParentClassName(parentName);
                        classDef.inheritFrom(classDefinitions.get(parentName));
                    } else if (interfaceDefinitions.containsKey(parentName)) {
                        classDef.addImplementedInterface(parentName);
                    }
                }
            }

            // Process class body (same as before)
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
                        if (member.methodDeclaration().procedureDeclaration() != null) {
                            classDef.addMethodVisibility(member.methodDeclaration().procedureDeclaration().IDENTIFIER().getText(), visibility);
                        } else if (member.methodDeclaration().functionDeclaration() != null) {
                            classDef.addMethodVisibility(member.methodDeclaration().functionDeclaration().IDENTIFIER().getText(), visibility);
                        }
                    }
                    if (member.constructorDeclaration() != null) {
                        classDef.addMethodVisibility(member.constructorDeclaration().IDENTIFIER().getText(), visibility);
                    }
                    if (member.destructorDeclaration() != null) {
                        classDef.addMethodVisibility(member.destructorDeclaration().IDENTIFIER().getText(), visibility);
                    }
                }
            }

            // Validate interfaces
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
        String className = null;
        String methodName = null;
        ParseTree body = null;
        delphiParser.FormalParameterListContext params = null;

        if (ctx.constructorImplementation() != null) {
            className = ctx.constructorImplementation().IDENTIFIER(0).getText();
            methodName = ctx.constructorImplementation().IDENTIFIER(1).getText();
            body = ctx.constructorImplementation().block();
            params = ctx.constructorImplementation().formalParameterList();
        } else if (ctx.destructorImplementation() != null) {
            className = ctx.destructorImplementation().IDENTIFIER(0).getText();
            methodName = ctx.destructorImplementation().IDENTIFIER(1).getText();
            body = ctx.destructorImplementation().block();
        } else if (ctx.procedureImplementation() != null) {
            className = ctx.procedureImplementation().IDENTIFIER(0).getText();
            methodName = ctx.procedureImplementation().IDENTIFIER(1).getText();
            body = ctx.procedureImplementation().block();
            params = ctx.procedureImplementation().formalParameterList();
        } else if (ctx.functionImplementation() != null) {
            className = ctx.functionImplementation().IDENTIFIER(0).getText();
            methodName = ctx.functionImplementation().IDENTIFIER(1).getText();
            body = ctx.functionImplementation().block();
            params = ctx.functionImplementation().formalParameterList();
        }

        String key = className + "." + methodName;
        methodBodies.put(key, body);
        if (params != null) {
            methodParams.put(key, params);
        }
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
            String key = resolveMethodKey(className, methodName);
            if (methodBodies.containsKey(key)) {
                Environment savedEnv = currentEnv;
                currentEnv = new Environment(currentEnv);
                ObjectInstance savedSelf = currentSelf;
                currentSelf = obj;
                String savedClassName = currentClassName;
                currentClassName = className;

                bindParameters(key, ctx.argumentList());
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

            if (ctx.argumentList() == null && classDef.getFields().containsKey(memberName)) {
                checkAccess(memberName, classDef.getFieldVisibility(memberName), className);
                return obj.getField(memberName);
            }

            String methodName = memberName;
            checkAccess(methodName, classDef.getMethodVisibility(methodName), className);

            String key = resolveMethodKey(className, methodName);
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
}