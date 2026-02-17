grammar delphi;

// --- Parser Rules ---

program
    : programHeader typeSection? implementationSection* variableDeclarationSection? block '.'
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
    | writelnStatement
    | readlnStatement
    | /* empty */
    ;

readlnStatement
    : 'readln' '(' IDENTIFIER ')'
    ;
    
methodCallStatement
    : IDENTIFIER '.' IDENTIFIER argumentList?
    ;

assignmentStatement
    : IDENTIFIER ':=' expression                      // x := 10
    | IDENTIFIER '.' IDENTIFIER ':=' expression       // obj.Field := 10
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
    : IDENTIFIER '.' IDENTIFIER argumentList?    # MemberAccessExpr
    | IDENTIFIER argumentList                     # FunctionCallExpr
    | IDENTIFIER                                  # IdentifierExpr
    | INTEGER_LITERAL                             # IntegerExpr
    | STRING_LITERAL                              # StringExpr
    | '(' expression ')'                          # ParenExpr
    ;

argumentList
    : '(' (expression (',' expression)*)? ')'
    ;

typeSection
    : 'type' classDeclaration+
    ;

classDeclaration
    : IDENTIFIER '=' 'class' classBody 'end' ';'
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

WS
    : [ \t\r\n]+ -> skip
    ;