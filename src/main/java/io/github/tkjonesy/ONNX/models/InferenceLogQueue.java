package io.github.tkjonesy.ONNX.models;

import io.github.tkjonesy.ONNX.enums.InferenceLogEnum;
import io.github.tkjonesy.utils.settings.ProgramSettings;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The {@code LogQueue} class represents a queue of logs that can be added to and
 * retrieved from. It supports adding logs with different log levels (error, info,
 * success) and retrieving logs in a first-in, first-out (FIFO) order.
 */
public class InferenceLogQueue {

    /** The queue that stores the logs in FIFO order. */
    private final Queue<InferenceLog> inferenceLogs;

    /**
     * Initializes a new {@code LogQueue} with an empty queue.
     */
    public InferenceLogQueue() {
        this.inferenceLogs = new LinkedList<>();
    }

    /**
     * Flushes all logs from the queue.
     */
    public void flushLogs() {
        inferenceLogs.clear();
    }

    /**
     * Adds an error log to the queue.
     *
     * @param message The message to be logged with an error level.
     */
    public void addRedLog(String message) {
        inferenceLogs.add(new InferenceLog(InferenceLogEnum.LOG_REMOVED, message));
    }

    /**
     * Adds an informational log (yellow) to the queue.
     *
     * @param message The message to be logged with an informational level.

    public void addYellowLog(String message) {
        inferenceLogs.add(new InferenceLog(InferenceLogEnum.INFO, message));
    }
     */

    /**
     * Adds a success log to the queue.
     *
     * @param message The message to be logged with a success level.
     */
    public void addGreenLog(String message) {
        inferenceLogs.add(new InferenceLog(InferenceLogEnum.LOG_ADDED, message));
    }

    /**
     * Gets the dynamically updated "Added" color from settings.
     */
    private static java.awt.Color getLogAddedColor() {
        int[] colorArray = ProgramSettings.getCurrentSettings().getLogAddedColor();
        return new java.awt.Color(colorArray[0], colorArray[1], colorArray[2]);
    }

    /**
     * Gets the dynamically updated "Removed" color from settings.
     */
    private static java.awt.Color getLogRemovedColor() {
        int[] colorArray = ProgramSettings.getCurrentSettings().getLogRemovedColor();
        return new java.awt.Color(colorArray[0], colorArray[1], colorArray[2]);
    }

    /**
     * Retrieves and removes the latest log from the queue.
     * Returns {@code null} if the queue is empty.
     *
     * @return The latest {@code Log} object, or {@code null} if no logs are available.
     */
    public InferenceLog getNextLog(){
        return inferenceLogs.poll();
    }
}
