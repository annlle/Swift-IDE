package org.example.ast;

import org.example.semantic.SemanticVisitor;

public class ArrayAccessNode extends AstNode {
    public AstNode array;
    public AstNode index;

    public ArrayAccessNode(AstNode array, AstNode index) {
        this.array = array;
        this.index = index;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "ArrayAccess");
        array.print(indent + "  [Target] ");
        index.print(indent + "  [Index] ");
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) {
        return visitor.visit(this);
    }
}