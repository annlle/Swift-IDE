package org.example.semantic;

import org.example.ast.*;

public interface SemanticVisitor<T> {
    T visit(ProgramNode node);

    T visit(VariableDeclNode node);
    T visit(ClassDeclNode node);
    T visit(FunctionDeclNode node);
    T visit(InitDeclNode node);
    T visit(ImportNode node);

    T visit(BinaryOpNode node);
    T visit(LiteralNode node);
    T visit(PrintNode node);

    T visit(IfStatementNode node);
    T visit(ForStatementNode node);
    T visit(WhileStatementNode node);

    T visit(BreakNode breakNode);
    T visit(ContinueNode continueNode);

    T visit(ReturnNode returnNode);

    T visit(ArrayAccessNode node);

    T visit(CallNode callNode);

}