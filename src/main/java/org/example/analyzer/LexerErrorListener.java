package org.example.analyzer;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import java.util.ArrayList;
import java.util.List;

public class LexerErrorListener extends BaseErrorListener {
    private final List<String> errorMessages = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        errorMessages.add(String.format("Line %d:%d — %s", line, charPositionInLine, msg));
    }

    public List<String> getErrorMessages() { return errorMessages; }
    public boolean hasErrors() { return !errorMessages.isEmpty(); }
}