package org.example.ast;

import org.example.semantic.SemanticVisitor;

public class ContinueNode extends AstNode{
    @Override
    public void print(String indent) {
        System.out.println(indent + "ContinueStatement");
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
