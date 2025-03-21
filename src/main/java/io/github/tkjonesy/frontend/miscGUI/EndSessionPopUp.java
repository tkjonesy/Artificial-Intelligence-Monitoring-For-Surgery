package io.github.tkjonesy.frontend.miscGUI;

import io.github.tkjonesy.utils.logging.AIMsLogger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Utility class to display a popup at the end of a session,
 * prompting the user to open the sessions directory.
 */
public class EndSessionPopUp {

    /**
     * Displays a confirmation popup asking the user if they want to open the specific session directory.@param sessionTitle The title of the session, used to locate the session folder.
     */
    public static void showSessionEndDialog(String title, String duration, int peakSeen,
                                            HashMap<String, Integer> finalToolCounts,
                                            HashMap<String, Integer> totalToolsAdded,
                                            HashMap<String, Integer> toolsRemoved,
                                            String sessionDirectory) {
        SwingUtilities.invokeLater(() -> {
            Object[] options = {"Open session folder", "Cancel"};

            StringBuilder miniAAR = new StringBuilder();
            miniAAR.append("Session Name: ").append(title).append("\n");
            miniAAR.append("Recording Duration: ").append(duration).append("\n");
            miniAAR.append("Peak Objects Seen at Once: ").append(peakSeen).append("\n\n");

            miniAAR.append("Total Instances of Each Object Ever Added:\n");
            miniAAR.append("-----------------------------------------------------\n");
            if (totalToolsAdded.isEmpty()) {
                miniAAR.append("None\n");
            } else {
                for (var entry : totalToolsAdded.entrySet()) {
                    miniAAR.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }
            miniAAR.append("-----------------------------------------------------\n\n");

            miniAAR.append("Objects Present at End:\n");
            miniAAR.append("------------------------\n");
            if (finalToolCounts.isEmpty()) {
                miniAAR.append("None\n");
            } else {
                for (var entry : finalToolCounts.entrySet()) {
                    miniAAR.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }
            miniAAR.append("------------------------\n\n");

            miniAAR.append("Objects Removed During Session:\n");
            miniAAR.append("-----------------------------------------------------\n");
            if (toolsRemoved.isEmpty()) {
                miniAAR.append("None\n");
            } else {
                for (var entry : toolsRemoved.entrySet()) {
                    miniAAR.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }
            miniAAR.append("-----------------------------------------------------\n\n");

            int choice = JOptionPane.showOptionDialog(null,
                    miniAAR.toString(),
                    "Session Ended",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]
            );

            if (choice == JOptionPane.YES_OPTION) {
                openSessionDirectory(sessionDirectory);
            }
        });
    }

    /**
     * Opens the specific session directory based on the session title.
     * @param sessionTitle The full path of the session directory.
     */
    private static void openSessionDirectory(String sessionTitle) {
        File directory = new File(sessionTitle);
        AIMsLogger.TRACE("Attempting to open: " + directory.getAbsolutePath());

        if (!directory.exists() || !directory.isDirectory()) {
            JOptionPane.showMessageDialog(null, "Error: The session folder does not exist: " + directory.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Desktop.getDesktop().open(directory);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error: Failed to open the session folder: " + directory.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
