package org.example.ast;

import org.example.semantic.SemanticVisitor;

public abstract class AstNode {
    public int line;
    public int column;

    public abstract void print(String indent);

    public abstract <T> T accept(SemanticVisitor<T> visitor);
}