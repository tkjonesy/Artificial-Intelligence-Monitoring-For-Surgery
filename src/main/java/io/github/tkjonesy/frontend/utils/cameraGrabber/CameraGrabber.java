package io.github.tkjonesy.frontend.utils.cameraGrabber;

import io.github.tkjonesy.utils.logging.AIMsLogger;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for camera detection across different platforms.
 * Provides a unified approach to camera detection with platform-specific
 * optimizations handled by subclasses.
 */
public abstract class CameraGrabber {

    private static final int MAX_CAMERAS_TO_CHECK = 10;
    private static final int CONNECTION_TIMEOUT_MS = 2000; // Increased timeout
    private static final String osName = System.getProperty("os.name").toLowerCase();

    /**
     * Creates the appropriate CameraGrabber for the current operating system.
     *
     * @return A platform-appropriate CameraGrabber instance
     */
    public static CameraGrabber createForPlatform() {

        if (osName.contains("win")) {
            return new WindowsCameraGrabber();
        } else if (osName.contains("mac")) {
            return new MacOSCameraGrabber();
        } else if (osName.contains("linux") || osName.contains("unix")) {
            return new LinuxCameraGrabber();
        } else {
            // Default to generic implementation if OS is unknown
            AIMsLogger.WARN("Unknown operating system: " + osName + ". Using default camera detection.");
            return new GenericCameraGrabber();
        }
    }

    /**
     * Returns a mapping of camera names to their corresponding device indices.
     * This method is the main entry point for camera detection.
     *
     * @return A HashMap mapping camera names to their device indices
     */
    public HashMap<String, Integer> getCameraNames() {
        // First, quickly find available camera indices
        List<Integer> cameraIndices = findAvailableCameraIndices();

        // If no cameras found, return empty map
        if (cameraIndices.isEmpty()) {
            AIMsLogger.WARN("No cameras detected");
            return new HashMap<>();
        }

        // Then map these indices to names
        return getCameraNamesFromIndices(cameraIndices);
    }

    /**
     * Maps discovered camera indices to human-readable names.
     * Uses platform-specific naming when available.
     *
     * @param cameraIndices List of available camera indices
     * @return HashMap mapping camera names to their indices
     */
    private HashMap<String, Integer> getCameraNamesFromIndices(List<Integer> cameraIndices){
        // Try to get platform-specific names
        List<String> platformSpecificNames = getPlatformCameraNames();
        HashMap<String, Integer> cameraMap = new HashMap<>();

        // Map indices to names
        for (int i = 0; i <cameraIndices.size(); i++) {
            int index;
            if(osName.contains("mac")) {
                index = cameraIndices.get(cameraIndices.size() - 1 - i);
            }else{
                index = cameraIndices.get(i);
            }

            // Use platform-specific name if available, otherwise use a generic name
            String cameraName;
            if (i < platformSpecificNames.size() && !platformSpecificNames.get(i).isEmpty()) {
                cameraName = platformSpecificNames.get(i);
            } else {
                cameraName = "Camera " + index;
            }

            AIMsLogger.TRACE("Mapping camera index " + index + " to name: " + cameraName);
            cameraMap.put(cameraName, index);
        }
        return cameraMap;
    }

    /**
     * Efficiently discovers available camera indices by checking
     * multiple cameras in parallel with timeouts.
     *
     * @return List of available camera indices
     */
    private List<Integer> findAvailableCameraIndices() {
        List<Integer> validIndices = new ArrayList<>();

        // First try the simple approach which is faster and more reliable in most cases
        int i=0;
        while(i<MAX_CAMERAS_TO_CHECK && isCameraPresent(i)) {
            validIndices.add(i);
            AIMsLogger.TRACE("Camera found at index: " + i);
            i++;
        }

        // If that didn't find any cameras, try the more thorough approach
        if (validIndices.isEmpty()) {
            AIMsLogger.INFO("No cameras found with quick check, trying thorough check...");
            validIndices = findCamerasThoroughly();
        }

        return validIndices;
    }

    /**
     * More thorough method to find cameras by actually trying to start
     * the grabber and capture a frame.
     */
    private List<Integer> findCamerasThoroughly() {
        List<Integer> validIndices = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CAMERAS_TO_CHECK);
        List<Future<CameraCheckResult>> futures = new ArrayList<>();

        // Submit camera check tasks
        for (int i = 0; i < MAX_CAMERAS_TO_CHECK; i++) {
            futures.add(executor.submit(new CameraChecker(i)));
        }

        // Collect results
        for (int i = 0; i < futures.size(); i++) {
            try {
                CameraCheckResult result = futures.get(i).get(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (result.isAvailable) {
                    validIndices.add(i);
                    AIMsLogger.TRACE("Camera found at index: " + i);
                }
            } catch (Exception e) {
                // Camera check timed out or failed - skip this index
                AIMsLogger.TRACE("Camera check failed for index " + i + ": " + e.getMessage());
            }
        }

        executor.shutdown();
        return validIndices;
    }

    /**
     * Quick check if a camera is present at a specific index.
     * This method is optimized for speed.
     *
     * @param index The index to check
     * @return true if a camera is present, false otherwise
     */
    protected boolean isCameraPresent(int index) {
        try(VideoCapture capture = new VideoCapture(index)) {
            boolean isOpened = capture.isOpened();

            if (isOpened) {
                org.bytedeco.opencv.opencv_core.Mat testFrame = new org.bytedeco.opencv.opencv_core.Mat();
                // Set a short timeout for read operation
                capture.set(org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_BUFFERSIZE, 1);

                // Try to read a frame with timeout
                long startTime = System.currentTimeMillis();
                boolean frameRead = false;

                while (System.currentTimeMillis() - startTime < 300) {
                    if (capture.read(testFrame)) {
                        frameRead = !testFrame.empty();
                        if (frameRead) break;
                    }
                }

                testFrame.release();
                capture.release();
                return frameRead;
            }

            capture.release();
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Abstract method to be implemented by platform-specific subclasses.
     * Should return a list of descriptive names for detected cameras.
     *
     * @return List of camera names, which may be empty if platform-specific detection fails
     */
    protected abstract List<String> getPlatformCameraNames();

        /**
         * Helper class to check if a camera is available at a specific index
         */
        private record CameraChecker(int index) implements Callable<CameraCheckResult> {

            @Override
            public CameraCheckResult call() {
                // First try with OpenCVFrameGrabber
                try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(index)) {
                    grabber.setFormat("mjpeg");
                    grabber.setImageWidth(320);
                    grabber.setImageHeight(240);
                    grabber.start();

                    // Try to grab a frame
                    boolean success = false;
                    for (int attempt = 0; attempt < 3; attempt++) {
                        try {
                            if (grabber.grab() != null) {
                                success = true;
                                break;
                            }
                        } catch (Exception ignore) {}
                    }

                    grabber.stop();
                    grabber.release();

                    if (success) {
                        return new CameraCheckResult(true);
                    }
                } catch (Exception e) {
                    AIMsLogger.TRACE("OpenCVFrameGrabber failed for index " + index + ": " + e.getMessage());
                }

                // Fallback to VideoCapture approach
                try(VideoCapture capture = new VideoCapture(index)) {
                    if (capture.isOpened()) {
                        org.bytedeco.opencv.opencv_core.Mat frame = new org.bytedeco.opencv.opencv_core.Mat();
                        boolean readSuccess = capture.read(frame);
                        boolean notEmpty = !frame.empty();
                        frame.release();
                        capture.release();

                        if (readSuccess && notEmpty) {
                            return new CameraCheckResult(true);
                        }
                    }
                } catch (Exception e) {
                    AIMsLogger.TRACE("Both detection methods failed for index " + index);
                }

                return new CameraCheckResult(false);
            }
        }

        /**
         * Simple result class for camera availability check
         */
        private record CameraCheckResult(boolean isAvailable) {}
}