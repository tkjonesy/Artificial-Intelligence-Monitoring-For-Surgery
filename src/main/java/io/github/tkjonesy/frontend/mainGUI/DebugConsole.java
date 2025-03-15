package io.github.tkjonesy.frontend.mainGUI;

import lombok.Getter;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * A debug console window that captures and displays System.out and System.err output.
 * This component is useful for debugging and monitoring application logs.
 */
public class DebugConsole extends JFrame {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final Color BACKGROUND_COLOR = new Color(30, 31, 34);
    private static final Color DEFAULT_TEXT_COLOR = Color.WHITE;
    private static final Color ERROR_TEXT_COLOR = new Color(255, 80, 80);
    private static final Color INFO_TEXT_COLOR = new Color(135, 206, 250);
    private static final Color DEBUG_TEXT_COLOR = new Color(170, 255, 170);

    @Getter
    private final JTextPane consoleTextPane;
    private final StyledDocument document;
    private final PrintStream originalSystemOut;
    private final PrintStream originalSystemErr;
    private final Style defaultStyle;
    private final Style errorStyle;
    private final Style infoStyle;
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

        // Save original streams
        originalSystemOut = System.out;
        originalSystemErr = System.err;

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
     * Starts the debug console by redirecting System.out and System.err
     */
    public void startConsole() {
        // Redirect system output streams
        System.setOut(new PrintStream(new ConsoleOutputStream(defaultStyle), true));
        System.setErr(new PrintStream(new ConsoleOutputStream(errorStyle), true));

        // Log that the console has started
        appendLog("Debug console started - System.out and System.err are now redirected", infoStyle);
    }

    /**
     * Stops the debug console and restores original System.out and System.err
     */
    public void stopConsole() {
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);
        appendLog("Debug console stopped", infoStyle);
    }

    /**
     * Clears the console text
     */
    public void clearConsole() {
        try {
            document.remove(0, document.getLength());
            appendLog("Console cleared", infoStyle);
        } catch (BadLocationException e) {
            appendLog("Error clearing console: " + e.getMessage(), errorStyle);
        }
    }

    /**
     * Appends a log message to the console with the current timestamp
     *
     * @param message The message to log
     * @param style The style to apply to the message
     */
    public void appendLog(String message, Style style) {
        try {
            String timestamp = "[" + LocalTime.now().format(TIME_FORMATTER) + "] ";
            document.insertString(document.getLength(), timestamp, infoStyle);
            document.insertString(document.getLength(), message + "\n", style);

            // Auto-scroll to bottom
            consoleTextPane.setCaretPosition(document.getLength());
        } catch (BadLocationException e) {
            // If we can't log to the console, print to the original out
            originalSystemErr.println("Error logging to debug console: " + e.getMessage());
        }
    }

    /**
     * Logs an info message
     *
     * @param message The info message
     */
    public void info(String message) {
        appendLog(message, infoStyle);
    }

    /**
     * Logs an error message
     *
     * @param message The error message
     */
    public void error(String message) {
        appendLog(message, errorStyle);
    }

    /**
     * Custom OutputStream that writes to the debug console
     */
    private class ConsoleOutputStream extends OutputStream {
        private final StringBuilder buffer = new StringBuilder();
        private final Style style;

        public ConsoleOutputStream(Style style) {
            this.style = style;
        }

        @Override
        public void write(int b) {
            char c = (char) b;
            if (c == '\n') {
                final String message = buffer.toString();
                SwingUtilities.invokeLater(() -> appendLog(message, style));
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
        }
    }
}