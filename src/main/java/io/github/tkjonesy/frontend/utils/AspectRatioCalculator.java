package io.github.tkjonesy.frontend.utils;

import java.awt.*;

public class AspectRatioCalculator {

    public static Dimension calculateRatio(int areaWidth, int areaHeight, int widthRatio, int heightRatio) {

        float currentRatio = Math.round((float) areaWidth / (float) areaHeight * 100.0f) / 100.0f;
        float targetRatio = Math.round((float) widthRatio / (float) heightRatio * 100.0f) / 100.0f;

        // Width is limiting factor, adjust height

        // Height is limiting factor, adjust width

        // Width and height match ideal ratio, return it
        return new Dimension(areaWidth, areaHeight);
    }

    public static Dimension calculate169(int areaWidth, int areaHeight) {
        return calculateRatio(areaWidth, areaHeight, 16, 9);
    }

    public static Dimension calculate43(int areaWidth, int areaHeight) {
        return calculateRatio(areaWidth, areaHeight, 4, 3);
    }



}
