package io.github.tkjonesy.frontend.models;

import io.github.tkjonesy.frontend.mainGUI.DebugConsole;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.utils.settings.ProgramSettings;

/**
 * Manages the debug console window.
 * This class provides static methods for showing, hiding, and logging to the debug console.
 */
public class DebugConsoleManager {
    private static boolean debugModeEnabled = false;

    /**
     * Initializes the debug console based on the current program settings.
     *
     * @param settings The program settings
     */
    public static void initialize(ProgramSettings settings) {
        debugModeEnabled = true;

        if (debugModeEnabled) {
            AIMsLogger.info("Debug mode is enabled, starting debug console");
            DebugConsole.getInstance().startConsole();
        } else {
            AIMsLogger.info("Debug mode is disabled");
        }
    }

    /**
     * Shows the debug console window.
     */
    public static void showConsole() {
        DebugConsole.getInstance().setVisible(true);

        if (!debugModeEnabled) {
            DebugConsole.getInstance().startConsole();
            debugModeEnabled = true;
        }
    }

    /**
     * Hides the debug console window.
     */
    public static void hideConsole() {
        DebugConsole.getInstance().setVisible(false);
    }

    /**
     * Toggles the debug console visibility.
     *
     * @return true if console is now visible, false otherwise
     */
    public static boolean toggleConsole() {
        boolean newVisibility = !DebugConsole.getInstance().isVisible();
        DebugConsole.getInstance().setVisible(newVisibility);

        if (newVisibility && !debugModeEnabled) {
            DebugConsole.getInstance().startConsole();
            debugModeEnabled = true;
        }

        return newVisibility;
    }

    /**
     * Enables debug mode and starts the console.
     */
    public static void enableDebugMode() {
        if (!debugModeEnabled) {
            DebugConsole.getInstance().startConsole();
            debugModeEnabled = true;
        }
    }

    /**
     * Disables debug mode and stops the console.
     */
    public static void disableDebugMode() {
        if (debugModeEnabled) {
            DebugConsole.getInstance().stopConsole();
            debugModeEnabled = false;
        }
    }

    /**
     * Logs an info message to the console.
     *
     * @param message The info message
     */
    public static void info(String message) {
        if (debugModeEnabled) {
            DebugConsole.getInstance().info(message);
        }
    }

    /**
     * Logs an error message to the console.
     *
     * @param message The error message
     */
    public static void error(String message) {
        if (debugModeEnabled) {
            DebugConsole.getInstance().error(message);
        }
    }
}