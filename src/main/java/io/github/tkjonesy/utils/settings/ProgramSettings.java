package io.github.tkjonesy.utils.settings;

import ai.onnxruntime.OrtSession;
import io.github.tkjonesy.frontend.App;
import io.github.tkjonesy.utils.annotations.SettingsLabel;
import io.github.tkjonesy.utils.logging.AIMsLogger;
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
    private int cameraDeviceId;
    @SettingsLabel(value = "cameraFps", type = Integer.class)
    private int cameraFps;
    @SettingsLabel(value = "cameraRotation", type = Integer.class)
    private int cameraRotation;
    @SettingsLabel(value = "mirrorCamera", type = Boolean.class)
    private boolean mirrorCamera;
    @SettingsLabel(value = "preserveAspectRatio", type = Boolean.class)
    private boolean preserveAspectRatio;

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
    private int[] boundingBoxColor;
    @SettingsLabel(value = "showBoundingBoxes", type = Boolean.class)
    private boolean showBoundingBoxes;
    @SettingsLabel(value = "showLabels", type = Boolean.class)
    private boolean showLabels;
    @SettingsLabel(value = "showConfidences", type = Boolean.class)
    private boolean showConfidences;
    @SettingsLabel(value = "processEveryNthFrame", type = Integer.class)
    private int processEveryNthFrame;
    @SettingsLabel(value = "bufferThreshold", type = Integer.class)
    private int bufferThreshold;
    @SettingsLabel(value = "confThreshold", type = Float.class)
    private float confThreshold;

    // Advanced AI settings
    @Setter
    @SettingsLabel(value = "useGPU", type = Boolean.class)
    private boolean useGPU;
    @SettingsLabel(value = "gpuDeviceId", type = Integer.class)
    private int gpuDeviceId;
    @SettingsLabel(value = "nmsThreshold", type = Float.class)
    private float nmsThreshold;
    @SettingsLabel(value = "optimizationLevel", type = OrtSession.SessionOptions.OptLevel.class) // all, extended, basic, no
    private OrtSession.SessionOptions.OptLevel optimizationLevel;
    @SettingsLabel(value = "inputSize", type = Integer.class)
    private int inputSize;
    @SettingsLabel(value = "inputShape", type = long[].class)
    private long[] inputShape;
    @SettingsLabel(value = "numInputElements", type = Integer.class)
    private int numInputElements;

    @SettingsLabel(value = "debugMode", type = Boolean.class)
    private boolean debugMode;
    @SettingsLabel(value = "continuousLogging", type = Boolean.class)
    private boolean continuousLogging;
    @SettingsLabel(value = "showInferenceTime", type = Boolean.class)
    private boolean showInferenceTime;

    @SettingsLabel(value = "notZeroNum", type = Integer.class)
    private int notZeroNum;

    // -------------------------------------------------------------------------

    public void updateSettings(HashMap<String, Object> newSettings) {
        boolean updateONNX = false, updateCamera = false, updateBuffer = false, updateDebug = false;
        for (String key : newSettings.keySet()) {
            setSettings(key, newSettings.get(key));
            if(key.equals("modelPath") || key.equals("labelPath") || key.equals("useGPU")){
                updateONNX = true;
            }
            if(key.equals("cameraDeviceId")){
                updateCamera = true;
            }
            if(key.equals("bufferThreshold")){
                updateBuffer = true;
            }
            if(key.equals("debugMode") || key.equals("continuousLogging")){
                updateDebug = true;
            }
        }
        if(updateONNX){
            App.getOnnxRunner().updateInferenceSession(modelPath, labelPath);
        }
        if(updateBuffer){
            App.getOnnxRunner().setBufferThreshold(bufferThreshold);
        }
        if(updateCamera){
            App.getInstance().updateCamera((int)newSettings.get("cameraDeviceId"));
        }
        if(updateDebug){
            AIMsLogger.initialize(this);
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
