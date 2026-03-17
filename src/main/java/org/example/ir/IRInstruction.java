package org.example.ir;

public class IRInstruction {
    private final String result;
    private final String arg1;
    private final String op;
    private final String arg2;

    public IRInstruction(String result, String arg1, String op, String arg2) {
        this.result = result;
        this.arg1 = arg1;
        this.op = op;
        this.arg2 = arg2;
    }

    @Override
    public String toString() {
        if (op == null) {
            return result + " = " + arg1;
        }
        return result + " = " + arg1 + " " + op + " " + arg2;
    }
}