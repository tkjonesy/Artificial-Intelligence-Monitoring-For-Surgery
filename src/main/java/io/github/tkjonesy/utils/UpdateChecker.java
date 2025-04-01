package io.github.tkjonesy.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tkjonesy.frontend.App;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.utils.settings.ProgramSettings;
import lombok.AllArgsConstructor;
import lombok.ToString;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {

    private static final String REPO_OWNER = "tkjonesy";
    private static final String REPO_NAME = "Artificial-Intelligence-Monitoring-For-Surgery";
    private static final String RELEASES_API_URL = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/releases/latest";

    /**
     * Asynchronously checks for updates in the background
     */
    public static void checkForUpdatesAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                checkForUpdates();
            } catch (Exception e) {
                AIMsLogger.ERROR("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    /**
     * Checks for updates and displays a popup if a newer version is available
     */
    public static void checkForUpdates() throws IOException {
        String currentCommitHash = AppVersion.getCOMMIT_ID_FULL();
        ReleaseInfo latestRelease = getLatestReleaseFromGitHub();

        if (latestRelease != null) {
            // Get the commit hash for the tag
            String releaseCommitHash = getCommitHashForTag(latestRelease.tagName);
            AIMsLogger.INFO("Latest commit hash: " + releaseCommitHash);
            AIMsLogger.INFO("Current commit hash: " + currentCommitHash);

            if (releaseCommitHash != null && isCurrentCommitBehind(currentCommitHash, releaseCommitHash)) {
                latestRelease.commitHash = releaseCommitHash;
                AIMsLogger.INFO("New version available: " + latestRelease.commitHash);
                showUpdateAvailableDialog(latestRelease);
            }else {
                AIMsLogger.INFO("Current commit is not behind the latest release commit. No update needed.");
            }
        }
    }

    /**
     * Retrieves the latest release information from GitHub
     * @return ReleaseInfo object containing version and download URL, or null if unable to retrieve
     */
    private static ReleaseInfo getLatestReleaseFromGitHub() throws IOException {
        URL url = new URL(RELEASES_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("User-Agent", "AIMs Update Checker");

        if (connection.getResponseCode() != 200) {
            AIMsLogger.ERROR("Failed to fetch latest release. HTTP error code: " + connection.getResponseCode());
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.toString());

            String tagName = rootNode.path("tag_name").asText();
            String htmlUrl = rootNode.path("html_url").asText();
            ReleaseInfo releaseInfo = new ReleaseInfo(tagName, null, htmlUrl);
            AIMsLogger.INFO("Latest release: " + releaseInfo);
            return releaseInfo;
        }
    }

    /**
     * Gets the commit hash associated with a tag
     * @param tagName The tag name to lookup
     * @return The commit hash or null if not found
     */
    private static String getCommitHashForTag(String tagName) throws IOException {
        // API endpoint to get a specific tag
        String tagUrl = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/git/refs/tags/" + tagName;

        URL url = new URL(tagUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("User-Agent", "AIMs Update Checker");

        if (connection.getResponseCode() != 200) {
            AIMsLogger.ERROR("Failed to fetch tag information. HTTP error code: " + connection.getResponseCode());
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.toString());

            // For an annotated tag, we need an extra step
            String objectType = rootNode.path("object").path("type").asText();

            if ("tag".equals(objectType)) {
                // This is an annotated tag, we need to get the commit it points to
                String tagObjectUrl = rootNode.path("object").path("url").asText();
                return getCommitHashFromAnnotatedTag(tagObjectUrl);
            } else {
                // This is a lightweight tag that points directly to a commit
                AIMsLogger.TRACE(rootNode.toString());
                return rootNode.path("object").path("sha").asText();
            }
        }
    }

    private static boolean isCurrentCommitBehind(String currentCommitHash, String releaseCommitHash) throws IOException {
        // Use GitHub's compare API to determine which commit is ahead
        String compareUrl = "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME +
                "/compare/" + releaseCommitHash + "..." + currentCommitHash;

        URL url = new URL(compareUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("User-Agent", "AIMs Update Checker");

        if (connection.getResponseCode() != 200) {
            AIMsLogger.ERROR("Failed to compare commits. HTTP error code: " + connection.getResponseCode());
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.toString());

            // The status field tells us the relationship between the two commits
            // "behind" means currentCommit is behind releaseCommit (an update is available)
            // "ahead" means currentCommit is ahead of releaseCommit (you're on a newer version)
            // "identical" means they're the same commit
            // "diverged" means they're on different branches with no direct relationship

            String status = rootNode.path("status").asText();
            int behindBy = rootNode.path("behind_by").asInt();
            int aheadBy = rootNode.path("ahead_by").asInt();

            if("identical".equals(status)){
                AIMsLogger.INFO("Current commit is identical to the latest release commit.");
            } else if("ahead".equals(status)){
                AIMsLogger.INFO("Current commit is ahead of the latest release commit by " + aheadBy + " commits.");
            } else if("diverged".equals(status)){
                AIMsLogger.INFO("Current commit has diverged from the latest release commit.");
            } else {
                AIMsLogger.INFO("Current commit is behind the latest release commit by " + behindBy + " commits.");
            }
            return "behind".equals(status) && behindBy > 0;
        }
    }

    /**
     * Gets the commit hash from an annotated tag object
     */
    private static String getCommitHashFromAnnotatedTag(String tagObjectUrl) throws IOException {
        URL url = new URL(tagObjectUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("User-Agent", "AIMs Update Checker");

        if (connection.getResponseCode() != 200) {
            AIMsLogger.ERROR("Failed to fetch tag object. HTTP error code: " + connection.getResponseCode());
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.toString());

            return rootNode.path("object").path("sha").asText();
        }
    }

    /**
     * Shows a dialog notifying the user of an available update
     */
    private static void showUpdateAvailableDialog(ReleaseInfo releaseInfo) {

        if(releaseInfo.commitHash.equals(ProgramSettings.getCurrentSettings().getSkipVersion())) return;

        SwingUtilities.invokeLater(() -> {
            String message = "A new version of AIMs is available!\n\n" +
                    "Current version: " + AppVersion.getCOMMIT_ID_ABBREV() + "\n" +
                    "Latest version: " + releaseInfo.commitHash.substring(0, 8) + "\n\n" +
                    "Would you like to open the download page?";

            // Create custom button options
            Object[] options = {"Download", "Remind Later", "Skip This Version"};

            int option = JOptionPane.showOptionDialog(
                    App.getInstance(),
                    message,
                    "Update Available",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (option == 0) {
                try {
                    Desktop.getDesktop().browse(new URI(releaseInfo.downloadUrl));
                } catch (Exception e) {
                    AIMsLogger.ERROR("Failed to open browser: " + e.getMessage());
                    DialogManager.displayErrorDialog("Could not open the browser. Please visit: " + releaseInfo.downloadUrl);
                }
            } else if (option == 2) {
                ProgramSettings settings = ProgramSettings.getCurrentSettings();
                settings.setSkipVersion(releaseInfo.commitHash);

                AIMsLogger.INFO("User chose to skip version: " + releaseInfo.commitHash.substring(0, 8));
            }
        });
    }


    /**
     * Helper class to store release information
     */
    @AllArgsConstructor
    @ToString
    private static class ReleaseInfo {
        private final String tagName;
        private String commitHash;
        private final String downloadUrl;
    }
}