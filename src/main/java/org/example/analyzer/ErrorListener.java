package org.example.analyzer;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import java.util.ArrayList;
import java.util.List;

public class ErrorListener extends BaseErrorListener {

    private final List<Diagnostic> diagnostics = new ArrayList<>();

    private void addDiagnostic(String message, int line, int column) {
        diagnostics.add(new Diagnostic(Diagnostic.Severity.ERROR, message, line, column));
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        addDiagnostic(msg, line, charPositionInLine);
    }

    public List<Diagnostic> getDiagnostics() { return diagnostics; }

    public boolean hasErrors() { return !diagnostics.isEmpty(); }
}