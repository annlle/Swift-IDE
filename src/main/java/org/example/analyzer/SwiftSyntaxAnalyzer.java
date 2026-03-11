package org.example.analyzer;

import org.antlr.v4.runtime.*;
import org.example.SwiftLexer;
import org.example.SwiftParser;
import org.example.ast.AstBuilder;
import org.example.ast.AstNode;

import java.util.*;

public class SwiftSyntaxAnalyzer {

    public static class SyntaxResult {
        public final AstNode ast;
        public final List<String> errors;

        public SyntaxResult(AstNode ast, List<String> errors) {
            this.ast = ast;
            this.errors = errors;
        }
    }

    public SyntaxResult analyze(String input) {
        CharStream stream = CharStreams.fromString(input);

        SwiftLexer lexer = new SwiftLexer(stream);
        ErrorListener errorListener = new ErrorListener();

        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        SwiftParser parser = new SwiftParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        SwiftParser.ProgramContext parseTree = parser.program();

        List<String> errors = errorListener.getErrorMessages();

        AstNode ast = null;
        if (errors.isEmpty()) {
            AstBuilder builder = new AstBuilder();
            ast = builder.visit(parseTree);
        }

        return new SyntaxResult(ast, errors);
    }
}