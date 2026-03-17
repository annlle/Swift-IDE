package org.example.ast;

import org.example.semantic.SemanticVisitor;

import java.util.ArrayList;
import java.util.List;

public class ProgramNode extends AstNode {
    public List<AstNode> declarations = new ArrayList<>();

    @Override
    public void print(String indent) {
        System.out.println(indent + "Program");
        for (AstNode node : declarations) {
            if (node != null) node.print(indent + "  ");
        }
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) { return visitor.visit(this); }
}
