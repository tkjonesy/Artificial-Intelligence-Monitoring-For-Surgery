package io.github.tkjonesy.utils;

import io.github.tkjonesy.utils.logging.AIMsLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.github.tkjonesy.utils.Paths.AIMS_ERRORS_DIRECTORY;

public class ErrorUtils {

    /**
     * Saves the exception to a file in the error directory.
     * @param e The exception to save
     */
    public static void saveExceptionToFile(Exception e) {
        try{
            // Get the error directory
            Path errorsDirectory = Paths.get(AIMS_ERRORS_DIRECTORY);
            if (!Files.exists(errorsDirectory)) {
                Files.createDirectories(errorsDirectory);
            }

            String timeStamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            Path errorFilePath = errorsDirectory.resolve(timeStamp + ".err");

            try (PrintWriter printWriter = new PrintWriter(new FileWriter(errorFilePath.toFile(), true))) {
                printWriter.println("========== UNKNOWN ERROR OCCURRED ==========");
                printWriter.println("Version: " + AppVersion.getCOMMIT_ID_FULL());
                printWriter.println("Exception: " + e.getClass().getName());
                printWriter.println("Message: " + e.getMessage());
                printWriter.println("Stack Trace:");
                e.printStackTrace(printWriter);
                printWriter.println("=============================================");
                printWriter.println("If the cause or fix for this issue is not clear, please open an issue ticket at https://github.com/tkjonesy/Artificial-Intelligence-Monitoring-For-Surgery");
            }
        }catch (IOException ex){
            AIMsLogger.ERROR("Failed to save error to file: " + ex.getMessage());
        }
    }

    /**
     * Saves the console output to a file in the error directory.
     * @param consoleOutput The console output to save
     */
    public static void saveConsoleToFile(String consoleOutput) {
        try {
            // Get the error directory
            Path errorsDirectory = Paths.get(AIMS_ERRORS_DIRECTORY);
            if (!Files.exists(errorsDirectory)) {
                Files.createDirectories(errorsDirectory);
            }

            String timeStamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            Path errorFilePath = errorsDirectory.resolve(timeStamp + ".log");

            try (PrintWriter printWriter = new PrintWriter(new FileWriter(errorFilePath.toFile(), true))) {
                printWriter.println("========== CONSOLE OUTPUT ==========");
                printWriter.println("Version: " + AppVersion.getCOMMIT_ID_FULL());
                printWriter.println(consoleOutput);
                printWriter.println("=====================================");
            }
            DialogManager.displayInfoDialog("Console output saved to: " + errorFilePath.toAbsolutePath());
        } catch (IOException ex) {
            AIMsLogger.ERROR("Failed to save console output to file: " + ex.getMessage());
        }
    }

}
