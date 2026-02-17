package org.example.analyzer;

import org.example.SwiftLexer;
import org.antlr.v4.runtime.*;
import java.util.*;

public class SwiftLexicalAnalyzer {

    public static class Result {
        public final List<Token> tokens;
        public final List<Diagnostic> diagnostics;

        public Result(List<Token> tokens, List<Diagnostic> diagnostics) {
            this.tokens = tokens;
            this.diagnostics = diagnostics;
        }
    }

    public Result analyze(String input) {
        CharStream stream = CharStreams.fromString(input);
        SwiftLexer lexer = new SwiftLexer(stream);

        LexerErrorListener errorListener = new LexerErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();

        List<Token> tokens = tokenStream.getTokens();
        List<Diagnostic> diagnostics = new ArrayList<>();

        for (Token t : tokens) {
            if (t.getType() == Token.EOF) break;

            switch (t.getType()) {
                case SwiftLexer.UNCLOSED_STRING:
                    diagnostics.add(new Diagnostic(
                            Diagnostic.Severity.ERROR,
                            "Unclosed line",
                            t.getLine()
                    ));
                    break;

                case SwiftLexer.INVALID_CHAR:
                    diagnostics.add(new Diagnostic(
                            Diagnostic.Severity.ERROR,
                            "Invalid character: " + t.getText(),
                            t.getLine()
                    ));
                    break;

                case SwiftLexer.INVALID_NUMBER:
                    diagnostics.add(new Diagnostic(
                            Diagnostic.Severity.ERROR,
                            "Invalid format of number: " + t.getText(),
                            t.getLine()
                    ));
                    break;
            }
        }

        if (errorListener.hasErrors()) {
            for (String msg : errorListener.getErrorMessages()) {
                diagnostics.add(new Diagnostic(
                        Diagnostic.Severity.ERROR,
                        msg,
                        -1
                ));
            }
        }

        return new Result(tokens, diagnostics);
    }
}
