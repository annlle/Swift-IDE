package org.example.ast;

import org.example.semantic.SemanticVisitor;

import java.util.ArrayList;
import java.util.List;

public class IfStatementNode extends AstNode {
    public AstNode condition;
    public List<AstNode> thenBranch = new ArrayList<>();
    public List<AstNode> elseBranch = new ArrayList<>();

    @Override
    public void print(String indent) {
        System.out.println(indent + "IF");
        condition.print(indent + "  ├── Cond: ");
        System.out.println(indent + "  ├── THEN:");
        for (AstNode s : thenBranch) s.print(indent + "  │   ");
        if (!elseBranch.isEmpty()) {
            System.out.println(indent + "  └── ELSE:");
            for (AstNode s : elseBranch) s.print(indent + "      ");
        }
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) { return visitor.visit(this); }
}
