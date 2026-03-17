package org.example.ast;

import org.example.semantic.SemanticVisitor;

import java.util.ArrayList;
import java.util.List;

public class ForStatementNode extends AstNode {
    public String iteratorName;
    public AstNode iterable;
    public List<AstNode> body = new ArrayList<>();

    public ForStatementNode(String id, AstNode expr) {
        this.iteratorName = id;
        this.iterable = expr;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "FOR " + iteratorName + " IN");
        iterable.print(indent + "  ├── ");
        System.out.println(indent + "  └── BODY:");
        for (AstNode s : body) s.print(indent + "      ");
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) { return visitor.visit(this); }
}
