package org.example.ui;

import org.example.ast.AstNode;
import org.example.analyzer.SwiftLexicalAnalyzer;
import org.example.analyzer.SwiftSyntaxAnalyzer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;

public class SwiftIDE extends JFrame {

    private final JTextArea codeArea;
    private final JTextArea outputArea;

    public SwiftIDE() {
        setTitle("Swift Lexical & Syntax Analyzer");
        setSize(1200, 800);
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

        codeArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                lineNumbers.revalidate();
                lineNumbers.repaint();
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { lineNumbers.repaint(); }
            public void removeUpdate(DocumentEvent e) { lineNumbers.repaint(); }
            public void changedUpdate(DocumentEvent e) { lineNumbers.repaint(); }
        });

        outputArea = new JTextArea(4, 0);
        outputArea.setForeground(Color.RED);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.BOLD, 13));

        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Analysis Results"));

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codeScroll, outputScroll);
        verticalSplit.setResizeWeight(0.85);
        verticalSplit.setDividerLocation(550);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 0, 0));

        JButton lexBtn = new JButton("Run lexical analysis");
        lexBtn.addActionListener(_ -> runLexicalAnalysis());

        JButton synBtn = new JButton("Run syntax analysis");
        synBtn.setBackground(new Color(34,139,34));
        synBtn.setForeground(Color.WHITE);
        synBtn.addActionListener(_ -> runSyntaxAnalysis());

        buttonPanel.add(lexBtn);
        buttonPanel.add(synBtn);

        add(verticalSplit, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private static class LineNumberView extends JComponent {
        private final JTextArea textArea;

        public LineNumberView(JTextArea textArea) {
            this.textArea = textArea;
            setBackground(new Color(240, 240, 240));
            setForeground(new Color(128, 128, 128));
            setFont(textArea.getFont());
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics metrics = getFontMetrics(getFont());
            int lineCount = Math.max(textArea.getLineCount(), 1);
            int digits = String.valueOf(lineCount).length();
            int width = (digits * metrics.charWidth('0')) + 15;

            return new Dimension(width, textArea.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            FontMetrics metrics = getFontMetrics(getFont());
            int lineHeight = metrics.getHeight();
            int lineCount = textArea.getLineCount();

            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());

            g2.setColor(getForeground());
            int y = metrics.getAscent() + textArea.getInsets().top;

            for (int i = 1; i <= lineCount; i++) {
                String label = String.valueOf(i);
                int labelWidth = metrics.stringWidth(label);
                g2.drawString(label, getWidth() - labelWidth - 8, y);
                y += lineHeight;
            }
        }
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String content = Files.readString(selectedFile.toPath());
                codeArea.setText(content);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage());
            }
        }
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
            outputArea.setText("Lexical analysis completed successfully. No errors found.");
        } else {
            outputArea.setForeground(Color.RED);
            outputArea.append("Lexical Errors Found:\n");

            for (var diag : result.diagnostics) {

                int line = diag.getLine();
                int column = diag.getColumn();
                String message = diag.getMessage();

                outputArea.append("Line " + line + ":" + column + " — " + message + "\n");
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

        if(!result.errors.isEmpty()) {
            outputArea.setForeground(Color.RED);
            outputArea.append("Syntax Errors Found:\n");
            for (String err : result.errors) {
                outputArea.append(err + "\n");
            }
        } else if (result.ast != null) {
            outputArea.setForeground(new Color(0,100,0));
            outputArea.append("Syntax Tree (AST) formed successfully:");

            displayAst(result.ast);
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

    public static void main(String[] args) {
        if(args.length > 0) {
            runConsoleMode(args[0]);
        } else {
            SwingUtilities.invokeLater(() -> new SwiftIDE().setVisible(true));
        }

    }

    private static void runConsoleMode(String filePath) {
        try {
            System.out.println("Running in Console Mode...");
            String code = Files.readString(new File(filePath).toPath());

            SwiftSyntaxAnalyzer analyzer = new SwiftSyntaxAnalyzer();
            SwiftSyntaxAnalyzer.SyntaxResult result = analyzer.analyze(code);

            if(!result.errors.isEmpty()) {
                System.err.println("Errors found:");
                result.errors.forEach(System.err::println);
            } else {
                System.out.println("AST Result:");
                result.ast.print("");
            }
        } catch (Exception e) {
            System.err.println("File error:" + e.getMessage());
        }
    }
}