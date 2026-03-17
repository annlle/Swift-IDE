package org.example.semantic;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final SymbolTable parent;
    private final Map<String, Symbol> symbols = new HashMap<>();

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public boolean define(Symbol symbol) {
        if(symbols.containsKey(symbol.name)) {
            return false;
        }
        symbols.put(symbol.name, symbol);
        return true;
    }

    public Symbol lookup(String name) {
        Symbol s = symbols.get(name);
        if(s != null) return s;
        if(parent != null) return parent.lookup(name);
        return null;
    }

    public SymbolTable getParent() {
        return parent;
    }

    public Map<String, Symbol> getSymbols() {
        return symbols;
    }
}
