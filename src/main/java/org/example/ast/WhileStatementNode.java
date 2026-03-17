package org.example.ast;

import org.example.semantic.SemanticVisitor;

import java.util.ArrayList;
import java.util.List;

public class WhileStatementNode extends AstNode{
    public AstNode condition;
    public List<AstNode> body = new ArrayList<>();

    @Override
    public void print(String indent) {
        System.out.println(indent + "WhileStatement");
        condition.print(indent+ " (cond) ");
        for (AstNode node : body) node.print(indent + " ");
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
