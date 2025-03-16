package io.github.tkjonesy.frontend.utils.cameraGrabber;

import io.github.tkjonesy.utils.logging.AIMsLogger;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Windows-specific implementation of the CameraGrabber.
 * Uses DirectShow names when available.
 */
public class WindowsCameraGrabber extends CameraGrabber {

    /**
     * Gets camera names using Windows-specific methods.
     */
    @Override
    protected List<String> getPlatformCameraNames() {
        List<String> cameras = new ArrayList<>();
        try {
            String[] deviceDescriptions = VideoInputFrameGrabber.getDeviceDescriptions();
            for (int i = 0; i < deviceDescriptions.length; i++) {
                AIMsLogger.INFO("Camera " + i + ": " + deviceDescriptions[i]);
                cameras.add(deviceDescriptions[i]);
            }
        }catch (FrameGrabber.Exception e) {
            AIMsLogger.FATAL("Error getting camera names: " + e.getMessage());
        }


        return cameras;
    }
}