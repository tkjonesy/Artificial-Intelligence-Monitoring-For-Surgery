package io.github.tkjonesy.utils.models;

import io.github.tkjonesy.ONNX.models.OnnxRunner;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Responsible for managing a sessions creation, termination, and status tracking
 */
@Getter
public class SessionHandler {

    private FileSession fileSession;
    private String sessionTitle;
    private String sessionDescription;
    private final LogHandler logHandler;

    private AtomicBoolean isActive = new AtomicBoolean(false);
    private Instant startTime;

    /**
     * Creates a new {@code SessionHandler} with specified {@link LogHandler}
     * @param logHandler used for logging session events
     */
    public SessionHandler(LogHandler logHandler) {
        this.logHandler = logHandler;
    }

    /**
     * Starts a new session
     * @param title Title for session
     * @param description Description of the session
     * @param onnxRunner The managing machine learning inference within the session
     * @return {@code true} if session starts successfully, {@code false} otherwise
     */
    public boolean startNewSession(String title, String description, OnnxRunner onnxRunner) {
        this.sessionTitle = title;
        this.sessionDescription = description;

        try{
            this.fileSession = new FileSession(onnxRunner, title, description, logHandler); // Throws RunTimeException if fails
            this.logHandler.setFileSession(fileSession);

        }catch (RuntimeException e) {
            System.err.println("Failed to start new SessionHandler: " + e.getMessage());
            return false;
        }

        this.startTime = Instant.now();
        onnxRunner.getInferenceLogQueue().addInfoLog("---Session started.---");
        this.isActive = new AtomicBoolean(true);
        return true;
    }

    /**
     * Ends the current active session and releases resources
     */
    public void endSession() {
        isActive.set(false);
        fileSession.endSession();
        fileSession.destroyVideoWriter();
        fileSession = null;
        startTime = null;
        this.logHandler.setFileSession(null);
    }

    /**
     * Checks if a session is currently active
     * @return {@code true} if a session is active, {@code false} otherwise
     */
    public boolean isSessionActive() {
        return isActive.get();
    }

    /**
     * Calculates and returns the duration of the current session
     * @return String representing the session duration in "MM:SS" format
     */
    public String getSessionDuration() {
        if (startTime == null) return "00:00";
        Duration duration = Duration.between(startTime, Instant.now());
        long minutes = duration.toMinutes();
        long seconds = duration.getSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

}
