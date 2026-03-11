package org.example.ast;

import org.example.SwiftParserBaseVisitor;
import org.example.SwiftParser;
import org.antlr.v4.runtime.tree.ParseTree;

public class AstBuilder extends SwiftParserBaseVisitor<AstNode> {

    @Override
    public AstNode visitProgram(SwiftParser.ProgramContext ctx) {
        ProgramNode program = new ProgramNode();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (!(child instanceof org.antlr.v4.runtime.tree.TerminalNode)) {
                AstNode node = visit(child);
                if (node != null) program.declarations.add(node);
            }
        }
        return program;
    }

    @Override
    public AstNode visitImportDecl(SwiftParser.ImportDeclContext ctx) {
        return new ImportNode(ctx.getText());
    }

    @Override
    public AstNode visitVariableDecl(SwiftParser.VariableDeclContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        String type = (ctx.typeAnnotation() != null) ? ctx.typeAnnotation().type().getText() : null;
        AstNode value = (ctx.expression() != null) ? visit(ctx.expression()) : null;
        return new VariableDeclNode("var", name, type, value);
    }

    @Override
    public AstNode visitConstantDecl(SwiftParser.ConstantDeclContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        String type = (ctx.typeAnnotation() != null) ? ctx.typeAnnotation().type().getText() : null;
        AstNode value = (ctx.expression() != null) ? visit(ctx.expression()) : null;
        return new VariableDeclNode("let", name, type, value);
    }

    @Override
    public AstNode visitClassDecl(SwiftParser.ClassDeclContext ctx) {
        ClassDeclNode classNode = new ClassDeclNode(ctx.IDENTIFIER().getText());
        for (SwiftParser.DeclarationContext decl : ctx.declaration()) {
            AstNode node = visit(decl);
            if (node != null) classNode.members.add(node);
        }
        return classNode;
    }

    @Override
    public AstNode visitInitDecl(SwiftParser.InitDeclContext ctx) {
        InitDeclNode node = new InitDeclNode();
        if (ctx.block() != null) {
            AstNode body = visit(ctx.block());
            if (body instanceof ProgramNode) {
                node.body.addAll(((ProgramNode) body).declarations);
            }
        }
        return node;
    }

    @Override
    public AstNode visitFunctionDecl(SwiftParser.FunctionDeclContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        String retType = (ctx.returnType() != null) ? ctx.returnType().type().getText() : "Void";
        FunctionDeclNode node = new FunctionDeclNode(name, retType);
        if (ctx.block() != null) {
            AstNode body = visit(ctx.block());
            if (body instanceof ProgramNode) {
                node.body.addAll(((ProgramNode) body).declarations);
            }
        }
        return node;
    }

    @Override
    public AstNode visitIfStatement(SwiftParser.IfStatementContext ctx) {
        IfStatementNode node = new IfStatementNode();
        node.condition = visit(ctx.expression());

        if (ctx.block(0) != null) {
            node.thenBranch.add(visit(ctx.block(0)));
        }

        if (ctx.ELSE() != null) {
            if (!ctx.block().isEmpty() && ctx.block().size() > 1) {
                node.elseBranch.add(visit(ctx.block(1)));
            } else if (ctx.ifStatement() != null) {
                node.elseBranch.add(visit(ctx.ifStatement()));
            }
        }
        return node;
    }

    @Override
    public AstNode visitForStatement(SwiftParser.ForStatementContext ctx) {
        ForStatementNode node = new ForStatementNode(ctx.IDENTIFIER().getText(), visit(ctx.expression()));
        node.body.add(visit(ctx.block()));
        return node;
    }

    @Override
    public AstNode visitWhileStatement(SwiftParser.WhileStatementContext ctx) {
        // Додаємо підтримку While (якщо у вас є WhileStatementNode)
        // Якщо немає, можна створити спеціальний вузол або використати BinaryOp для умови
        AstNode condition = visit(ctx.expression());
        AstNode body = visit(ctx.block());
        return new BinaryOpNode(condition, "WHILE_LOOP", body);
        // Порада: Краще створити окремий WhileStatementNode у вашому проекті
    }

    @Override
    public AstNode visitAssignment(SwiftParser.AssignmentContext ctx) {
        if (ctx.getChildCount() == 1) return visit(ctx.logicOr());
        AstNode left = visit(ctx.logicOr());
        String op = ctx.getChild(1).getText();
        AstNode right = visit(ctx.assignment());
        return new BinaryOpNode(left, op, right);
    }

    @Override
    public AstNode visitComparison(SwiftParser.ComparisonContext ctx) {
        if (ctx.getChildCount() == 1) return visit(ctx.term(0));
        AstNode left = visit(ctx.term(0));
        for (int i = 1; i < ctx.getChildCount(); i += 2) {
            String op = ctx.getChild(i).getText();
            AstNode right = visit(ctx.term((i + 1) / 2));
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    @Override
    public AstNode visitTerm(SwiftParser.TermContext ctx) {
        if (ctx.getChildCount() == 1) return visit(ctx.factor(0));
        AstNode left = visit(ctx.factor(0));
        for (int i = 1; i < ctx.getChildCount(); i += 2) {
            String op = ctx.getChild(i).getText();
            AstNode right = visit(ctx.factor((i + 1) / 2));
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    @Override
    public AstNode visitFactor(SwiftParser.FactorContext ctx) {
        if (ctx.getChildCount() == 1) return visit(ctx.unary(0));
        AstNode left = visit(ctx.unary(0));
        for (int i = 1; i < ctx.getChildCount(); i += 2) {
            String op = ctx.getChild(i).getText();
            AstNode right = visit(ctx.unary((i + 1) / 2));
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    @Override
    public AstNode visitPrimary(SwiftParser.PrimaryContext ctx) {
        if (ctx.INT() != null) return new LiteralNode(ctx.INT().getText(), "INT");
        if (ctx.DOUBLE() != null) return new LiteralNode(ctx.DOUBLE().getText(), "DOUBLE");
        if (ctx.STRING() != null) return new LiteralNode(ctx.STRING().getText(), "STRING");
        if (ctx.IDENTIFIER() != null) return new LiteralNode(ctx.IDENTIFIER().getText(), "IDENTIFIER");
        if (ctx.TRUE() != null || ctx.FALSE() != null) return new LiteralNode(ctx.getText(), "BOOL");
        if (ctx.arrayLiteral() != null) return new LiteralNode(ctx.arrayLiteral().getText(), "ARRAY");
        if (ctx.dictionaryLiteral() != null) return new LiteralNode(ctx.dictionaryLiteral().getText(), "DICTIONARY");
        if (ctx.SELF() != null) return new LiteralNode("self", "SELF");
        if (ctx.expression() != null) return visit(ctx.expression());

        return visitChildren(ctx);
    }

    @Override
    public AstNode visitCall(SwiftParser.CallContext ctx) {
        AstNode base = visit(ctx.primary());
        // Обробка викликів методів через крапку (self.name)
        if (ctx.IDENTIFIER() != null && !ctx.IDENTIFIER().isEmpty()) {
            StringBuilder accessPath = new StringBuilder();
            if (base instanceof LiteralNode) accessPath.append(((LiteralNode) base).value);
            for (var id : ctx.IDENTIFIER()) {
                accessPath.append(".").append(id.getText());
            }
            return new LiteralNode(accessPath.toString(), "IDENTIFIER_ACCESS");
        }
        return base;
    }

    @Override
    public AstNode visitBlock(SwiftParser.BlockContext ctx) {
        ProgramNode blockNode = new ProgramNode();
        for (SwiftParser.DeclarationContext decl : ctx.declaration()) {
            AstNode node = visit(decl);
            if (node != null) blockNode.declarations.add(node);
        }
        return blockNode;
    }

    @Override
    public AstNode visitPrintStatement(SwiftParser.PrintStatementContext ctx) {
        PrintNode node = new PrintNode();
        if (ctx.argumentList() != null) {
            for (SwiftParser.ExpressionContext e : ctx.argumentList().expression()) {
                node.args.add(visit(e));
            }
        }
        return node;
    }

    @Override public AstNode visitDeclaration(SwiftParser.DeclarationContext ctx) { return visitChildren(ctx); }
    @Override public AstNode visitStatement(SwiftParser.StatementContext ctx) { return visitChildren(ctx); }
    @Override public AstNode visitExpression(SwiftParser.ExpressionContext ctx) { return visitChildren(ctx); }
}