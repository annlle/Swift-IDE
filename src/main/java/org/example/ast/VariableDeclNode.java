package org.example.ast;

import org.example.semantic.SemanticVisitor;

public class VariableDeclNode extends AstNode {
    public String kind;
    public String name;
    public String type;
    public AstNode initializer;

    public VariableDeclNode(String kind, String name, String type, AstNode initializer) {
        this.kind = kind;
        this.name = name;
        this.type = type;
        this.initializer = initializer;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + kind.toUpperCase() + " Declaration: " + name + (type != null ? " (" + type + ")" : ""));
        if (initializer != null) initializer.print(indent + "  └── init: ");
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) { return visitor.visit(this); }
}
