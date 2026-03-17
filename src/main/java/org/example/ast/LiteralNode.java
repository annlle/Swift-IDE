package org.example.ast;

import org.example.semantic.SemanticVisitor;

public class LiteralNode extends AstNode {
    public String value;
    public String literalType;

    public LiteralNode(String value, String type) {
        this.value = value;
        this.literalType = type;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + literalType + ": " + value);
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) { return visitor.visit(this); }
}
