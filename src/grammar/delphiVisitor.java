// Generated from grammar/delphi.g4 by ANTLR 4.13.2
package grammar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link delphiParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface delphiVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link delphiParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(delphiParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#programHeader}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgramHeader(delphiParser.ProgramHeaderContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#implementationSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImplementationSection(delphiParser.ImplementationSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#procedureImplementation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedureImplementation(delphiParser.ProcedureImplementationContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#functionImplementation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionImplementation(delphiParser.FunctionImplementationContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#constructorImplementation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorImplementation(delphiParser.ConstructorImplementationContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#destructorImplementation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDestructorImplementation(delphiParser.DestructorImplementationContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#variableDeclarationSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclarationSection(delphiParser.VariableDeclarationSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#variableDeclarationList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclarationList(delphiParser.VariableDeclarationListContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#variableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaration(delphiParser.VariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#identifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierList(delphiParser.IdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(delphiParser.TypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(delphiParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#statementList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementList(delphiParser.StatementListContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(delphiParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#methodCallStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCallStatement(delphiParser.MethodCallStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#assignmentStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#writelnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWritelnStatement(delphiParser.WritelnStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MultiplicativeExpr}
	 * labeled alternative in {@link delphiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpr(delphiParser.MultiplicativeExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AdditiveExpr}
	 * labeled alternative in {@link delphiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpr(delphiParser.AdditiveExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AtomExpr}
	 * labeled alternative in {@link delphiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomExpr(delphiParser.AtomExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MemberAccessExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberAccessExpr(delphiParser.MemberAccessExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FunctionCallExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCallExpr(delphiParser.FunctionCallExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IdentifierExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierExpr(delphiParser.IdentifierExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IntegerExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerExpr(delphiParser.IntegerExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StringExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringExpr(delphiParser.StringExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link delphiParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenExpr(delphiParser.ParenExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(delphiParser.ArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#typeSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeSection(delphiParser.TypeSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#classDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassDeclaration(delphiParser.ClassDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#classBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassBody(delphiParser.ClassBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#classVisibilitySection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassVisibilitySection(delphiParser.ClassVisibilitySectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#visibilityModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVisibilityModifier(delphiParser.VisibilityModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#classMember}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassMember(delphiParser.ClassMemberContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#fieldDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldDeclaration(delphiParser.FieldDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#methodDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodDeclaration(delphiParser.MethodDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#constructorDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorDeclaration(delphiParser.ConstructorDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#destructorDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDestructorDeclaration(delphiParser.DestructorDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#procedureDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedureDeclaration(delphiParser.ProcedureDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#functionDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDeclaration(delphiParser.FunctionDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#formalParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameterList(delphiParser.FormalParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link delphiParser#formalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameter(delphiParser.FormalParameterContext ctx);
}