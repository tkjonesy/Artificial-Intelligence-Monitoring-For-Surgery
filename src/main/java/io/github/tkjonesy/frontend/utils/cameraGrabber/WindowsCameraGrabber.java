package io.github.tkjonesy.frontend.utils.cameraGrabber;

import io.github.tkjonesy.utils.logging.AIMsLogger;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        AIMsLogger.TRACE("Getting camera names using Windows-specific methods");
        List<String> cameras = new ArrayList<>();
        try {
            String[] deviceDescriptions = VideoInputFrameGrabber.getDeviceDescriptions();
            cameras.addAll(Arrays.asList(deviceDescriptions));
        }catch (FrameGrabber.Exception e) {
            AIMsLogger.FATAL("Error getting camera names: " + e.getMessage());
        }


        return cameras;
    }
}