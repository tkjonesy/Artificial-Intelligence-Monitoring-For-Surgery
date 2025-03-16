package io.github.tkjonesy.utils.logging;

import io.github.tkjonesy.frontend.models.DebugConsoleManager;
import io.github.tkjonesy.utils.settings.ProgramSettings;
import lombok.Getter;
import lombok.Setter;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AIMsLogger {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static boolean debugModeEnabled = false;

    @Getter
    private static final PrintStream originalSystemOut = System.out;
    @Getter
    private static final PrintStream originalSystemErr = System.err;

    private static final PrintStream stdoutPrintStream;
    private static final PrintStream stderrPrintStream;

    static {
        // Initialize custom streams
        LogOutputStream stdoutLogStream = new LogOutputStream(false); // false = not error stream
        LogOutputStream stderrLogStream = new LogOutputStream(true);  // true = error stream
        stdoutPrintStream = new PrintStream(stdoutLogStream, true);
        stderrPrintStream = new PrintStream(stderrLogStream, true);
    }

    /**
     * Initializes the logger system based on current settings
     */
    public static void initialize(ProgramSettings settings) {
        debugModeEnabled = settings.isDebugMode() || settings.isContinuousLogging();

        DebugConsoleManager.initialize(settings);
    }

    /**
     * Starts redirecting system output streams to the debug console
     */
    public static void startRedirection() {
        // Redirect system output streams
        System.setOut(stdoutPrintStream);
        System.setErr(stderrPrintStream);

        TRACE("System output redirection started");
    }

    /**
     * Stops redirecting system output streams and restores the originals
     */
    public static void stopRedirection() {
        TRACE("System output redirection stopped");

        // Restore original streams
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);
    }

    public static void TRACE(String message) {
        if (debugModeEnabled) {
            String timestampedMessage = addTimestamp("[TRACE] " + message);
            DebugConsoleManager.displayLog(timestampedMessage, false);
        }
    }

    public static void INFO(String message) {
        if (debugModeEnabled) {
            String timestampedMessage = addTimestamp("[INFO] " + message);
            DebugConsoleManager.displayLog(timestampedMessage, false);
        }
    }

    public static void WARN(String message) {
        if (debugModeEnabled) {
            String timestampedMessage = addTimestamp("[WARN] " + message);
            DebugConsoleManager.displayLog(timestampedMessage, false);
        }
    }

    public static void ERROR(String message) {
        if (debugModeEnabled) {
            String timestampedMessage = addTimestamp("[ERROR] " + message);
            DebugConsoleManager.displayLog(timestampedMessage, true);
        }
    }

    public static void FATAL(String message) {
        if (debugModeEnabled) {
            String timestampedMessage = addTimestamp("[FATAL] " + message);
            DebugConsoleManager.displayLog(timestampedMessage, true);
        }
    }

    /**
     * Adds a timestamp to a log message
     */
    private static String addTimestamp(String message) {
        return "[" + LocalTime.now().format(TIME_FORMATTER) + "] " + message;
    }

    /**
     * Custom OutputStream that writes to the debug console
     */
    private static class LogOutputStream extends OutputStream {
        private final StringBuilder buffer = new StringBuilder();
        private final boolean isErrorStream;

        public LogOutputStream(boolean isErrorStream) {
            this.isErrorStream = isErrorStream;
        }

        @Override
        public void write(int b) {
            char c = (char) b;
            if (c == '\n') {
                String message = buffer.toString();
                if (debugModeEnabled) {
                    DebugConsoleManager.displayLog(message, isErrorStream);
                }
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
        }
    }
}