package org.example.ir;

import java.util.*;

public class IROptimizer {

    public static List<IRInstruction> optimize(List<IRInstruction> ir) {

        List<IRInstruction> result = new ArrayList<>();

        Map<String, String> copyMap = new HashMap<>();

        for (IRInstruction instr : ir) {

            String arg1 = replace(instr.getArg1(), copyMap);
            String arg2 = replace(instr.getArg2(), copyMap);

            if (isNumber(arg1) && isNumber(arg2)) {

                Integer value = compute(instr.getOp(), arg1, arg2);

                if (value != null) {
                    result.add(new IRInstruction("=", value.toString(), "", instr.getResult()));
                    copyMap.put(instr.getResult(), value.toString());
                    continue;
                }
            }

            if (instr.getOp().equals("=") && (arg2 == null || arg2.isEmpty())) {
                copyMap.put(instr.getResult(), arg1);
                result.add(new IRInstruction("=", arg1, "", instr.getResult()));
                continue;
            }

            result.add(new IRInstruction(instr.getOp(), arg1, arg2, instr.getResult()));
        }

        return removeUnusedTemps(result);
    }


    private static String replace(String arg, Map<String, String> map) {
        if (arg == null) return null;

        while (map.containsKey(arg)) {
            arg = map.get(arg);
        }
        return arg;
    }

    private static boolean isNumber(String s) {
        return s != null && s.matches("-?\\d+");
    }

    private static Integer compute(String op, String a, String b) {
        int x = Integer.parseInt(a);
        int y = Integer.parseInt(b);

        return switch (op) {
            case "+" -> x + y;
            case "-" -> x - y;
            case "*" -> x * y;
            case "/" -> (y != 0) ? x / y : null;
            default -> null;
        };
    }

    private static List<IRInstruction> removeUnusedTemps(List<IRInstruction> ir) {

        Set<String> used = new HashSet<>();

        for (IRInstruction instr : ir) {
            if (instr.getArg1() != null && instr.getArg1().startsWith("t")) {
                used.add(instr.getArg1());
            }
            if (instr.getArg2() != null && instr.getArg2().startsWith("t")) {
                used.add(instr.getArg2());
            }
        }

        List<IRInstruction> result = new ArrayList<>();

        for (IRInstruction instr : ir) {
            if (instr.getResult() != null && instr.getResult().startsWith("t") && !used.contains(instr.getResult())) {
                continue;
            }

            result.add(instr);
        }

        return result;
    }
}
