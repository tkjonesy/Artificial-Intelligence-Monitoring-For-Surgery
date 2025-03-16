package io.github.tkjonesy.frontend.mainGUI;

import lombok.Getter;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A debug console window that displays log messages.
 * This component is focused on UI display only.
 */
public class DebugConsole extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(30, 31, 34);
    private static final Color DEFAULT_TEXT_COLOR = Color.WHITE;
    private static final Color ERROR_TEXT_COLOR = new Color(255, 80, 80);
    private static final Color INFO_TEXT_COLOR = new Color(135, 206, 250);
    private static final Color DEBUG_TEXT_COLOR = new Color(170, 255, 170);

    @Getter
    private final JTextPane consoleTextPane;
    private final StyledDocument document;

    @Getter
    private final Style defaultStyle;
    @Getter
    private final Style errorStyle;
    @Getter
    private final Style infoStyle;
    @Getter
    private final Style debugStyle;

    private static DebugConsole instance;

    /**
     * Gets the singleton instance of the DebugConsole.
     * @return the DebugConsole instance
     */
    public static synchronized DebugConsole getInstance() {
        if (instance == null) {
            instance = new DebugConsole();
        }
        return instance;
    }

    private DebugConsole() {
        super("AIMs DEBUG");

        setSize(800, 600);
        setMinimumSize(new Dimension(400, 300));

        consoleTextPane = new JTextPane();
        consoleTextPane.setEditable(false);
        consoleTextPane.setBackground(BACKGROUND_COLOR);

        document = consoleTextPane.getStyledDocument();

        // Create styles
        defaultStyle = consoleTextPane.addStyle("default", null);
        StyleConstants.setForeground(defaultStyle, DEFAULT_TEXT_COLOR);
        StyleConstants.setFontFamily(defaultStyle, "Monospaced");

        errorStyle = consoleTextPane.addStyle("error", null);
        StyleConstants.setForeground(errorStyle, ERROR_TEXT_COLOR);
        StyleConstants.setFontFamily(errorStyle, "Monospaced");

        infoStyle = consoleTextPane.addStyle("info", null);
        StyleConstants.setForeground(infoStyle, INFO_TEXT_COLOR);
        StyleConstants.setFontFamily(infoStyle, "Monospaced");

        debugStyle = consoleTextPane.addStyle("debug", null);
        StyleConstants.setForeground(debugStyle, DEBUG_TEXT_COLOR);
        StyleConstants.setFontFamily(debugStyle, "Monospaced");

        JScrollPane scrollPane = new JScrollPane(consoleTextPane);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane, BorderLayout.CENTER);

        // Add clear button at the bottom
        JButton clearButton = new JButton("Clear Console");
        clearButton.addActionListener(e -> clearConsole());
        add(clearButton, BorderLayout.SOUTH);

        // Handle window closing - should just hide, not dispose
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
    }

    /**
     * Displays a log message to the console with the specified style
     *
     * @param message The message to log
     * @param style The style to apply to the message
     */
    public void displayLog(String message, Style style) {
        try {
            document.insertString(document.getLength(), message + "\n", style);

            // Auto-scroll to bottom
            consoleTextPane.setCaretPosition(document.getLength());
        } catch (BadLocationException e) {
            // If we can't log to the console, print to the original out
            System.err.println("Error logging to debug console: " + e.getMessage());
        }
    }

    /**
     * Clears the console text
     */
    public void clearConsole() {
        try {
            document.remove(0, document.getLength());
            displayLog("Console cleared", infoStyle);
        } catch (BadLocationException e) {
            displayLog("Error clearing console: " + e.getMessage(), errorStyle);
        }
    }
}