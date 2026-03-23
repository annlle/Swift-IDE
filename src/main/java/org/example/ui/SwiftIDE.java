package org.example.ui;

import org.example.ast.AstNode;
import org.example.analyzer.SwiftLexicalAnalyzer;
import org.example.analyzer.SwiftSyntaxAnalyzer;
import org.example.analyzer.SemanticAnalyzer;
import org.example.codegen.ASMFileWriter;
import org.example.ir.IRGenerator;
import org.example.ir.IRInstruction;
import org.example.ir.IROptimizer;
import org.example.codegen.ASMGenerator;
import org.example.codegen.GCCCompiler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class SwiftIDE extends JFrame {

    private final JTextArea codeArea;
    private final JTextArea outputArea;

    public SwiftIDE() {
        setTitle("Swift IDE - Lexical, Syntax & Semantic Analyzer");
        setSize(1200, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open file...");
        openItem.addActionListener(_ -> openFile());
        fileMenu.add(openItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        codeArea = new JTextArea();
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane codeScroll = new JScrollPane(codeArea);
        LineNumberView lineNumbers = new LineNumberView(codeArea);
        codeScroll.setRowHeaderView(lineNumbers);

        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            private void update() { lineNumbers.revalidate(); lineNumbers.repaint(); }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        outputArea = new JTextArea(10, 0);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.BOLD, 13));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Analysis Results"));

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codeScroll, outputScroll);
        verticalSplit.setResizeWeight(0.7);
        verticalSplit.setDividerLocation(500);

        JPanel buttonPanel = getButtonPanel();

        add(verticalSplit, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel getButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 5, 5));

        JButton lexBtn = new JButton("Run lexical analysis");
        lexBtn.addActionListener(_ -> runLexicalAnalysis());

        JButton synBtn = new JButton("Run syntax analysis");
        synBtn.setBackground(new Color(34, 139, 34));
        synBtn.setForeground(Color.WHITE);
        synBtn.addActionListener(_ -> runSyntaxAnalysis());

        JButton semBtn = new JButton("Run semantic analysis");
        semBtn.setBackground(new Color(70, 130, 180));
        semBtn.setForeground(Color.WHITE);
        semBtn.addActionListener(_ -> runSemanticAnalysis());

        JButton compileBtn = new JButton("Generate & Compile");
        compileBtn.setBackground(new Color(255, 140, 0));
        compileBtn.setForeground(Color.WHITE);
        compileBtn.addActionListener(_ -> runCodeGenerationAndCompilation());

        panel.add(lexBtn);
        panel.add(synBtn);
        panel.add(semBtn);
        panel.add(compileBtn);
        return panel;
    }

    private void runLexicalAnalysis() {
        outputArea.setText("");
        outputArea.setForeground(Color.BLACK);
        String code = codeArea.getText();
        if (code.isEmpty()) return;
        SwiftLexicalAnalyzer analyzer = new SwiftLexicalAnalyzer();
        SwiftLexicalAnalyzer.Result result = analyzer.analyze(code);
        if (result.diagnostics.isEmpty()) {
            outputArea.setForeground(new Color(0, 128, 0));
            outputArea.setText("Lexical analysis completed successfully.");
        } else {
            outputArea.setForeground(Color.RED);
            for (var diag : result.diagnostics) {
                outputArea.append("Line " + diag.getLine() + ":" + diag.getColumn() + " — " + diag.getMessage() + "\n");
            }
        }
    }

    private void runSyntaxAnalysis() {
        outputArea.setText("");
        outputArea.setForeground(Color.BLACK);
        String code = codeArea.getText();
        if (code.isEmpty()) return;
        SwiftSyntaxAnalyzer analyzer = new SwiftSyntaxAnalyzer();
        SwiftSyntaxAnalyzer.SyntaxResult result = analyzer.analyze(code);
        if (!result.errors.isEmpty()) {
            outputArea.setForeground(Color.RED);
            for (String err : result.errors) outputArea.append(err + "\n");
        } else if (result.ast != null) {
            outputArea.setForeground(new Color(0, 128, 0));
            outputArea.append("Syntax Tree (AST) formed:\n");
            displayAst(result.ast);
        }
    }

    private void runSemanticAnalysis() {
        outputArea.setText("");
        String code = codeArea.getText();
        if (code.isEmpty()) return;

        SwiftSyntaxAnalyzer syntaxAnalyzer = new SwiftSyntaxAnalyzer();
        SwiftSyntaxAnalyzer.SyntaxResult syntaxResult = syntaxAnalyzer.analyze(code);

        if (!syntaxResult.errors.isEmpty()) {
            outputArea.setForeground(Color.RED);
            outputArea.setText("Cannot run semantic analysis: Syntax errors found!\n");
            for (String err : syntaxResult.errors) outputArea.append(err + "\n");
            return;
        }

        if (syntaxResult.ast != null) {
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            syntaxResult.ast.accept(semanticAnalyzer);

            boolean hasErrors = !semanticAnalyzer.getErrors().isEmpty();
            boolean hasWarnings = !semanticAnalyzer.getWarnings().isEmpty();

            if(hasErrors) {
                outputArea.setForeground(Color.RED);
                outputArea.append("Semantic errors found:\n");
                for(String err : semanticAnalyzer.getErrors()) outputArea.append(err + "\n");
            }

            if(hasWarnings) {
                outputArea.setForeground(hasErrors ? Color.RED : Color.ORANGE);
                outputArea.append("Semantic warnings found:\n");
                for (String warn : semanticAnalyzer.getWarnings()) outputArea.append(warn + "\n");
            }

            if(!hasErrors) {
                outputArea.setForeground(new Color(0,128,0));
                outputArea.append("Semantic analysis completed successfully.\n");

                IRGenerator irGenerator = new IRGenerator();
                syntaxResult.ast.accept(irGenerator);

                List<IRInstruction> irCode = irGenerator.generate(syntaxResult.ast);
                if(!irCode.isEmpty()) {
                    outputArea.append("IR Code\n");
                    for(IRInstruction instr : irCode) outputArea.append(instr.toString() + "\n");
                } else {
                    outputArea.append("\nNo IR instructions generated.\n");
                }
            }
        }
    }

    private void runCodeGenerationAndCompilation() {
        outputArea.setText("");
        String code = codeArea.getText();
        if (code.isEmpty()) return;

        try {
            // 1. Синтаксический анализ
            SwiftSyntaxAnalyzer syntaxAnalyzer = new SwiftSyntaxAnalyzer();
            SwiftSyntaxAnalyzer.SyntaxResult syntaxResult = syntaxAnalyzer.analyze(code);

            if (!syntaxResult.errors.isEmpty()) {
                outputArea.setForeground(Color.RED);
                outputArea.append("Cannot generate code: Syntax errors found!\n");
                syntaxResult.errors.forEach(err -> outputArea.append(err + "\n"));
                return;
            }

            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            syntaxResult.ast.accept(semanticAnalyzer);

            if (!semanticAnalyzer.getErrors().isEmpty()) {
                outputArea.setForeground(Color.RED);
                outputArea.append("Cannot generate code: Semantic errors found!\n");
                semanticAnalyzer.getErrors().forEach(err -> outputArea.append(err + "\n"));
                return;
            }

            IRGenerator irGenerator = new IRGenerator();
            List<IRInstruction> irCode = irGenerator.generate(syntaxResult.ast);

            if (irCode.isEmpty()) {
                outputArea.setForeground(Color.RED);
                outputArea.append("No IR instructions generated.\n");
                return;
            }

            irCode = IROptimizer.optimize(irCode);

            String asmCode = ASMGenerator.generate(irCode);

            String asmFile = "program.s";
            ASMFileWriter.writeToFile(asmCode, asmFile);

            String exeFile = "program";
            GCCCompiler.compile(asmFile, exeFile);

            outputArea.setForeground(new Color(0, 128, 0));
            outputArea.append("Code generation and compilation finished.\n");
            outputArea.append("Executable: " + exeFile + "\n");

        } catch (Exception e) {
            outputArea.setForeground(Color.RED);
            outputArea.append("Error: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void displayAst(AstNode node) {
        if (node == null) return;
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(baos);
        java.io.PrintStream oldOut = System.out;
        System.setOut(ps);
        node.print("");
        System.out.flush();
        System.setOut(oldOut);
        outputArea.append(baos.toString());
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                codeArea.setText(Files.readString(fileChooser.getSelectedFile().toPath()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private static void runConsoleMode(String filePath) {
        try {
            System.out.println("Running in Console Mode...");
            String code = Files.readString(new File(filePath).toPath());

            SwiftSyntaxAnalyzer analyzer = new SwiftSyntaxAnalyzer();
            SwiftSyntaxAnalyzer.SyntaxResult result = analyzer.analyze(code);

            if (!result.errors.isEmpty()) {
                result.errors.forEach(System.err::println);
                return;
            }

            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            result.ast.accept(semanticAnalyzer);

            if (!semanticAnalyzer.getErrors().isEmpty()) {
                semanticAnalyzer.getErrors().forEach(System.err::println);
                return;
            }

            IRGenerator irGenerator = new IRGenerator();
            List<IRInstruction> irCode = irGenerator.generate(result.ast);
            irCode = IROptimizer.optimize(irCode);

            String asmCode = ASMGenerator.generate(irCode);
            String asmFile = "program.s";
            ASMFileWriter.writeToFile(asmCode, asmFile);

            String exeFile = "program";
            GCCCompiler.compile(asmFile, exeFile);

            System.out.println("Success! Code generation and compilation finished.");
            System.out.println("Executable created: " + exeFile);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            runConsoleMode(args[0]);
        } else {
            SwingUtilities.invokeLater(() -> new SwiftIDE().setVisible(true));
        }
    }

    private static class LineNumberView extends JComponent {
        private final JTextArea textArea;

        public LineNumberView(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics metrics = getFontMetrics(textArea.getFont());
            int lineCount = Math.max(textArea.getLineCount(), 1);
            int width = (String.valueOf(lineCount).length() * metrics.charWidth('0')) + 15;
            return new Dimension(width, textArea.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setFont(textArea.getFont());
            g.setColor(Color.LIGHT_GRAY);
            FontMetrics metrics = g.getFontMetrics();
            int lineHeight = metrics.getHeight();
            int ascent = metrics.getAscent();
            for (int i = 0; i < textArea.getLineCount(); i++) {
                g.drawString(String.valueOf(i + 1), 5, (i * lineHeight) + ascent);
            }
        }
    }
}