lexer grammar SwiftLexer;

@header {
package org.example;
}

IMPORT  : 'import';
LET     : 'let';
VAR     : 'var';
FUNC    : 'func';
CLASS   : 'class';
INIT    : 'init';
SELF    : 'self';
DEINIT  : 'deinit';
IF      : 'if';
ELSE    : 'else';
FOR     : 'for';
WHILE   : 'while';
IN      : 'in';
RETURN  : 'return';
PRINT   : 'print';
TRUE    : 'true';
FALSE   : 'false';
NIL     : 'nil';

TYPE_INT    : 'Int';
TYPE_STRING : 'String';
TYPE_DOUBLE : 'Double';
TYPE_BOOL   : 'Bool';
TYPE_VOID   : 'Void';

ARROW       : '->';
PLUS_ASSIGN : '+=';
MINUS_ASSIGN: '-=';
EQUAL       : '==';
NOT_EQUAL   : '!=';
LE          : '<=';
GE          : '>=';
AND         : '&&';
OR          : '||';

ASSIGN      : '=';
PLUS        : '+';
MINUS       : '-';
MULT        : '*';
DIV         : '/';
MOD         : '%';
NOT         : '!';

LT          : '<';
GT          : '>';

LPAREN      : '(';
RPAREN      : ')';
LBRACE      : '{';
RBRACE      : '}';
LBRACK      : '[';
RBRACK      : ']';

DOT         : '.';
COMMA       : ',';
COLON       : ':';
SEMICOL     : ';';
QUESTION    : '?';

IDENTIFIER  : [a-zA-Z_][a-zA-Z0-9_]*;

DOUBLE  : [0-9]+ '.' [0-9]+;
INT     : [0-9]+;

STRING
    : '"' ( ~["\\\r\n] | '\\' . )* '"'
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' -> pushMode(COMMENT), skip
    ;

WS : [ \t\r\n]+ -> skip;

UNCLOSED_STRING
    : '"' ( ~["\\\r\n] | '\\' . )*
    ;

INVALID_NUMBER
    : [0-9]+ ('.' [0-9]+)+
    ;

INVALID_CHAR
    : .
    ;

mode COMMENT;

COMMENT_OPEN
    : '/*' -> pushMode(COMMENT), skip
    ;

COMMENT_CLOSE
    : '*/' -> popMode, skip
    ;

COMMENT_CONTENT
    : . -> skip
    ;