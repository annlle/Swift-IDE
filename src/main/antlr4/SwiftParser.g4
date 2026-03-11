parser grammar SwiftParser;

options { tokenVocab=SwiftLexer; }

@header {
package org.example;
}

// ================= PROGRAM =================

program
    : (importDecl | declaration)* EOF
    ;

importDecl
    : IMPORT IDENTIFIER (DOT IDENTIFIER)* SEMICOL?
    ;

// ================= DECLARATIONS =================

declaration
    : variableDecl
    | constantDecl
    | classDecl
    | functionDecl
    | initDecl
    | statement
    ;

// ================= VARIABLES =================

variableDecl
    : VAR IDENTIFIER typeAnnotation? (ASSIGN expression)? SEMICOL?
    ;

constantDecl
    : LET IDENTIFIER typeAnnotation? ASSIGN expression SEMICOL?
    ;

// ================= TYPES =================

typeAnnotation
    : COLON type
    ;

type
    : IDENTIFIER
    | TYPE_INT
    | TYPE_STRING
    | TYPE_DOUBLE
    | TYPE_BOOL
    | TYPE_VOID
    | LBRACK type RBRACK
    | LBRACK type COLON type RBRACK
    ;

// ================= CLASS =================

classDecl
    : CLASS IDENTIFIER LBRACE declaration* RBRACE
    ;

initDecl
    : INIT LPAREN parameterList? RPAREN block
    ;

// ================= FUNCTIONS =================

functionDecl
    : FUNC IDENTIFIER LPAREN parameterList? RPAREN returnType? block
    ;

parameterList
    : parameter (COMMA parameter)*
    ;

parameter
    : IDENTIFIER COLON type
    ;

returnType
    : ARROW type
    ;

// ================= STATEMENTS =================

statement
    : ifStatement
    | forStatement
    | whileStatement
    | returnStatement
    | printStatement
    | expressionStatement
    | block
    ;

block
    : LBRACE declaration* RBRACE
    ;

printStatement
    : PRINT LPAREN argumentList? RPAREN SEMICOL?
    ;

// ================= CONTROL FLOW =================

ifStatement
    : IF expression block (ELSE (ifStatement | block))?
    ;

forStatement
    : FOR IDENTIFIER IN expression block
    ;

whileStatement
    : WHILE expression block
    ;

returnStatement
    : RETURN expression? SEMICOL?
    ;

expressionStatement
    : expression SEMICOL?
    ;

// ================= EXPRESSIONS =================

expression
    : assignment
    ;

assignment
    : logicOr ((ASSIGN | PLUS_ASSIGN | MINUS_ASSIGN) assignment)?
    ;

logicOr
    : logicAnd (OR logicAnd)*
    ;

logicAnd
    : equality (AND equality)*
    ;

equality
    : comparison ((EQUAL | NOT_EQUAL) comparison)*
    ;

comparison
    : term ((LT | GT | LE | GE) term)*
    ;

term
    : factor ((PLUS | MINUS) factor)*
    ;

factor
    : unary ((MULT | DIV | MOD) unary)*
    ;

unary
    : (NOT | MINUS) unary
    | call
    ;

// ================= CALL / OBJECT ACCESS =================

call
    : primary ( LPAREN argumentList? RPAREN
              | DOT (IDENTIFIER | PRINT)
              | LBRACK expression RBRACK )*
    ;

argumentList
    : expression (COMMA expression)*
    ;

// ================= PRIMARY EXPRESSIONS =================

primary
    : INT
    | DOUBLE
    | STRING
    | TRUE
    | FALSE
    | NIL
    | IDENTIFIER
    | SELF
    | arrayLiteral
    | dictionaryLiteral
    | LPAREN expression RPAREN
    ;

// ================= LITERALS =================

arrayLiteral
    : LBRACK (expression (COMMA expression)*)? RBRACK
    ;

dictionaryLiteral
    : LBRACK (expression COLON expression
        (COMMA expression COLON expression)*)? RBRACK
    ;