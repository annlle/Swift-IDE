package org.example.semantic;

public enum Type {
    INT, DOUBLE, STRING, BOOL, VOID, ARRAY, DICTIONARY, UNKNOWN;

    public static Type fromString(String typeStr) {
        if (typeStr == null) return UNKNOWN;
        typeStr = typeStr.trim();

        if (typeStr.startsWith("[") && typeStr.contains(":") && typeStr.endsWith("]")) {
            return DICTIONARY;
        }

        if (typeStr.startsWith("[") && typeStr.endsWith("]")) {
            return ARRAY;
        }

        return switch (typeStr) {
            case "Int" -> INT;
            case "Double" -> DOUBLE;
            case "String" -> STRING;
            case "Bool" -> BOOL;
            case "Void" -> VOID;
            default -> UNKNOWN;
        };
    }
}
