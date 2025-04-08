package io.github.tkjonesy.utils.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

import static io.github.tkjonesy.utils.Paths.*;

/**
 * Class responsible for loading and saving the settings
 */
public class SettingsLoader {

    // Default model to use if none is specified
    private static final String DEFAULT_MODEL = "yolo11m";
    @Getter
    private static final ProgramSettings DEFAULT_SETTINGS = loadSettingsFromResource(new ObjectMapper());

    public static void resetToDefaultSettings(){
        // Save the default settings to the file
        if(DEFAULT_SETTINGS != null){
            saveSettings(DEFAULT_SETTINGS);
            ProgramSettings.setCurrentSettings(DEFAULT_SETTINGS);
            saveSettings(DEFAULT_SETTINGS);
            AIMsLogger.WARN("Reset settings to default.");
        }
    }

    /**
     * Loads program settings
     * @return A valid {@link ProgramSettings} object.
     */
    public static ProgramSettings loadSettings(){
        // Load settings from the Settings file
        ObjectMapper objectMapper = new ObjectMapper();
        ProgramSettings settings = loadSettingsFromFile(objectMapper);

        // If settings are null, load default settings from resources
        if(settings == null){
            AIMsLogger.WARN("No settings file found, loading default settings.");
            settings = loadSettingsFromResource(objectMapper);
        }

        AIMsLogger.TRACE("Loaded settings." + settings);
        // Save the settings to the file, then verify that the specified model and label files exist
        if(settings != null){

            // Ensure all the values in the current settings are set. Compare to default settings.
            // If not set, assign default values.
//            if (DEFAULT_SETTINGS != null) {
//                sanitizeBrokenFields(settings);
//            }

            if(settings.getFileDirectory() == null){
                settings.setFileDirectory(DEFAULT_AIMS_SESSIONS_DIRECTORY);
            }

            saveSettings(settings);
            verifyModelAndLabels(settings);
        }

        return settings;
    }

    /**
     * Creates AIMs directory if it does not exist
     */
    public static void initializeAIMsDirectories() {
        try {
            Path parentDirectory = Paths.get(AIMS_DIRECTORY);
            if (!Files.exists(parentDirectory)) {
                Files.createDirectories(parentDirectory);
            }

            Path modelsDirectory = Paths.get(AIMS_MODELS_DIRECTORY);
            if (!Files.exists(modelsDirectory)) {
                Files.createDirectories(modelsDirectory);
            }

            Path settingsDirectory = Paths.get(AIMS_SESSIONS_DIRECTORY);
            if (!Files.exists(settingsDirectory)) {
                Files.createDirectories(settingsDirectory);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to create required directories: " + AIMS_DIRECTORY, e);
        }
    }

    /**
     * Loads settings from settings file
     * @param objectMapper The {@link ObjectMapper} instance used to parse the settings file
     * @return {@link ProgramSettings} object containing the loaded settings, or null if an error occurs
     */
    private static ProgramSettings loadSettingsFromFile(ObjectMapper objectMapper) {
        File settingsFile = new File(AIMS_SETTINGS_FILE_PATH);
        if (settingsFile.exists()) {
            try {
                return objectMapper.readValue(settingsFile, ProgramSettings.class);
            } catch (IOException e) {
                AIMsLogger.ERROR("Failed to load settings from file: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Loads settings from resource file
     * @param objectMapper The {@link ObjectMapper} instance used to parse the resource
     * @return {@link ProgramSettings} object containing the loaded settings, or null if an error occurs
     */
    private static ProgramSettings loadSettingsFromResource(ObjectMapper objectMapper) {
        try (InputStream inputStream = SettingsLoader.class.getResourceAsStream(RESOURCE_DEFAULT_SETTINGS_PATH)) {
            if (inputStream != null) {
                return objectMapper.readValue(inputStream, ProgramSettings.class);
            } else {
                AIMsLogger.FATAL("Default settings file not found in resources.");
            }
        } catch (IOException e) {
            AIMsLogger.FATAL("Failed to load default settings: " + e.getMessage());
        }
        return null;
    }

    /**
     * Saves the program settings to the settings file
     * @param settings The {@link ProgramSettings} object to be saved.
     */
    public static void saveSettings(ProgramSettings settings){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(AIMS_SETTINGS_FILE_PATH), settings);
        } catch (IOException e) {
            AIMsLogger.FATAL("Failed to save settings: " + e.getMessage());
        }
    }

    /**
     * Verifies if the model and label files specified in the settings exist
     * If they do not exist, extracts from the bundled resource
     * @param settings The {@link ProgramSettings} object containing the paths to the model and label files
     */
    private static void verifyModelAndLabels(ProgramSettings settings) {
        String modelPath = settings.getModelPath();
        String labelPath = settings.getLabelPath();

        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            AIMsLogger.WARN("Model file not found, extracting default model.");
            modelPath = extractResourceIfMissing(RESOURCE_DEFAULT_MODEL_PATH, AIMS_MODELS_DIRECTORY + "/" + DEFAULT_MODEL + ".onnx");
            settings.setModelPath(modelPath);
        }

        File labelFile = new File(labelPath);
        if (!labelFile.exists()) {
            AIMsLogger.WARN("Label file not found, extracting default labels.");
            labelPath = extractResourceIfMissing(RESOURCE_DEFAULT_LABELS_PATH, AIMS_MODELS_DIRECTORY + "/" + DEFAULT_MODEL + ".names");
            settings.setLabelPath(labelPath);
        }
    }

    /**
     * Extracts resource file to target path if it doesnt exist
     * @param resourcePath Path to the resource file
     * @param targetPath Path where the resource should be extracted
     * @return Path to extracted resource file
     */
    private static String extractResourceIfMissing(String resourcePath, String targetPath) {
        File targetFile = new File(targetPath);

        if (!targetFile.exists()) {
            try (InputStream in = SettingsLoader.class.getResourceAsStream(resourcePath)) {
                if (in == null) {
                    throw new IOException("Resource not found inside JAR: " + resourcePath);
                }
                Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                AIMsLogger.TRACE("Extracted resource: " + resourcePath + " -> " + targetPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to extract required resource: " + resourcePath, e);
            }
        }

        return targetPath;
    }
}
