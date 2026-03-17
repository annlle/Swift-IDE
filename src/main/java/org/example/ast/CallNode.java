package org.example.ast;

import org.example.semantic.SemanticVisitor;
import java.util.List;

public class CallNode extends AstNode {
    public String funcName;
    public List<AstNode> args;

    public CallNode(String funcName, List<AstNode> args) {
        this.funcName = funcName;
        this.args = args;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "FunctionCall: " + funcName);
        for (AstNode arg : args) {
            arg.print(indent + "  [Arg] ");
        }
    }

    @Override
    public <T> T accept(SemanticVisitor<T> visitor) {
        return visitor.visit(this);
    }
}