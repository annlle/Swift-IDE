package org.example.ast;

import org.example.semantic.SemanticVisitor;

import java.util.ArrayList;
import java.util.List;

public class PrintNode extends AstNode {
    public List<AstNode> args = new ArrayList<>();
    @Override
    public void print(String indent) {
        System.out.println(indent + "PRINT CALL");
        for (AstNode arg : args) arg.print(indent + "  └── ");
    }
    @Override
    public <T> T accept(SemanticVisitor<T> visitor) { return visitor.visit(this); }
}
