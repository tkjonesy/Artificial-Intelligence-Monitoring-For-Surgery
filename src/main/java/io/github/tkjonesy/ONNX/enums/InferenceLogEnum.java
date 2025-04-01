package io.github.tkjonesy.ONNX.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.awt.Color;

/**
 * The {@code LogEnum} enum represents different log levels, each associated with a specific color.
 * It is used to classify log entries by type, such as errors, informational messages, and successes.
 */
@Getter
@AllArgsConstructor
public enum InferenceLogEnum {
    INFO(Color.YELLOW),
    LOG_ADDED(Color.GREEN),
    LOG_REMOVED(Color.RED);

    private Color color;

    public void updateColor(int[] colorArray) {
        if (colorArray != null && colorArray.length == 3) {
            this.color = new Color(colorArray[0], colorArray[1], colorArray[2]);
        }
    }
}
