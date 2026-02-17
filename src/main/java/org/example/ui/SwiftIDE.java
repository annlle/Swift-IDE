package org.example.ui;

import org.example.analyzer.SwiftLexicalAnalyzer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;

public class SwiftIDE extends JFrame {

    private final JTextArea codeArea;
    private final JTextArea errorArea;

    public SwiftIDE() {
        setTitle("Swift Lexical Analyzer");
        setSize(1000, 700);
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

        errorArea = new JTextArea(4, 0);
        errorArea.setForeground(Color.RED);
        errorArea.setEditable(false);
        errorArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        JScrollPane errorScroll = new JScrollPane(errorArea);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codeScroll, errorScroll);
        verticalSplit.setResizeWeight(0.85);
        verticalSplit.setDividerLocation(550);

        JButton analyzeButton = new JButton("Run lexical analysis");
        analyzeButton.setFont(new Font("Arial", Font.BOLD, 14));
        analyzeButton.setBackground(new Color(70, 130, 180));
        analyzeButton.setForeground(Color.WHITE);
        analyzeButton.addActionListener(_ -> runLexicalAnalysis());

        add(verticalSplit, BorderLayout.CENTER);
        add(analyzeButton, BorderLayout.SOUTH);
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
        errorArea.setText("");
        errorArea.setForeground(Color.RED);

        String code = codeArea.getText();
        if (code.isEmpty()) return;

        SwiftLexicalAnalyzer analyzer = new SwiftLexicalAnalyzer();
        SwiftLexicalAnalyzer.Result result = analyzer.analyze(code);

        if (result.diagnostics.isEmpty()) {
            errorArea.setForeground(new Color(0, 128, 0));
            errorArea.setText("Analysis completed successfully. No errors found.");
        } else {
            for (var diag : result.diagnostics) {
                String lineInfo = diag.getLine() > 0 ? " (Line: " + diag.getLine() + ")" : "";
                errorArea.append(diag.getSeverity() + ": " + diag.getMessage() + lineInfo + "\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwiftIDE().setVisible(true));
    }
}