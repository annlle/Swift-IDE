package org.example.ast;

import java.util.ArrayList;
import java.util.List;

public abstract class AstNode {
    public abstract void print(String indent);
}

class ProgramNode extends AstNode {
    public List<AstNode> declarations = new ArrayList<>();

    @Override
    public void print(String indent) {
        System.out.println(indent + "Program");
        for (AstNode node : declarations) {
            if (node != null) node.print(indent + "  ");
        }
    }
}

class VariableDeclNode extends AstNode {
    public String kind;
    public String name;
    public String type;
    public AstNode initializer;

    public VariableDeclNode(String kind, String name, String type, AstNode initializer) {
        this.kind = kind;
        this.name = name;
        this.type = type;
        this.initializer = initializer;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + kind.toUpperCase() + " Declaration: " + name + (type != null ? " (" + type + ")" : ""));
        if (initializer != null) {
            initializer.print(indent + "  └── init: ");
        }
    }
}

class ClassDeclNode extends AstNode {
    public String className;
    public List<AstNode> members = new ArrayList<>();

    public ClassDeclNode(String name) { this.className = name; }

    @Override
    public void print(String indent) {
        System.out.println(indent + "CLASS: " + className);
        for (AstNode member : members) {
            member.print(indent + "  │ ");
        }
    }
}

class FunctionDeclNode extends AstNode {
    public String funcName;
    public String returnType;
    public List<AstNode> body = new ArrayList<>();

    public FunctionDeclNode(String name, String returnType) {
        this.funcName = name;
        this.returnType = returnType;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "FUNCTION: " + funcName + " -> " + (returnType != null ? returnType : "Void"));
        for (AstNode stmt : body) {
            stmt.print(indent + "  │ ");
        }
    }
}

class BinaryOpNode extends AstNode {
    public AstNode left, right;
    public String op;

    public BinaryOpNode(AstNode l, String o, AstNode r) { left = l; op = o; right = r; }

    @Override
    public void print(String indent) {
        System.out.println(indent + "Op (" + op + ")");

        if (left != null) left.print(indent + "  ├── ");
        else System.out.println(indent + "  ├── [NULL LEFT]");

        if (right != null) right.print(indent + "  └── ");
        else System.out.println(indent + "  └── [NULL RIGHT]");
    }
}

class IfStatementNode extends AstNode {
    public AstNode condition;
    public List<AstNode> thenBranch = new ArrayList<>();
    public List<AstNode> elseBranch = new ArrayList<>();

    @Override
    public void print(String indent) {
        System.out.println(indent + "IF");
        condition.print(indent + "  ├── Cond: ");
        System.out.println(indent + "  ├── THEN:");
        for (AstNode s : thenBranch) s.print(indent + "  │   ");
        if (!elseBranch.isEmpty()) {
            System.out.println(indent + "  └── ELSE:");
            for (AstNode s : elseBranch) s.print(indent + "      ");
        }
    }
}

class ForStatementNode extends AstNode {
    public String iteratorName;
    public AstNode iterable;
    public List<AstNode> body = new ArrayList<>();

    public ForStatementNode(String id, AstNode expr) {
        this.iteratorName = id;
        this.iterable = expr;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "FOR " + iteratorName + " IN");
        iterable.print(indent + "  ├── ");
        System.out.println(indent + "  └── BODY:");
        for (AstNode s : body) s.print(indent + "      ");
    }
}

class LiteralNode extends AstNode {
    public String value;
    public String type;

    public LiteralNode(String value, String type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + type + ": " + value);
    }
}

class ImportNode extends AstNode {
    public String modulePath;
    public ImportNode(String path) { this.modulePath = path; }
    @Override
    public void print(String indent) {
        System.out.println(indent + "IMPORT: " + modulePath);
    }
}

class InitDeclNode extends AstNode {
    public List<AstNode> body = new ArrayList<>();
    @Override
    public void print(String indent) {
        System.out.println(indent + "INITIALIZER (init)");
        for (AstNode stmt : body) stmt.print(indent + "  │ ");
    }
}

class PrintNode extends AstNode {
    public List<AstNode> args = new ArrayList<>();
    @Override
    public void print(String indent) {
        System.out.println(indent + "PRINT CALL");
        for (AstNode arg : args) arg.print(indent + "  └── ");
    }
}