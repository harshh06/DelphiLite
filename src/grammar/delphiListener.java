// Generated from grammar/delphi.g4 by ANTLR 4.13.2
package grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link delphiParser}.
 */
public interface delphiListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link delphiParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(delphiParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(delphiParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#programHeader}.
	 * @param ctx the parse tree
	 */
	void enterProgramHeader(delphiParser.ProgramHeaderContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#programHeader}.
	 * @param ctx the parse tree
	 */
	void exitProgramHeader(delphiParser.ProgramHeaderContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#implementationSection}.
	 * @param ctx the parse tree
	 */
	void enterImplementationSection(delphiParser.ImplementationSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#implementationSection}.
	 * @param ctx the parse tree
	 */
	void exitImplementationSection(delphiParser.ImplementationSectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#procedureImplementation}.
	 * @param ctx the parse tree
	 */
	void enterProcedureImplementation(delphiParser.ProcedureImplementationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#procedureImplementation}.
	 * @param ctx the parse tree
	 */
	void exitProcedureImplementation(delphiParser.ProcedureImplementationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#functionImplementation}.
	 * @param ctx the parse tree
	 */
	void enterFunctionImplementation(delphiParser.FunctionImplementationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#functionImplementation}.
	 * @param ctx the parse tree
	 */
	void exitFunctionImplementation(delphiParser.FunctionImplementationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#constructorImplementation}.
	 * @param ctx the parse tree
	 */
	void enterConstructorImplementation(delphiParser.ConstructorImplementationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#constructorImplementation}.
	 * @param ctx the parse tree
	 */
	void exitConstructorImplementation(delphiParser.ConstructorImplementationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#destructorImplementation}.
	 * @param ctx the parse tree
	 */
	void enterDestructorImplementation(delphiParser.DestructorImplementationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#destructorImplementation}.
	 * @param ctx the parse tree
	 */
	void exitDestructorImplementation(delphiParser.DestructorImplementationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#variableDeclarationSection}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclarationSection(delphiParser.VariableDeclarationSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#variableDeclarationSection}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclarationSection(delphiParser.VariableDeclarationSectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#variableDeclarationList}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclarationList(delphiParser.VariableDeclarationListContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#variableDeclarationList}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclarationList(delphiParser.VariableDeclarationListContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclaration(delphiParser.VariableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclaration(delphiParser.VariableDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierList(delphiParser.IdentifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierList(delphiParser.IdentifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterTypeName(delphiParser.TypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitTypeName(delphiParser.TypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(delphiParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(delphiParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#statementList}.
	 * @param ctx the parse tree
	 */
	void enterStatementList(delphiParser.StatementListContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#statementList}.
	 * @param ctx the parse tree
	 */
	void exitStatementList(delphiParser.StatementListContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(delphiParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(delphiParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#readlnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReadlnStatement(delphiParser.ReadlnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#readlnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReadlnStatement(delphiParser.ReadlnStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#methodCallStatement}.
	 * @param ctx the parse tree
	 */
	void enterMethodCallStatement(delphiParser.MethodCallStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#methodCallStatement}.
	 * @param ctx the parse tree
	 */
	void exitMethodCallStatement(delphiParser.MethodCallStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#assignmentStatement}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentStatement(delphiParser.AssignmentStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#assignmentStatement}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentStatement(delphiParser.AssignmentStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#writelnStatement}.
	 * @param ctx the parse tree
	 */
	void enterWritelnStatement(delphiParser.WritelnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#writelnStatement}.
	 * @param ctx the parse tree
	 */
	void exitWritelnStatement(delphiParser.WritelnStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MultiplicativeExpr}
	 * labeled alternative in {@link delphiParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpr(delphiParser.MultiplicativeExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MultiplicativeExpr}
	 * labeled alternative in {@link delphiParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpr(delphiParser.MultiplicativeExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AdditiveExpr}
	 * labeled alternative in {@link delphiParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpr(delphiParser.AdditiveExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AdditiveExpr}
	 * labeled alternative in {@link delphiParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpr(delphiParser.AdditiveExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AtomExpr}
	 * labeled alternative in {@link delphiParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAtomExpr(delphiParser.AtomExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AtomExpr}
	 * labeled alternative in {@link delphiParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAtomExpr(delphiParser.AtomExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MemberAccessExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterMemberAccessExpr(delphiParser.MemberAccessExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MemberAccessExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitMemberAccessExpr(delphiParser.MemberAccessExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FunctionCallExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCallExpr(delphiParser.FunctionCallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FunctionCallExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCallExpr(delphiParser.FunctionCallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IdentifierExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierExpr(delphiParser.IdentifierExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IdentifierExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierExpr(delphiParser.IdentifierExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IntegerExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterIntegerExpr(delphiParser.IntegerExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IntegerExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitIntegerExpr(delphiParser.IntegerExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterStringExpr(delphiParser.StringExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitStringExpr(delphiParser.StringExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterParenExpr(delphiParser.ParenExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitParenExpr(delphiParser.ParenExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentList(delphiParser.ArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentList(delphiParser.ArgumentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#typeSection}.
	 * @param ctx the parse tree
	 */
	void enterTypeSection(delphiParser.TypeSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#typeSection}.
	 * @param ctx the parse tree
	 */
	void exitTypeSection(delphiParser.TypeSectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterClassDeclaration(delphiParser.ClassDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitClassDeclaration(delphiParser.ClassDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#classParentList}.
	 * @param ctx the parse tree
	 */
	void enterClassParentList(delphiParser.ClassParentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#classParentList}.
	 * @param ctx the parse tree
	 */
	void exitClassParentList(delphiParser.ClassParentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceDeclaration(delphiParser.InterfaceDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#interfaceDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceDeclaration(delphiParser.InterfaceDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#interfaceBody}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceBody(delphiParser.InterfaceBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#interfaceBody}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceBody(delphiParser.InterfaceBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterInterfaceMethodDeclaration(delphiParser.InterfaceMethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#interfaceMethodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitInterfaceMethodDeclaration(delphiParser.InterfaceMethodDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#classBody}.
	 * @param ctx the parse tree
	 */
	void enterClassBody(delphiParser.ClassBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#classBody}.
	 * @param ctx the parse tree
	 */
	void exitClassBody(delphiParser.ClassBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#classVisibilitySection}.
	 * @param ctx the parse tree
	 */
	void enterClassVisibilitySection(delphiParser.ClassVisibilitySectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#classVisibilitySection}.
	 * @param ctx the parse tree
	 */
	void exitClassVisibilitySection(delphiParser.ClassVisibilitySectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#visibilityModifier}.
	 * @param ctx the parse tree
	 */
	void enterVisibilityModifier(delphiParser.VisibilityModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#visibilityModifier}.
	 * @param ctx the parse tree
	 */
	void exitVisibilityModifier(delphiParser.VisibilityModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#classMember}.
	 * @param ctx the parse tree
	 */
	void enterClassMember(delphiParser.ClassMemberContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#classMember}.
	 * @param ctx the parse tree
	 */
	void exitClassMember(delphiParser.ClassMemberContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFieldDeclaration(delphiParser.FieldDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFieldDeclaration(delphiParser.FieldDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterMethodDeclaration(delphiParser.MethodDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#methodDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitMethodDeclaration(delphiParser.MethodDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterConstructorDeclaration(delphiParser.ConstructorDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitConstructorDeclaration(delphiParser.ConstructorDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#destructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterDestructorDeclaration(delphiParser.DestructorDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#destructorDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitDestructorDeclaration(delphiParser.DestructorDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#procedureDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterProcedureDeclaration(delphiParser.ProcedureDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#procedureDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitProcedureDeclaration(delphiParser.ProcedureDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDeclaration(delphiParser.FunctionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDeclaration(delphiParser.FunctionDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameterList(delphiParser.FormalParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#formalParameterList}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameterList(delphiParser.FormalParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link delphiParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameter(delphiParser.FormalParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link delphiParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameter(delphiParser.FormalParameterContext ctx);
}