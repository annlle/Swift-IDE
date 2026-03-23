lexer grammar SwiftLexer;

@header {
package org.example;
}

// ================= KEYWORDS =================
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

// ================= TYPES =================
TYPE_INT    : 'Int';
TYPE_STRING : 'String';
TYPE_DOUBLE : 'Double';
TYPE_BOOL   : 'Bool';
TYPE_VOID   : 'Void';

// ================= OPERATORS =================
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

// ================= DELIMITERS =================
LPAREN      : '(';
RPAREN      : ')';
LBRACE      : '{';
RBRACE      : '}';
LBRACK      : '[';
RBRACK      : ']';

// ================= PUNCTUATION =================
DOT         : '.';
COMMA       : ',';
COLON       : ':';
SEMICOL     : ';';
QUESTION    : '?';

// ================= CONTROL FLOW =================
BREAK       : 'break';
CONTINUE    : 'continue';

// ================= IDENTIFIERS =================
IDENTIFIER  : [a-zA-Z_][a-zA-Z0-9_]*;

// ================= LITERALS =================
DOUBLE  : [0-9]+ '.' [0-9]+;
INT     : [0-9]+;
STRING
    : '"' ( ~["\\\r\n] | '\\' . )* '"'
    ;

// ================= COMMENTS =================
LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;
BLOCK_COMMENT
    : '/*' -> pushMode(COMMENT), skip
    ;

// ================= WHITESPACE =================
WS : [ \t\r\n]+ -> skip;

// ================= ERROR TOKENS =================
UNCLOSED_STRING
    : '"' ( ~["\\\r\n] | '\\' . )*
    ;
INVALID_NUMBER
    : [0-9]+ ('.' [0-9]+)+
    ;
INVALID_CHAR
    : .
    ;

// ================= COMMENT MODE =================
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