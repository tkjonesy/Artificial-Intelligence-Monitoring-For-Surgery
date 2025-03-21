package io.github.tkjonesy.utils.logging;

import io.github.tkjonesy.frontend.App;
import io.github.tkjonesy.frontend.utils.DebugConsoleManager;
import io.github.tkjonesy.utils.settings.ProgramSettings;
import lombok.Getter;

import java.io.*;
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
        App instance = App.getInstance();
        if(instance.getButtonPanel()!=null) {
            instance.getButtonPanel().updateDebugButtonVisibility();
        }
        instance.updateTitle();

        startRedirection();
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

    public static void DEBUG(String message) {
        if (debugModeEnabled) {
            String timestampedMessage = addTimestamp("[DEBUG]\t" + message);
            DebugConsoleManager.displayLog(timestampedMessage, LogType.DEBUG);
        }
    }

    public static void TRACE(String message) {
        if (debugModeEnabled) {
            String timestampedMessage = addTimestamp("[TRACE]\t" + message);
            DebugConsoleManager.displayLog(timestampedMessage, LogType.TRACE);
        }
    }

    public static void INFO(String message) {
        if (debugModeEnabled) {
            String timestampedMessage = addTimestamp("[INFO]\t" + message);
            DebugConsoleManager.displayLog(timestampedMessage, LogType.INFO);
        }
    }

    public static void WARN(String message) {
        if (debugModeEnabled) {
            String timestampedMessage = addTimestamp("[WARN]\t" + message);
            DebugConsoleManager.displayLog(timestampedMessage, LogType.ERROR);
        }
    }

    public static void ERROR(String message) {
        if (debugModeEnabled) {
            String timestampedMessage = addTimestamp("[ERROR]\t" + message);
            DebugConsoleManager.displayLog(timestampedMessage, LogType.ERROR);
        }
    }

    public static void FATAL(String message) {
        if (debugModeEnabled) {
            String timestampedMessage = addTimestamp("[FATAL]\t" + message);
            DebugConsoleManager.displayLog(timestampedMessage, LogType.ERROR);
        }
    }

    /**
     * Adds a timestamp to a log message
     */
    private static String addTimestamp(String message) {
        return "[" + LocalTime.now().format(TIME_FORMATTER) + "]\t" + message;
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
                    String timestampedMessage = addTimestamp("[CONSOLE]\t"+message);
                    LogType logType = isErrorStream ? LogType.ERROR : LogType.INFO;
                    DebugConsoleManager.displayLog(timestampedMessage, logType);
                }
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
        }
    }
}