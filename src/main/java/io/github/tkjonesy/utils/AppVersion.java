package io.github.tkjonesy.utils;

import lombok.Getter;

import java.io.InputStream;
import java.util.Properties;

/**
 * Class to get the app version from git.properties
 */
public class AppVersion {

    @Getter
    private static final String COMMIT_ID_ABBREV;
    @Getter
    private static final String COMMIT_ID_FULL;

    static{
        Properties properties = new Properties();
        try (InputStream input = AppVersion.class.getClassLoader().getResourceAsStream("git.properties")) {
            properties.load(input);
            COMMIT_ID_ABBREV = properties.getProperty("git.commit.id.abbrev", "unknown version");
            COMMIT_ID_FULL = properties.getProperty("git.commit.id.full", "unknown version");
        }catch (Exception e) {
            throw new RuntimeException("Failed to load app version", e);
        }
    }

}
