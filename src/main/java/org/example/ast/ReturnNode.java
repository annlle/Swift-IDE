package org.example.ast;
import org.example.semantic.SemanticVisitor;

public class ReturnNode extends AstNode {
    public AstNode expression;

    public ReturnNode(AstNode expression) {
        this.expression = expression;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "ReturnStatement");
        if (expression != null) {
            expression.print(indent + "  ");
        }
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
