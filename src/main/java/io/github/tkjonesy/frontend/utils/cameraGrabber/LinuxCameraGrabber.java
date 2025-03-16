package io.github.tkjonesy.frontend.utils.cameraGrabber;

import io.github.tkjonesy.utils.logging.AIMsLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Linux-specific implementation of the CameraGrabber.
 * Uses v4l2-ctl or /sys/class/video4linux for device info.
 */
public class LinuxCameraGrabber extends CameraGrabber {

    /**
     * Gets camera names from Linux-specific methods.
     * Tries multiple approaches to get camera names.
     */
    @Override
    protected List<String> getPlatformCameraNames() {
        List<String> cameraNames = new ArrayList<>();

        // First try using v4l2-ctl if available (most reliable)
        if (cameraNames.isEmpty()) {
            cameraNames = getV4L2CameraNames();
        }

        // If that didn't work, try reading from sysfs
        if (cameraNames.isEmpty()) {
            cameraNames = getSysfsCameraNames();
        }

        return cameraNames;
    }

    /**
     * Attempt to get camera names using v4l2-ctl command
     */
    private List<String> getV4L2CameraNames() {
        List<String> cameraNames = new ArrayList<>();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("v4l2-ctl", "--list-devices");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new RuntimeException("v4l2-ctl command timed out");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String currentCamera = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (!line.startsWith("/dev/video")) {
                        // This is a camera name
                        currentCamera = line.replace(":", "").trim();
                    } else if (currentCamera != null) {
                        // This is a device node, only add the camera once
                        cameraNames.add(currentCamera);
                        currentCamera = null;
                    }
                }
            }
        } catch (Exception e) {
            AIMsLogger.WARN("Error using v4l2-ctl to list cameras: " + e.getMessage());
        }

        return cameraNames;
    }

    /**
     * Attempt to get camera names from sysfs
     */
    private List<String> getSysfsCameraNames() {
        List<String> cameraNames = new ArrayList<>();
        File videoDir = new File("/sys/class/video4linux");

        if (videoDir.exists() && videoDir.isDirectory()) {
            File[] videoDev = videoDir.listFiles();
            if (videoDev != null) {
                for (File device : videoDev) {
                    try {
                        File nameFile = new File(device, "name");
                        if (nameFile.exists()) {
                            try (BufferedReader reader = new BufferedReader(new FileReader(nameFile))) {
                                String name = reader.readLine();
                                if (name != null && !name.isEmpty()) {
                                    cameraNames.add(name.trim());
                                }
                            }
                        }
                    } catch (Exception e) {
                        AIMsLogger.WARN("Error reading camera name from sysfs: " + e.getMessage());
                    }
                }
            }
        }

        return cameraNames;
    }
}