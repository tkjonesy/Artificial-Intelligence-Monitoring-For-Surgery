package io.github.tkjonesy.utils.models;

import io.github.tkjonesy.ONNX.models.InferenceLog;
import io.github.tkjonesy.ONNX.models.InferenceLogQueue;
import io.github.tkjonesy.utils.settings.ProgramSettings;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;

public class LogHandler {

    @Getter
    private static JTextPane logTextPane;
    @Getter
    private static final InferenceLogQueue INFERENCE_LOG_QUEUE = new InferenceLogQueue();

    // New method to set FileSession after initialization
    @Setter
    private FileSession fileSession;
    private Timer timer;

    // This StringBuilder accumulates the log messages in HTML format
    private static int fontSize;
    private static StringBuilder logHtmlContent;

    /**
     * Constructs a LogHandler object for displaying log messages
     * @param textPane Where the log messages will be displayed
     */
    public LogHandler(JTextPane textPane) {
        logTextPane = textPane;

        fontSize = ProgramSettings.getCurrentSettings().getLogFontSize();
        logHtmlContent = new StringBuilder("<html><body style='color:white; font-size:" + fontSize + "pt;'>");

        startLogProcessing();
    }

    /**
     * Processes a log entry by appending it to the log text pane and saving it to a file.
     *
     * @param inferenceLog The log entry to process.
     */
    private void processLog(InferenceLog inferenceLog){
        appendLogToPane(inferenceLog);
        saveLogToFile(inferenceLog);
    }

    /**
     * Forces the processing of the next log entry
     */
    public static void forceProcessNextLog(){
        InferenceLog nextInferenceLog = INFERENCE_LOG_QUEUE.getNextLog();
        if(nextInferenceLog != null){
            appendLogToPane(nextInferenceLog);
        }
    }

    /**
     * Appends a new log entry as colored HTML text to the log text pane.
     *
     * @param inferenceLog The log entry to display.
     */
    private static void appendLogToPane(InferenceLog inferenceLog) {
        // Get the color of the log type as a hex code
        String colorHex = "#" + Integer.toHexString(inferenceLog.getLogType().getColor().getRGB()).substring(2);

        // Format the log entry as an HTML line with timestamp and message
        String logMessage = String.format(
                "<span style='color:%s; font-size:%dpt;'>%s - %s</span><br>",
                colorHex, fontSize, inferenceLog.getTimeStamp(), inferenceLog.getMessage()
        );

        // Append the log message to the accumulated HTML content
        logHtmlContent.append(logMessage);
        logTextPane.setText(logHtmlContent + "</body></html>");

        // Auto-scroll to the bottom of the JTextPane
        logTextPane.setCaretPosition(logTextPane.getDocument().getLength());
    }

    /**
     * Saves a log entry to a file.
     *
     * @param inferenceLog The log entry to save.
     */
    private void saveLogToFile(InferenceLog inferenceLog){
        fileSession.writeLogToFile(inferenceLog);
    }

    /**
     * Starts a timer that processes logs from the log queue every second.
     */
    public void startLogProcessing() {
        this.timer = new Timer(1000, e ->
        {
            // Process logs from the queue
            InferenceLog nextInferenceLog;
            while ((nextInferenceLog = INFERENCE_LOG_QUEUE.getNextLog()) != null && fileSession != null){
                processLog(nextInferenceLog);
            }
        });
        timer.start();
    }


    /**
     * Clears the log display when a session ends.
     */
    public static void clearLogPane() {
        logHtmlContent.setLength(0);
        fontSize = ProgramSettings.getCurrentSettings().getLogFontSize(); // Just in case font was changed
        logHtmlContent.append("<html><body style='color:white; font-size:")
                .append(fontSize)
                .append("pt;'>");
        logTextPane.setText(logHtmlContent + "</body></html>");
    }
}
