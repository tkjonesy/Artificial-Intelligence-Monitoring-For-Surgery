package io.github.tkjonesy.frontend.utils.cameraGrabber;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic fallback implementation of CameraGrabber.
 * Used when a platform-specific implementation isn't available.
 */
public class GenericCameraGrabber extends CameraGrabber{
    @Override
    protected List<String> getPlatformCameraNames() {
        return new ArrayList<>();
    }
}
