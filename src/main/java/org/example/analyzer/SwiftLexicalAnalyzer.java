package org.example.analyzer;

import org.example.SwiftLexer;
import org.antlr.v4.runtime.*;
import java.util.*;

public class SwiftLexicalAnalyzer {

    public static class Result {
        public final List<Token> tokens; // список токенів
        public final List<Diagnostic> diagnostics; // список помилок

        public Result(List<Token> tokens, List<Diagnostic> diagnostics) {
            this.tokens = tokens;
            this.diagnostics = diagnostics;
        }
    }

    public Result analyze(String input) {
        CharStream stream = CharStreams.fromString(input); // перетворення рядка на потік символів
        SwiftLexer lexer = new SwiftLexer(stream);

        // ініціалізація кастомного оброника помилок
        ErrorListener errorListener = new ErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokenStream = new CommonTokenStream(lexer); // буфер токенів
        tokenStream.fill();

        List<Token> tokens = tokenStream.getTokens(); // отримання токенів
        List<Diagnostic> diagnostics = new ArrayList<>(); // ініціалізація списку помилок

        // аналіз токенів
        for (Token t : tokens) {
            if (t.getType() == Token.EOF) break;

            // обробка помилок з граматики
            switch (t.getType()) {
                case SwiftLexer.UNCLOSED_STRING:
                    diagnostics.add(new Diagnostic(
                            Diagnostic.Severity.ERROR,
                            "Unclosed line",
                            t.getLine(),
                            t.getCharPositionInLine()
                    ));
                    break;

                case SwiftLexer.INVALID_CHAR:
                    diagnostics.add(new Diagnostic(
                            Diagnostic.Severity.ERROR,
                            "Invalid character: " + t.getText(),
                            t.getLine(),
                            t.getCharPositionInLine()
                    ));
                    break;

                case SwiftLexer.INVALID_NUMBER:
                    diagnostics.add(new Diagnostic(
                            Diagnostic.Severity.ERROR,
                            "Invalid format of number: " + t.getText(),
                            t.getLine(),
                            t.getCharPositionInLine()
                    ));
                    break;
            }
        }

        // помилки від ANTLR
        if (errorListener.hasErrors()) {
            diagnostics.addAll(errorListener.getDiagnostics());
        }

        return new Result(tokens, diagnostics);
    }
}
