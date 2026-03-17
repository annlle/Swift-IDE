package org.example.ast;

import org.example.semantic.SemanticVisitor;

import java.util.ArrayList;
import java.util.List;

public class InitDeclNode extends AstNode {
    public List<AstNode> body = new ArrayList<>();
    @Override
    public void print(String indent) {
        System.out.println(indent + "INITIALIZER (init)");
        for (AstNode stmt : body) stmt.print(indent + "  │ ");
    }
    @Override
    public <T> T accept(SemanticVisitor<T> visitor) { return visitor.visit(this); }
}
