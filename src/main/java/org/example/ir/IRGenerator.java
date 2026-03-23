package org.example.ir;
import org.example.ast.*;
import org.example.semantic.SemanticVisitor;

import java.util.ArrayList;
import java.util.List;

public class IRGenerator implements SemanticVisitor<String> {

    private final List<IRInstruction> instructions = new ArrayList<>();
    private final TempVarGenerator tempGen = new TempVarGenerator();

    public List<IRInstruction> generate(AstNode root) {
        root.accept(this);
        return instructions;
    }

    @Override
    public String visit(ProgramNode node) {
        for (AstNode child : node.declarations) {
            if (child != null) child.accept(this);
        }
        return null;
    }

    @Override
    public String visit(LiteralNode node) {
        return node.value;
    }

    @Override
    public String visit(VariableDeclNode node) {
        if (node.initializer != null) {
            String value = node.initializer.accept(this);
            // ФОРМАТ: (ОП, АРГ1, АРГ2, РЕЗ)
            instructions.add(new IRInstruction("=", value, "", node.name));
        }
        return node.name;
    }

    @Override
    public String visit(BinaryOpNode node) {
        String left = node.left.accept(this);
        String right = node.right.accept(this);
        String temp = tempGen.next();
        instructions.add(new IRInstruction(node.op, left, right, temp));
        return temp;
    }

    @Override
    public String visit(PrintNode node) {
        for (AstNode arg : node.args) {
            String val = arg.accept(this);

            instructions.add(new IRInstruction("print", val, "", ""));
        }
        return null;
    }

    @Override
    public String visit(IfStatementNode node) {
        String cond = node.condition.accept(this);
        String labelElse = "L_else_" + tempGen.next();
        String labelEnd = "L_end_" + tempGen.next();

        instructions.add(new IRInstruction("ifFalse", cond, "", labelElse));
        for (AstNode stmt : node.thenBranch) stmt.accept(this);
        instructions.add(new IRInstruction("goto", "", "", labelEnd));

        instructions.add(new IRInstruction("label", "", "", labelElse));
        for (AstNode stmt : node.elseBranch) stmt.accept(this);

        instructions.add(new IRInstruction("label", "", "", labelEnd));
        return null;
    }

    @Override
    public String visit(WhileStatementNode node) {
        String start = "L_start_" + tempGen.next();
        String end = "L_end_" + tempGen.next();

        instructions.add(new IRInstruction("label", "", "", start));
        String cond = node.condition.accept(this);
        instructions.add(new IRInstruction("ifFalse", cond, "", end));

        for (AstNode stmt : node.body) stmt.accept(this);

        instructions.add(new IRInstruction("goto", "", "", start));
        instructions.add(new IRInstruction("label", "", "", end));
        return null;
    }

    @Override
    public String visit(ReturnNode node) {
        if (node.expression != null) {
            String val = node.expression.accept(this);
            instructions.add(new IRInstruction("return", val, "", ""));
        }
        return null;
    }

    @Override
    public String visit(FunctionDeclNode node) {
        instructions.add(new IRInstruction("func", "", "", node.funcName));
        for (AstNode stmt : node.body) stmt.accept(this);
        instructions.add(new IRInstruction("endfunc", "", "", node.funcName));
        return null;
    }

    @Override
    public String visit(CallNode node) {
        for (AstNode arg : node.args) {
            String val = arg.accept(this);
            instructions.add(new IRInstruction("param", val, "", ""));
        }
        String temp = tempGen.next();
        instructions.add(new IRInstruction("call", node.funcName, "", temp));
        return temp;
    }

    @Override public String visit(ClassDeclNode node) { return null; }
    @Override public String visit(ImportNode node) { return null; }
    @Override public String visit(InitDeclNode node) { return null; }
    @Override public String visit(ForStatementNode node) { return null; }
    @Override public String visit(ArrayAccessNode node) { return null; }
    @Override public String visit(BreakNode node) { return null; }
    @Override public String visit(ContinueNode node) { return null; }
}