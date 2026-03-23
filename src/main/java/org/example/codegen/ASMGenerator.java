package org.example.codegen;

import org.example.ir.IRInstruction;
import java.util.*;

public class ASMGenerator {

    public static String generate(List<IRInstruction> ir) {
        StringBuilder asm = new StringBuilder();
        Map<String, String> variables = new HashMap<>();
        int varCounter = 0;

        for (IRInstruction instr : ir) {
            String res = instr.getResult();
            if (res != null && !res.isEmpty() && !isNumber(res)) {
                if (!variables.containsKey(res)) {
                    variables.put(res, "var" + varCounter++);
                }
            }
        }

        asm.append(".section .data\n");
        asm.append("    fmt: .asciz \"%d\\n\"\n");
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            asm.append("    ").append(entry.getValue()).append(": .quad 0\n");
        }

        asm.append("\n.section .text\n");
        asm.append(".global main\n\n");
        asm.append("main:\n");
        asm.append("    push %rbp\n");
        asm.append("    mov %rsp, %rbp\n");
        asm.append("    sub $32, %rsp\n\n");

        for (IRInstruction instr : ir) {
            String op = instr.getOp();
            String a1 = instr.getArg1();
            String a2 = instr.getArg2();
            String res = instr.getResult();

            if (op == null) continue;

            switch (op) {
                case "=" -> {
                    loadToRegister(asm, "%rax", a1, variables);
                    storeFromRegister(asm, "%rax", res, variables);
                }
                case "+" -> {
                    loadToRegister(asm, "%rax", a1, variables);
                    loadToRegister(asm, "%rbx", a2, variables);
                    asm.append("    add %rbx, %rax\n");
                    storeFromRegister(asm, "%rax", res, variables);
                }
                case "-" -> {
                    loadToRegister(asm, "%rax", a1, variables);
                    loadToRegister(asm, "%rbx", a2, variables);
                    asm.append("    sub %rbx, %rax\n");
                    storeFromRegister(asm, "%rax", res, variables);
                }
                case "*" -> {
                    loadToRegister(asm, "%rax", a1, variables);
                    loadToRegister(asm, "%rbx", a2, variables);
                    asm.append("    imul %rbx, %rax\n");
                    storeFromRegister(asm, "%rax", res, variables);
                }
                case "/" -> {
                    loadToRegister(asm, "%rax", a1, variables);
                    loadToRegister(asm, "%rbx", a2, variables);
                    asm.append("    cqo\n");
                    asm.append("    idiv %rbx\n");
                    storeFromRegister(asm, "%rax", res, variables);
                }
                case "print" -> {
                    loadToRegister(asm, "%rax", a1, variables);
                    // Додаємо ці три рядки в генератор:
                    asm.append("    lea fmt(%rip), %rcx\n");
                    asm.append("    mov %rax, %rdx\n");
                    asm.append("    call printf\n");
                }

            }
        }

        asm.append("\n    add $32, %rsp\n");
        asm.append("    pop %rbp\n");
        asm.append("    xor %rax, %rax\n");
        asm.append("    ret\n");

        return asm.toString();
    }

    private static void loadToRegister(StringBuilder asm, String reg, String value, Map<String, String> vars) {
        if (value == null || value.isEmpty()) return;
        if (isNumber(value)) {
            asm.append("    mov $").append(value).append(", ").append(reg).append("\n");
        } else {
            String asmVar = vars.get(value);
            if (asmVar != null) {
                asm.append("    mov ").append(asmVar).append("(%rip), ").append(reg).append("\n");
            }
        }
    }

    private static void storeFromRegister(StringBuilder asm, String reg, String varName, Map<String, String> vars) {
        if (varName == null || varName.isEmpty()) return;
        String asmVar = vars.get(varName);
        if (asmVar != null) {
            asm.append("    mov ").append(reg).append(", ").append(asmVar).append("(%rip)\n");
        }
    }

    private static boolean isNumber(String s) {
        return s != null && s.matches("-?\\d+");
    }
}