package org.example.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import org.example.SwiftParserBaseVisitor;
import org.example.SwiftParser;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

public class AstBuilder extends SwiftParserBaseVisitor<AstNode> {

    private <T extends AstNode> T setLoc(T node, ParserRuleContext ctx) {
        if (ctx != null && ctx.getStart() != null) {
            node.line = ctx.getStart().getLine();
            node.column = ctx.getStart().getCharPositionInLine();
        }
        return node;
    }

    @Override
    public AstNode visitProgram(SwiftParser.ProgramContext ctx) {
        ProgramNode program = setLoc(new ProgramNode(), ctx);
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
        return setLoc(new ImportNode(ctx.getText()), ctx);
    }

    @Override
    public AstNode visitVariableDecl(SwiftParser.VariableDeclContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        String type = (ctx.typeAnnotation() != null) ? ctx.typeAnnotation().type().getText() : null;
        AstNode value = (ctx.expression() != null) ? visit(ctx.expression()) : null;
        return setLoc(new VariableDeclNode("var", name, type, value), ctx);
    }

    @Override
    public AstNode visitConstantDecl(SwiftParser.ConstantDeclContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        String type = (ctx.typeAnnotation() != null) ? ctx.typeAnnotation().type().getText() : null;
        AstNode value = (ctx.expression() != null) ? visit(ctx.expression()) : null;
        return setLoc(new VariableDeclNode("let", name, type, value), ctx);
    }

    @Override
    public AstNode visitClassDecl(SwiftParser.ClassDeclContext ctx) {
        ClassDeclNode classNode = setLoc(new ClassDeclNode(ctx.IDENTIFIER().getText()), ctx);
        for (SwiftParser.DeclarationContext decl : ctx.declaration()) {
            AstNode node = visit(decl);
            if (node != null) classNode.members.add(node);
        }
        return classNode;
    }

    @Override
    public AstNode visitInitDecl(SwiftParser.InitDeclContext ctx) {
        InitDeclNode node = setLoc(new InitDeclNode(), ctx);
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

        if (ctx.parameterList() != null) {
            for (SwiftParser.ParameterContext pCtx : ctx.parameterList().parameter()) {
                String pName = pCtx.IDENTIFIER().getText();
                String pType = pCtx.type().getText();
                node.parameters.add(new FunctionDeclNode.Parameter(pName, pType));
            }
        }

        if (ctx.block() != null) {
            AstNode body = visit(ctx.block());
            if (body instanceof ProgramNode) {
                node.body.addAll(((ProgramNode) body).declarations);
            }
        }
        return setLoc(node, ctx);
    }

    @Override
    public AstNode visitIfStatement(SwiftParser.IfStatementContext ctx) {
        IfStatementNode node = setLoc(new IfStatementNode(), ctx);
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
        ForStatementNode node = setLoc(new ForStatementNode(ctx.IDENTIFIER().getText(), visit(ctx.expression())), ctx);
        node.body.add(visit(ctx.block()));
        return node;
    }

    @Override
    public AstNode visitWhileStatement(SwiftParser.WhileStatementContext ctx) {
        WhileStatementNode node = new WhileStatementNode();
        node.condition = visit(ctx.expression());

        AstNode bodyNode = visit(ctx.block());
        if (bodyNode instanceof ProgramNode) {
            node.body.addAll(((ProgramNode) bodyNode).declarations);
        }
        return setLoc(node, ctx);
    }

    @Override
    public AstNode visitAssignment(SwiftParser.AssignmentContext ctx) {
        if (ctx.getChildCount() == 1) return visit(ctx.logicOr());
        AstNode left = visit(ctx.logicOr());
        String op = ctx.getChild(1).getText();
        AstNode right = visit(ctx.assignment());
        return setLoc(new BinaryOpNode(left, op, right), ctx);
    }

    @Override
    public AstNode visitReturnStatement(SwiftParser.ReturnStatementContext ctx) {
        AstNode expr = null;
        if (ctx.expression() != null) {
            expr = visit(ctx.expression());
        }
        return setLoc(new ReturnNode(expr), ctx);
    }

    @Override
    public AstNode visitBreakStatement(SwiftParser.BreakStatementContext ctx) {
        return setLoc(new BreakNode(), ctx);
    }

    @Override
    public AstNode visitContinueStatement(SwiftParser.ContinueStatementContext ctx) {
        return setLoc(new ContinueNode(), ctx);
    }

    @Override
    public AstNode visitComparison(SwiftParser.ComparisonContext ctx) {
        if (ctx.getChildCount() == 1) return visit(ctx.term(0));
        AstNode left = visit(ctx.term(0));
        for (int i = 1; i < ctx.getChildCount(); i += 2) {
            String op = ctx.getChild(i).getText();
            AstNode right = visit(ctx.term((i + 1) / 2));
            left = setLoc(new BinaryOpNode(left, op, right), ctx);
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
            left = setLoc(new BinaryOpNode(left, op, right), ctx);
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
            left = setLoc(new BinaryOpNode(left, op, right), ctx);
        }
        return left;
    }

    @Override
    public AstNode visitPrimary(SwiftParser.PrimaryContext ctx) {
        if (ctx.INT() != null) return setLoc(new LiteralNode(ctx.INT().getText(), "INT"), ctx);
        if (ctx.DOUBLE() != null) return setLoc(new LiteralNode(ctx.DOUBLE().getText(), "DOUBLE"), ctx);
        if (ctx.STRING() != null) return setLoc(new LiteralNode(ctx.STRING().getText(), "STRING"), ctx);
        if (ctx.IDENTIFIER() != null) return setLoc(new LiteralNode(ctx.IDENTIFIER().getText(), "IDENTIFIER"), ctx);
        if (ctx.TRUE() != null || ctx.FALSE() != null) return setLoc(new LiteralNode(ctx.getText(), "BOOL"), ctx);
        if (ctx.arrayLiteral() != null) return setLoc(new LiteralNode(ctx.arrayLiteral().getText(), "ARRAY"), ctx);
        if (ctx.dictionaryLiteral() != null) return setLoc(new LiteralNode(ctx.dictionaryLiteral().getText(), "DICTIONARY"), ctx);
        if (ctx.SELF() != null) return setLoc(new LiteralNode("self", "SELF"), ctx);
        if (ctx.expression() != null) return visit(ctx.expression());

        return visitChildren(ctx);
    }

    @Override
    public AstNode visitCall(SwiftParser.CallContext ctx) {

        AstNode result = visit(ctx.primary());
        String name = ctx.primary().getText();

        for (int i = 1; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);

            if (child.getText().equals("(")) {

                List<AstNode> args = new ArrayList<>();

                if (ctx.argumentList() != null) {
                    for (SwiftParser.ArgumentListContext argListCtx : ctx.argumentList()) {
                        for (SwiftParser.ExpressionContext exprCtx : argListCtx.expression()) {
                            args.add(visit(exprCtx));
                        }
                    }
                }

                result = setLoc(new CallNode(name, args), ctx);
            }

            else if (child.getText().equals("[")) {

                AstNode index = visit(ctx.expression(0));
                result = setLoc(new ArrayAccessNode(result, index), ctx);
            }
        }

        return result;
    }

    @Override
    public AstNode visitBlock(SwiftParser.BlockContext ctx) {
        ProgramNode blockNode = setLoc(new ProgramNode(), ctx);
        for (SwiftParser.DeclarationContext decl : ctx.declaration()) {
            AstNode node = visit(decl);
            if (node != null) blockNode.declarations.add(node);
        }
        return blockNode;
    }

    @Override
    public AstNode visitPrintStatement(SwiftParser.PrintStatementContext ctx) {
        PrintNode node = setLoc(new PrintNode(), ctx);
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