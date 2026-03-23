package org.example.ir;

public class TempVarGenerator {
    private int counter = 0;

    public String next() {
        return "t" + (counter++);
    }
}
