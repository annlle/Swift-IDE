package org.example.codegen;

import java.io.FileWriter;
import java.io.IOException;

public class ASMFileWriter {

    public static void writeToFile(String asmCode, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(asmCode);
            System.out.println("ASM saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing ASM file: " + e.getMessage());
        }
    }
}
