package io.github.tkjonesy.frontend.utils;

import io.github.tkjonesy.frontend.mainGUI.DebugConsole;
import io.github.tkjonesy.utils.logging.LogType;

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
    public static void displayLog(String message, LogType logType) {
        SwingUtilities.invokeLater(() -> {
            switch(logType){
                case ERROR -> DebugConsole.getInstance().displayLog(message, DebugConsole.getInstance().getErrorStyle());
                case INFO -> DebugConsole.getInstance().displayLog(message, DebugConsole.getInstance().getInfoStyle());
                case TRACE -> DebugConsole.getInstance().displayLog(message, DebugConsole.getInstance().getTraceStyle());
                default -> DebugConsole.getInstance().displayLog(message, DebugConsole.getInstance().getDefaultStyle());
            }
        });
    }
}