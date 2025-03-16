package io.github.tkjonesy.frontend.utils.cameraGrabber;

import io.github.tkjonesy.utils.logging.AIMsLogger;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MacOSCameraGrabber extends CameraGrabber {


//    /**
//     * Gets camera names from system_profiler (MacOS only).
//     */
//    @Override
//    protected List<String> getPlatformCameraNames() {
//
//        List<String> cameraNames = new ArrayList<>();
//        try {
//            ProcessBuilder processBuilder = new ProcessBuilder("system_profiler", "SPCameraDataType");
//            processBuilder.redirectErrorStream(true);
//            Process process = processBuilder.start();
//
//            if(!process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS)) {
//                process.destroyForcibly();
//                throw new RuntimeException("Timed out waiting for system_profiler to complete.");
//            }
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//            while((line = reader.readLine()) !=null){
//                line = line.trim();
//                if (line.startsWith("Model ID:") || line.startsWith("Camera Name:")) {
//                    String cameraName = line.split(":", 2)[1].trim();
//                    if (!cameraName.isEmpty()) {
//                        cameraNames.add(cameraName);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            AIMsLogger.FATAL("Error retrieving camera names from system_profiler: " + e.getMessage());
//        }
//        return cameraNames;
//    }
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
