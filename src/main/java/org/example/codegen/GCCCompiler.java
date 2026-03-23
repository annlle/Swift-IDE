package org.example.codegen;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GCCCompiler {

    private static final Logger LOGGER = Logger.getLogger(GCCCompiler.class.getName());

    public static void compile(String asmFile, String outputFile) {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "gcc",
                    asmFile,
                    "-o",
                    outputFile
            );

            builder.inheritIO();

            Process process = builder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                LOGGER.info("Executable successfully created: " + outputFile);
            } else {
                LOGGER.severe("Compilation failed. Exit code: " + exitCode);
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error running GCC on file: " + asmFile, e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "The compilation process was interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}