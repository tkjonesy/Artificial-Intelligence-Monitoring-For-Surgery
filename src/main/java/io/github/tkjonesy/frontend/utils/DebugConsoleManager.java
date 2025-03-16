package io.github.tkjonesy.frontend.utils;

import io.github.tkjonesy.frontend.mainGUI.DebugConsole;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.utils.settings.ProgramSettings;

import javax.swing.SwingUtilities;

/**
 * Manages the debug console window.
 * This class provides static methods for showing, hiding, and logging to the debug console.
 */
public class DebugConsoleManager {

    /**
     * Toggles the debug console visibility.
     *
     * @return true if console is now visible, false otherwise
     */
    public static boolean toggleConsole() {
        boolean newVisibility = !DebugConsole.getInstance().isVisible();
        DebugConsole.getInstance().setVisible(newVisibility);
        return newVisibility;
    }

    /**
     * Display a log message in the console
     *
     * @param message The message to display
     * @param isError Whether this is an error message
     */
    public static void displayLog(String message, boolean isError) {
        SwingUtilities.invokeLater(() -> {
            if (isError) {
                DebugConsole.getInstance().displayLog(message, DebugConsole.getInstance().getErrorStyle());
            } else {
                DebugConsole.getInstance().displayLog(message, DebugConsole.getInstance().getDefaultStyle());
            }
        });
    }

    /**
     * Logs an info message to the console.
     *
     * @param message The info message
     */
    public static void INFO(String message) {
        SwingUtilities.invokeLater(() ->
                DebugConsole.getInstance().displayLog(message, DebugConsole.getInstance().getInfoStyle())
        );
    }

    /**
     * Logs an error message to the console.
     *
     * @param message The error message
     */
    public static void ERROR(String message) {
        SwingUtilities.invokeLater(() ->
                DebugConsole.getInstance().displayLog(message, DebugConsole.getInstance().getErrorStyle())
        );
    }
}