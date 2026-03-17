package org.example.ir;

import org.example.ast.*;
import org.example.semantic.SemanticVisitor;

import java.util.*;

public class IRGenerator implements SemanticVisitor<String> {

    private final List<IRInstruction> instructions = new ArrayList<>();
    private int tempCounter = 0;

    public List<IRInstruction> getInstructions() {
        return instructions;
    }

    private String newTemp() {
        return "t" + (tempCounter++);
    }

    @Override
    public String visit(ProgramNode node) {
        for (AstNode n : node.declarations) {
            if (n != null) n.accept(this);
        }
        return null;
    }

    @Override
    public String visit(VariableDeclNode node) {
        if (node.initializer != null) {
            String value = node.initializer.accept(this);

            instructions.add(new IRInstruction(
                    node.name,
                    value,
                    null,
                    null
            ));

            return node.name;
        }
        return null;
    }

    @Override
    public String visit(BinaryOpNode node) {
        String left = node.left.accept(this);
        String right = node.right.accept(this);

        String temp = newTemp();

        instructions.add(new IRInstruction(
                temp,
                left,
                node.op,
                right
        ));

        return temp;
    }

    @Override
    public String visit(LiteralNode node) {
        return node.value;
    }

    @Override
    public String visit(PrintNode node) {
        for (AstNode arg : node.args) {
            arg.accept(this);
        }
        return null;
    }

    @Override
    public String visit(IfStatementNode node) {
        node.condition.accept(this);
        for (AstNode s : node.thenBranch) s.accept(this);
        if (node.elseBranch != null) {
            for (AstNode s : node.elseBranch) s.accept(this);
        }
        return null;
    }

    @Override
    public String visit(WhileStatementNode node) {
        node.condition.accept(this);
        for (AstNode s : node.body) s.accept(this);
        return null;
    }

    @Override
    public String visit(ForStatementNode node) {
        node.iterable.accept(this);
        for (AstNode s : node.body) s.accept(this);
        return null;
    }

    @Override public String visit(FunctionDeclNode node) { return null; }
    @Override public String visit(ClassDeclNode node) { return null; }
    @Override public String visit(InitDeclNode node) { return null; }
    @Override public String visit(ImportNode node) { return null; }
    @Override public String visit(ReturnNode node) { return null; }
    @Override public String visit(BreakNode node) { return null; }
    @Override public String visit(ContinueNode node) { return null; }
    @Override public String visit(ArrayAccessNode node) { return null; }
    @Override public String visit(CallNode node) { return null; }
}