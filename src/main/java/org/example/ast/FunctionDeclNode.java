package org.example.ast;

import org.example.semantic.SemanticVisitor;

import java.util.ArrayList;
import java.util.List;

public class FunctionDeclNode extends AstNode {
    public String funcName;
    public String returnType;
    public List<AstNode> body = new ArrayList<>();

    public static class Parameter {
        public String name;
        public String type;
        public Parameter(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    public List <Parameter> parameters = new ArrayList<>();

    public FunctionDeclNode(String name, String returnType) {
        this.funcName = name;
        this.returnType = returnType;
    }

    @Override
    public void print(String indent) {
        StringBuilder paramsStr = new StringBuilder();
        for (Parameter p : parameters) {
            paramsStr.append(p.name).append(":").append(p.type).append(" ");
        }
        System.out.println(indent + "FUNCTION: " + funcName + "(" + paramsStr.toString().trim() + ") -> " + (returnType != null ? returnType : "Void"));
        for (AstNode stmt : body) stmt.print(indent + "  │ ");
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) { return visitor.visit(this); }
}
