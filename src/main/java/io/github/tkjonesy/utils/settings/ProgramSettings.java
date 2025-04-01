package io.github.tkjonesy.utils.settings;

import ai.onnxruntime.OrtSession;
import io.github.tkjonesy.frontend.App;
import io.github.tkjonesy.utils.annotations.SettingsLabel;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.ONNX.enums.InferenceLogEnum;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.HashMap;

@SuppressWarnings("unused")
@Getter
@ToString
public class ProgramSettings {

    @Getter
    @Setter
    private static ProgramSettings currentSettings;

    // Camera variables
    @SettingsLabel(value = "cameraDeviceId", type = Integer.class)
    private int cameraDeviceId = 0;
    @SettingsLabel(value = "cameraFps", type = Integer.class)
    private int cameraFps = 30;
    @SettingsLabel(value = "cameraRotation", type = Integer.class)
    private int cameraRotation = 0;
    @SettingsLabel(value = "mirrorCamera", type = Boolean.class)
    private boolean mirrorCamera;
    @SettingsLabel(value = "aspectRatio", type = String.class)
    private String aspectRatio = "4:3";

    // Storage variables
    @Setter
    @SettingsLabel(value = "fileDirectory", type = String.class)
    private String fileDirectory;
    @SettingsLabel(value = "saveVideo", type = Boolean.class)
    private boolean saveVideo;
    @SettingsLabel(value = "saveLogsTEXT", type = Boolean.class)
    private boolean saveLogsTEXT;
    @SettingsLabel(value = "saveLogsCSV", type = Boolean.class)
    private boolean saveLogsCSV;

    // AI settings
    @Setter
    @SettingsLabel(value = "modelPath", type = String.class)
    private String modelPath;
    @Setter
    @SettingsLabel(value = "labelPath", type = String.class)
    private String labelPath;
    @SettingsLabel(value = "boundingBoxColor", type = int[].class)
    private int[] boundingBoxColor = {255, 0, 0};
    @SettingsLabel(value = "logAddedColor", type = int[].class)
    private int[] logAddedColor = {0, 255, 0};
    @SettingsLabel(value = "logRemovedColor", type = int[].class)
    private int[] logRemovedColor = {255, 0, 0};
    @SettingsLabel(value = "showBoundingBoxes", type = Boolean.class)
    private boolean showBoundingBoxes;
    @SettingsLabel(value = "showLabels", type = Boolean.class)
    private boolean showLabels;
    @SettingsLabel(value = "showConfidences", type = Boolean.class)
    private boolean showConfidences;
    @SettingsLabel(value = "processEveryNthFrame", type = Integer.class)
    private int processEveryNthFrame = 30;
    @SettingsLabel(value = "bufferThreshold", type = Integer.class)
    private int bufferThreshold = 3;
    @SettingsLabel(value = "confThreshold", type = Float.class)
    private float confThreshold = 0.6f;
    @SettingsLabel(value = "logFontSize", type = Integer.class)
    private int logFontSize = 12;

    // Advanced AI settings
    @Setter
    @SettingsLabel(value = "useGPU", type = Boolean.class)
    private boolean useGPU;
    @SettingsLabel(value = "gpuDeviceId", type = Integer.class)
    private int gpuDeviceId = 0;
    @SettingsLabel(value = "nmsThreshold", type = Float.class)
    private float nmsThreshold = 0.45f;
    @SettingsLabel(value = "optimizationLevel", type = OrtSession.SessionOptions.OptLevel.class) // all, extended, basic, no
    private OrtSession.SessionOptions.OptLevel optimizationLevel = OrtSession.SessionOptions.OptLevel.ALL_OPT;
    @SettingsLabel(value = "numOnnxThreads", type = Integer.class)
    private int numOnnxThreads = 1;
    @SettingsLabel(value = "inputSize", type = Integer.class)
    private int inputSize = 640;
    @SettingsLabel(value = "inputShape", type = long[].class)
    private long[] inputShape = {1, 3, 640, 640};
    @SettingsLabel(value = "numInputElements", type = Integer.class)
    private int numInputElements;

    @SettingsLabel(value = "debugMode", type = Boolean.class)
    private boolean debugMode;
    @SettingsLabel(value = "continuousLogging", type = Boolean.class)
    private boolean continuousLogging = true;
    @SettingsLabel(value = "showInferenceTime", type = Boolean.class)
    private boolean showInferenceTime;

    // -------------------------------------------------------------------------

    public void updateSettings(HashMap<String, Object> newSettings) {
        boolean updateONNX = false, updateCamera = false, updateBuffer = false, updateDebug = false, updateLogColors = false, updateAspectRatio = false;

        for (String key : newSettings.keySet()) {
            setSettings(key, newSettings.get(key));

            if (key.equals("modelPath") || key.equals("labelPath") || key.equals("useGPU") || key.equals("gpuDeviceId") ||
                    key.equals("optimizationLevel") || key.equals("numOnnxThreads")) {
                updateONNX = true;
            }
            if (key.equals("cameraDeviceId")) {
                updateCamera = true;
            }
            if (key.equals("bufferThreshold")) {
                updateBuffer = true;
            }
            if (key.equals("debugMode") || key.equals("continuousLogging")) {
                updateDebug = true;
            }
            if (key.equals("logAddedColor") || key.equals("logRemovedColor")) {
                updateLogColors = true;
            }
            if (key.equals("aspectRatio")) {
                updateAspectRatio = true;
            }
        }
        if (updateONNX) {
            App.getOnnxRunner().updateInferenceSession(modelPath, labelPath);
        }
        if (updateBuffer) {
            App.getOnnxRunner().setBufferThreshold(bufferThreshold);
        }
        if (updateCamera) {
            App.getInstance().updateCamera((int) newSettings.get("cameraDeviceId"));
        }
        if (updateDebug) {
            AIMsLogger.initialize(this);
        }
        if (updateLogColors) {
            InferenceLogEnum.LOG_ADDED.updateColor(logAddedColor);
            InferenceLogEnum.LOG_REMOVED.updateColor(logRemovedColor);
        }
        if (updateAspectRatio) {
            App.getInstance().getCameraPanel().updateAspectRatio();
        }

        SettingsLoader.saveSettings(this);
    }

    private void setSettings(String label, Object value) {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(SettingsLabel.class)) {
                SettingsLabel annotation = field.getAnnotation(SettingsLabel.class);
                if (annotation.value().equals(label)) {
                    field.setAccessible(true);
                    try {
                        if (annotation.type().isInstance(value)) {
                            field.set(this, value);
                        } else {
                            AIMsLogger.ERROR("Type mismatch: Cannot assign " +
                                    value.getClass().getSimpleName() + " to " +
                                    annotation.type().getSimpleName());
                        }
                    } catch (IllegalAccessException e) {
                        AIMsLogger.ERROR("Failed to set value for " + label + ": " + e.getMessage());
                    }
                    return;
                }
            }
        }
    }
}