grammar delphi;

// --- Parser Rules ---

program
    : programHeader typeSection? (implementationSection | standaloneProcDecl | standaloneFuncDecl)* variableDeclarationSection? block '.'
    ;

standaloneProcDecl
    : 'procedure' IDENTIFIER formalParameterList? ';' variableDeclarationSection? block ';'
    ;

standaloneFuncDecl
    : 'function' IDENTIFIER formalParameterList? ':' typeName ';' variableDeclarationSection? block ';'
    ;

programHeader
    : 'program' IDENTIFIER ';'
    ;

implementationSection
    : procedureImplementation
    | functionImplementation
    | constructorImplementation
    | destructorImplementation
    ;

procedureImplementation
    : 'procedure' IDENTIFIER '.' IDENTIFIER formalParameterList? ';' variableDeclarationSection? block ';'
    ;

functionImplementation
    : 'function' IDENTIFIER '.' IDENTIFIER formalParameterList? ':' typeName ';' variableDeclarationSection? block ';'
    ;

constructorImplementation
    : 'constructor' IDENTIFIER '.' IDENTIFIER formalParameterList? ';' block ';'
    ;

destructorImplementation
    : 'destructor' IDENTIFIER '.' IDENTIFIER ';' block ';'
    ;

variableDeclarationSection
    : 'var' variableDeclarationList
    ;

variableDeclarationList
    : (variableDeclaration ';')+
    ;

variableDeclaration
    : identifierList ':' typeName
    ;

identifierList
    : IDENTIFIER (',' IDENTIFIER)*
    ;

typeName
    : 'Integer'
    | 'String'
    | 'Boolean'
    | IDENTIFIER
    ;

block
    : 'begin' statementList 'end'
    ;

statementList
    : (statement ';')*
    ;

statement
    : assignmentStatement
    | methodCallStatement
    | procedureCallStatement
    | writelnStatement
    | readlnStatement
    | ifStatement
    | whileStatement
    | forStatement
    | breakStatement
    | continueStatement
    | block
    | /* empty */
    ;

procedureCallStatement
    : IDENTIFIER argumentList?
    ;

ifStatement
    : 'if' expression 'then' statement ('else' statement)?
    ;

whileStatement
    : 'while' expression 'do' statement
    ;

forStatement
    : 'for' IDENTIFIER ':=' expression ('to' | 'downto') expression 'do' statement
    ;

breakStatement
    : 'break'
    ;

continueStatement
    : 'continue'
    ;

readlnStatement
    : 'readln' '(' IDENTIFIER ')'
    ;

methodCallStatement
    : IDENTIFIER '.' IDENTIFIER argumentList?
    ;

assignmentStatement
    : IDENTIFIER ':=' expression
    | IDENTIFIER '.' IDENTIFIER ':=' expression
    ;

writelnStatement
    : 'writeln' '(' expression ')'
    ;

// Expression rule — ordered highest to lowest precedence (ANTLR4 left-recursion)
expression
    : expression ('*' | '/') expression                                # MultiplicativeExpr
    | expression ('+' | '-') expression                                # AdditiveExpr
    | expression ('<' | '>' | '<=' | '>=' | '=' | '<>') expression    # CompareExpr
    | 'not' expression                                                  # NotExpr
    | expression 'and' expression                                       # AndExpr
    | expression 'or' expression                                        # OrExpr
    | atom                                                             # AtomExpr
    ;

atom
    : IDENTIFIER '.' IDENTIFIER argumentList?    # MemberAccessExpr
    | IDENTIFIER argumentList                     # FunctionCallExpr
    | IDENTIFIER                                  # IdentifierExpr
    | INTEGER_LITERAL                             # IntegerExpr
    | STRING_LITERAL                              # StringExpr
    | 'true'                                      # TrueExpr
    | 'false'                                     # FalseExpr
    | '(' expression ')'                          # ParenExpr
    ;

argumentList
    : '(' (expression (',' expression)*)? ')'
    ;

typeSection
    : 'type' (classDeclaration | interfaceDeclaration)+
    ;

classDeclaration
    : IDENTIFIER '=' 'class' ('(' classParentList ')')? classBody 'end' ';'
    ;

classParentList
    : IDENTIFIER (',' IDENTIFIER)*
    ;

interfaceDeclaration
    : IDENTIFIER '=' 'interface' interfaceBody 'end' ';'
    ;

interfaceBody
    : interfaceMethodDeclaration*
    ;

interfaceMethodDeclaration
    : procedureDeclaration
    | functionDeclaration
    ;

classBody
    : classVisibilitySection*
    ;

classVisibilitySection
    : visibilityModifier classMember+
    ;

visibilityModifier
    : 'private'
    | 'public'
    | 'protected'
    ;

classMember
    : fieldDeclaration
    | methodDeclaration
    | constructorDeclaration
    | destructorDeclaration
    ;

fieldDeclaration
    : identifierList ':' typeName ';'
    ;

methodDeclaration
    : procedureDeclaration
    | functionDeclaration
    ;

constructorDeclaration
    : 'constructor' IDENTIFIER formalParameterList? ';'
    ;

destructorDeclaration
    : 'destructor' IDENTIFIER ';'
    ;

procedureDeclaration
    : 'procedure' IDENTIFIER formalParameterList? ';'
    ;

functionDeclaration
    : 'function' IDENTIFIER formalParameterList? ':' typeName ';'
    ;

formalParameterList
    : '(' formalParameter (';' formalParameter)* ')'
    ;

formalParameter
    : identifierList ':' typeName
    ;


// --- Lexer Rules ---

IDENTIFIER
    : [a-zA-Z_] [a-zA-Z_0-9]*
    ;

INTEGER_LITERAL
    : [0-9]+
    ;

STRING_LITERAL
    : '\'' .*? '\''
    ;

COMMENT
    : '{' .*? '}' -> skip
    ;

WS
    : [ \t\r\n]+ -> skip
    ;