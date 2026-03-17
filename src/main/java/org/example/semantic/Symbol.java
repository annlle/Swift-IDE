package org.example.semantic;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    public String name;
    public Type type;
    public boolean isUsed;
    public int line;
    public int column;

    public List<Type> paramTypes = new ArrayList<>();
    public Type returnType = Type.VOID;

    public Symbol(String name, Type type, boolean isUsed, int line, int column) {
        this.name = name;
        this.type = type;
        this.isUsed = isUsed;
        this.line = line;
        this.column = column;
    }
}