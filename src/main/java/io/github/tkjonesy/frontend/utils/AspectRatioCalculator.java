package io.github.tkjonesy.frontend.utils;

import io.github.tkjonesy.utils.logging.AIMsLogger;

import java.awt.*;

public class AspectRatioCalculator {

    public static Dimension calculateRatio(int areaWidth, int areaHeight, int widthRatio, int heightRatio) {

        /*
         * Examples:
         * targetRatio = 16:9 = 1.78
         * 1. w = 16, h = 10, currentRatio = 16:10 = 1.67
         *      when currentRatio < targetRatio, width is the limiter, new height is width / targetRatio
         * 2. w = 17, h = 9, currentRatio = 17:9 = 1.89
         *      when currentRatio > targetRatio, height is the limiter, new width is height * targetRatio
         * 3. w = 16, h = 9, currentRatio = 16:9 = 1.78
         *      currentRatio is already targetRatio, so nothing has to be altered
         *
         * targetRatio = 1:1 = 1
         * 4. w = 5, h = 7, currentRatio = 5:7 = 0.714
         *      currentRatio < targetRatio, width is the limiter, new height is width / targetRatio
         */

        float currentRatio = Math.round((float) areaWidth / (float) areaHeight * 1000.0f) / 1000.0f;
        float targetRatio = Math.round((float) widthRatio / (float) heightRatio * 1000.0f) / 1000.0f;

        AIMsLogger.DEBUG(String.format("Current ratio: %.2f\n\tTarget ratio: %.2f\n\tInput W:H: %d:%d", currentRatio, targetRatio, areaWidth, areaHeight));

        // Width is limiting factor, adjust height
        if (currentRatio < targetRatio) {
            int newHeight = Math.round((float) areaWidth / targetRatio);
            AIMsLogger.DEBUG(String.format("Width is limiter, adjusting height, new dims are: %d:%d", areaWidth, newHeight));
            return new Dimension(areaWidth, newHeight);
        }

        // Height is limiting facter, adjust width
        if (currentRatio > targetRatio) {
            int newWidth = Math.round((float) areaHeight * targetRatio);
            AIMsLogger.DEBUG(String.format("Height is limiter, adjusting height, new dims are: %d:%d", newWidth, areaHeight));
            return new Dimension(newWidth, areaHeight);
        }

        // Width and height match ideal ratio, return it
        AIMsLogger.DEBUG("Current ratio is ideal, returning input dimensions");
        return new Dimension(areaWidth, areaHeight);
    }

    public static Dimension calculate169(int areaWidth, int areaHeight) {
        return calculateRatio(areaWidth, areaHeight, 16, 9);
    }

    public static Dimension calculate43(int areaWidth, int areaHeight) {
        return calculateRatio(areaWidth, areaHeight, 4, 3);
    }



}
