package org.example.ast;

import org.example.semantic.SemanticVisitor;

import java.util.ArrayList;
import java.util.List;

public class ClassDeclNode extends AstNode {
    public String className;
    public List<AstNode> members = new ArrayList<>();

    public ClassDeclNode(String name) { this.className = name; }

    @Override
    public void print(String indent) {
        System.out.println(indent + "CLASS: " + className);
        for (AstNode member : members) member.print(indent + "  │ ");
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) { return visitor.visit(this); }
}
