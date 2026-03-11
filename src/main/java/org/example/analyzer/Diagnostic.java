package org.example.analyzer;

public class Diagnostic {

    public enum Severity { ERROR, WARNING }

    private final Severity severity;
    private final String message;
    private final int line;
    private final int column;

    public Diagnostic(Severity severity, String message, int line, int column) {
        this.severity = severity;
        this.message = message;
        this.line = line;
        this.column = column;
    }

    public Severity getSeverity() { return severity; }
    public String getMessage() { return message; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
}
