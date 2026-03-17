package org.example.ast;

import org.example.semantic.SemanticVisitor;

public class BinaryOpNode extends AstNode {
    public AstNode left, right;
    public String op;

    public BinaryOpNode(AstNode l, String o, AstNode r) { left = l; op = o; right = r; }

    @Override
    public void print(String indent) {
        System.out.println(indent + "Op (" + op + ")");
        if (left != null) left.print(indent + "  ├── ");
        if (right != null) right.print(indent + "  └── ");
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) { return visitor.visit(this); }
}
