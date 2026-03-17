package org.example.analyzer;

import org.example.ast.*;
import org.example.semantic.SemanticVisitor;
import org.example.semantic.Symbol;
import org.example.semantic.SymbolTable;
import org.example.semantic.Type;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer implements SemanticVisitor<Type> {
    private SymbolTable currentScope;
    private final SymbolTable globalScope;
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private int loopDepth = 0;
    private Type currentFunctionReturnType = null;

    public SemanticAnalyzer() {
        this.globalScope = new SymbolTable(null);
        this.currentScope = globalScope;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    private void reportError(AstNode node, String message) {
        errors.add(String.format("Line %d:%d - ERROR: %s", node.line, node.column, message));
    }

    private void reportWarning(AstNode node, String message) {
        warnings.add(String.format("Line %d:%d - WARNING: %s", node.line, node.column, message));
    }

    private boolean isNumeric(Type type) {
        return type == Type.INT || type == Type.DOUBLE;
    }

    public void finalizeAnalysis() {
        checkUnused(globalScope);
    }

    private void checkUnused(SymbolTable table) {
        for (Symbol s : table.getSymbols().values()) {
            if (!s.isUsed) {
                warnings.add(String.format("Line %d:%d - WARNING: Variable '%s' is declared but never used.",
                        s.line, s.column, s.name));
            }
        }
    }

    @Override
    public Type visit(ProgramNode node) {
        for (AstNode decl : node.declarations) {
            if (decl != null) decl.accept(this);
        }
        finalizeAnalysis();
        return Type.VOID;
    }

    @Override
    public Type visit(VariableDeclNode node) {
        Type declaredType = Type.fromString(node.type);
        Symbol symbol = new Symbol(node.name, declaredType, false, node.line, node.column);

        if (node.initializer != null) {
            Type initType = node.initializer.accept(this);
            if (declaredType == Type.UNKNOWN) {
                declaredType = initType;
                symbol.type = initType;
            } else if (initType != Type.UNKNOWN && declaredType != initType) {
                if (!(declaredType == Type.DOUBLE && initType == Type.INT)) {
                    reportError(node, "Types do not match: " + declaredType + " and " + initType);
                }
            }
            symbol.isUsed = true;
        }

        if (!currentScope.define(symbol)) {
            reportError(node, "Identifier '" + node.name + "' has already been declared in this scope.");
        }

        return declaredType;
    }

    @Override
    public Type visit(BinaryOpNode node) {
        Type left = node.left.accept(this);
        Type right = node.right.accept(this);

        if (left == Type.UNKNOWN || right == Type.UNKNOWN) return Type.UNKNOWN;

        if (left == Type.ARRAY || left == Type.DICTIONARY) {
            if (!"+".equals(node.op)) {
                reportError(node, "Operation '" + node.op + "' is not supported for collections.");
            }
            if (left != right) {
                reportError(node, "Unable to perform operation between " + left + " and " + right);
            }
            return left;
        }

        boolean areNumeric = isNumeric(left) && isNumeric(right);

        if ("+".equals(node.op) || "-".equals(node.op) || "*".equals(node.op) || "/".equals(node.op)) {
            if (left == Type.INT && right == Type.INT) return Type.INT;
            if (areNumeric) return Type.DOUBLE;

            reportError(node, "Operation '" + node.op + "' is not supported for types " + left + " and " + right);
            return Type.UNKNOWN;
        }

        if (">".equals(node.op) || "<".equals(node.op) || "==".equals(node.op) || "!=".equals(node.op) || ">=".equals(node.op) || "<=".equals(node.op)) {
            if (left != right && !areNumeric) {
                reportError(node, "Cannot compare different types: " + left + " and " + right);
            }
            return Type.BOOL;
        }

        return Type.UNKNOWN;
    }

    @Override
    public Type visit(LiteralNode node) {
        if ("IDENTIFIER".equals(node.literalType)) {
            Symbol s = currentScope.lookup(node.value);
            if (s == null) {
                reportError(node, "Use '" + node.value + "' without declare.");
                return Type.UNKNOWN;
            }
            s.isUsed = true;
            return s.type;
        }

        if ("INT".equals(node.literalType) || "Double".equals(node.literalType) || "String".equals(node.literalType) || "Bool".equals(node.literalType)) {
            return Type.fromString(node.literalType);
        }

        try {
            Double.parseDouble(node.value);
            if (node.value.contains(".")) return Type.DOUBLE;
            return Type.INT;
        } catch (NumberFormatException e) {
            if ("true".equals(node.value) || "false".equals(node.value)) return Type.BOOL;
            if (node.value.startsWith("\"")) return Type.STRING;
        }

        return Type.fromString(node.literalType);
    }

    @Override
    public Type visit(IfStatementNode node) {
        Type condType = node.condition.accept(this);
        if (condType != Type.BOOL && condType != Type.UNKNOWN) {
            reportError(node, "Condition IF must be BOOL, not " + condType);
        }

        if (node.condition instanceof LiteralNode lit) {
            if ("true".equals(lit.value)) {
                reportWarning(node, "The ELSE branch will never be executed (condition is always true).");
            } else if ("false".equals(lit.value)) {
                reportWarning(node, "The IF branch will never be executed (condition is always false).");
            }
        }

        currentScope = new SymbolTable(currentScope);
        for (AstNode s : node.thenBranch) if (s != null) s.accept(this);
        currentScope = currentScope.getParent();

        if (node.elseBranch != null && !node.elseBranch.isEmpty()) {
            currentScope = new SymbolTable(currentScope);
            for (AstNode s : node.elseBranch) if (s != null) s.accept(this);
            currentScope = currentScope.getParent();
        }
        return Type.VOID;
    }

    @Override
    public Type visit(ForStatementNode node) {
        loopDepth++;
        currentScope = new SymbolTable(currentScope);

        currentScope.define(new Symbol(node.iteratorName, Type.INT, true, node.line, node.column));
        Type iterableType = node.iterable.accept(this);

        if (iterableType != Type.ARRAY && iterableType != Type.DICTIONARY && iterableType != Type.UNKNOWN && iterableType != Type.STRING) {
            reportError(node, "Type " + iterableType + " is not a sequence.");
        }

        for (AstNode s : node.body) if (s != null) s.accept(this);

        currentScope = currentScope.getParent();
        loopDepth--;
        return Type.VOID;
    }

    @Override
    public Type visit(WhileStatementNode node) {
        Type condType = node.condition.accept(this);
        if (condType != Type.BOOL && condType != Type.UNKNOWN) {
            reportError(node, "The WHILE loop condition must be BOOL type, not " + condType);
        }

        if (node.condition instanceof LiteralNode lit && "true".equals(lit.value)) {
            reportWarning(node, "Infinite loop detected (while true).");
        }

        loopDepth++;
        currentScope = new SymbolTable(currentScope);
        for (AstNode statement : node.body) if (statement != null) statement.accept(this);
        currentScope = currentScope.getParent();
        loopDepth--;

        return Type.VOID;
    }

    @Override
    public Type visit(FunctionDeclNode node) {
        Type retType = Type.fromString(node.returnType);
        Symbol funcSymbol = new Symbol(node.funcName, retType, true, node.line, node.column);
        funcSymbol.returnType = retType;

        if (node.parameters != null) {
            for (var param : node.parameters) {
                funcSymbol.paramTypes.add(Type.fromString(param.type));
            }
        }

        if (!currentScope.define(funcSymbol)) {
            reportError(node, "Function '" + node.funcName + "' already declared.");
        }

        SymbolTable functionScope = new SymbolTable(currentScope);
        if (node.parameters != null) {
            for (var param : node.parameters) {
                Type pType = Type.fromString(param.type);
                functionScope.define(new Symbol(param.name, pType, true, node.line, node.column));
            }
        }

        Type prevRetType = currentFunctionReturnType;
        currentFunctionReturnType = retType;
        SymbolTable previousScope = currentScope;
        currentScope = functionScope;

        for (AstNode s : node.body) if (s != null) s.accept(this);

        currentScope = previousScope;
        currentFunctionReturnType = prevRetType;

        return retType;
    }

    @Override
    public Type visit(ReturnNode node) {
        Type actualType = (node.expression != null) ? node.expression.accept(this) : Type.VOID;

        if (currentFunctionReturnType == null) {
            reportError(node, "Return statement outside of function.");
        } else {
            if (currentFunctionReturnType == Type.VOID && node.expression != null) {
                reportError(node, "Void function should not return a value.");
            } else if (currentFunctionReturnType != Type.VOID) {
                if (actualType != currentFunctionReturnType && !(currentFunctionReturnType == Type.DOUBLE && actualType == Type.INT)) {
                    reportError(node, "Return type mismatch: expected " + currentFunctionReturnType + " but got " + actualType);
                }
            }
        }
        return actualType;
    }

    @Override
    public Type visit(BreakNode node) {
        if (loopDepth <= 0) reportError(node, "Operator 'break' is only allowed inside a loop.");
        return Type.VOID;
    }

    @Override
    public Type visit(ContinueNode node) {
        if (loopDepth <= 0) reportError(node, "Operator 'continue' is only allowed inside a loop.");
        return Type.VOID;
    }

    @Override
    public Type visit(PrintNode node) {
        if (node.args != null) {
            for (AstNode arg : node.args) if (arg != null) arg.accept(this);
        }
        return Type.VOID;
    }

    @Override
    public Type visit(ClassDeclNode node) {
        if (!currentScope.define(new Symbol(node.className, Type.UNKNOWN, true, node.line, node.column))) {
            reportError(node, "Class '" + node.className + "' already declared.");
        }
        currentScope = new SymbolTable(currentScope);
        for (AstNode member : node.members) if (member != null) member.accept(this);
        currentScope = currentScope.getParent();
        return Type.VOID;
    }

    @Override
    public Type visit(ImportNode node) {
        return Type.VOID;
    }

    @Override
    public Type visit(InitDeclNode node) {
        for (AstNode s : node.body) if (s != null) s.accept(this);
        return Type.VOID;
    }

    @Override
    public Type visit(ArrayAccessNode node) {
        Type arrayType = node.array.accept(this);
        Type indexType = node.index.accept(this);

        if (arrayType != Type.ARRAY && arrayType != Type.UNKNOWN && arrayType != Type.STRING) {
            reportError(node, "Type " + arrayType + " does not support indexing.");
        }

        if (indexType != Type.INT && indexType != Type.UNKNOWN) {
            reportError(node, "Index must be INT, not " + indexType);
        }

        return Type.UNKNOWN;
    }

    @Override
    public Type visit(CallNode node) {
        Symbol func = currentScope.lookup(node.funcName);

        if (func == null) {
            reportError(node, "Function '" + node.funcName + "' not found.");
            return Type.UNKNOWN;
        }

        if (node.args.size() != func.paramTypes.size()) {
            reportError(node, "Argument count mismatch for '" + node.funcName + "'. Expected: " + func.paramTypes.size() + ", but got: " + node.args.size());
            return func.type;
        }

        for (int i = 0; i < node.args.size(); i++) {
            Type actualType = node.args.get(i).accept(this);
            Type expectedType = func.paramTypes.get(i);

            if (actualType != expectedType && actualType != Type.UNKNOWN) {
                if (!(expectedType == Type.DOUBLE && actualType == Type.INT)) {
                    reportError(node, "Argument " + (i + 1) + " type mismatch for '" + node.funcName + "'. Expected: " + expectedType + ", but got: " + actualType);
                }
            }
        }

        return func.type;
    }
}