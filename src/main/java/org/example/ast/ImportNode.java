package org.example.ast;

import org.example.semantic.SemanticVisitor;

public class ImportNode extends AstNode {
    public String modulePath;
    public ImportNode(String path) { this.modulePath = path; }
    @Override
    public void print(String indent) { System.out.println(indent + "IMPORT: " + modulePath); }
    @Override
    public <T> T accept(SemanticVisitor<T> visitor) { return visitor.visit(this); }
}
