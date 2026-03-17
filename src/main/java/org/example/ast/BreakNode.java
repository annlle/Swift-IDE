package org.example.ast;

import org.example.semantic.SemanticVisitor;

public class BreakNode extends AstNode {
    @Override
    public void print(String indent) {
        System.out.println(indent + "BreakStatement");
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
