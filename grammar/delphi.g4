grammar delphi;

program
    : 'program' IDENTIFIER ';' 'begin' statement* 'end' '.'
    ;

statement
    : 'writeln' '(' STRING_LITERAL ')' ';'
    ;

IDENTIFIER : [a-zA-Z]+ ;
STRING_LITERAL : '\'' .*? '\'' ;
WS : [ \t\r\n]+ -> skip ;