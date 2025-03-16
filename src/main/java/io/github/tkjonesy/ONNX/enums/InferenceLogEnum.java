package io.github.tkjonesy.ONNX.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import io.github.tkjonesy.utils.settings.ProgramSettings;
import java.awt.Color;

/**
 * The {@code LogEnum} enum represents different log levels, each associated with a specific color.
 * It is used to classify log entries by type, such as errors, informational messages, and successes.
 */
@Getter
public enum InferenceLogEnum {
    WARNING(Color.YELLOW),
    LOG_ADDED(getLogAddedColor()),  // New dynamic color
    LOG_REMOVED(getLogRemovedColor()); // New dynamic color

    private Color color;

    InferenceLogEnum(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void updateColor(Color newColor) {
        this.color = newColor;
    }

    private static Color getLogAddedColor() {
        int[] colorArray = ProgramSettings.getCurrentSettings().getLogAddedColor();
        return new Color(colorArray[0], colorArray[1], colorArray[2]);
    }

    private static Color getLogRemovedColor() {
        int[] colorArray = ProgramSettings.getCurrentSettings().getLogRemovedColor();
        return new Color(colorArray[0], colorArray[1], colorArray[2]);
    }
}
