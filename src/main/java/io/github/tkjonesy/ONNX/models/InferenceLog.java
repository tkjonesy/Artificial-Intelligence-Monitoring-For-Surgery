package io.github.tkjonesy.ONNX.models;

import io.github.tkjonesy.ONNX.enums.InferenceLogEnum;
import lombok.Getter;

import java.time.temporal.ChronoUnit;

/**
 * The {@code Log} class represents a log entry with a log type, message, and timestamp.
 * It provides methods for generating logs with a timestamp and displaying them in a UI component.
 */
@Getter
public class InferenceLog {

    /** The type of the log, defining its level and color (e.g., ERROR, INFO, SUCCESS). */
    private final InferenceLogEnum logType;

    /** The message associated with the log entry. */
    private final String message;

    /** The timestamp indicating when the log was created. */
    private final String timeStamp;

    /**
     * Creates a {@code Log} with a specified type and message, setting the timestamp to the current time.
     *
     * @param logType The type of the log, specifying the log level and color.
     * @param message The message for the log entry.
     */
    public InferenceLog(InferenceLogEnum logType, String message) {
        this.logType = logType;
        this.message = message;
        this.timeStamp = getCurrentTimestamp();
    }

    /**
     * Generates the current timestamp in a formatted string.
     *
     * @return The formatted current timestamp.
     */
    private String getCurrentTimestamp(){
        return "[" + java.time.LocalTime.now().truncatedTo(ChronoUnit.SECONDS) + "]";
    }
}
