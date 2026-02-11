grammar delphi;

// --- Parser Rules ---

program
    : programHeader variableDeclarationSection? block '.'
    ;

programHeader
    : 'program' IDENTIFIER ';'
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
    ;

block
    : 'begin' statementList 'end'
    ;

statementList
    : (statement ';')*
    ;

statement
    : assignmentStatement
    | writelnStatement
    | /* empty statement */
    ;

assignmentStatement
    : IDENTIFIER ':=' expression
    ;

writelnStatement
    : 'writeln' '(' expression ')'
    ;

expression
    : expression ('*' | '/') expression  # MultiplicativeExpr
    | expression ('+' | '-') expression  # AdditiveExpr
    | atom                               # AtomExpr
    ;

atom
    : IDENTIFIER         # IdentifierExpr
    | INTEGER_LITERAL    # IntegerExpr
    | STRING_LITERAL     # StringExpr
    | '(' expression ')' # ParenExpr
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

WS
    : [ \t\r\n]+ -> skip
    ;