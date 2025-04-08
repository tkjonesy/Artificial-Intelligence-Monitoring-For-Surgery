package io.github.tkjonesy.frontend.miscGUI;

import io.github.tkjonesy.utils.Paths;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class SplashScreen {
    private final JWindow splashScreen;

    /**
     * Initializes the splash screen window, loads image resources, and configures its display properties
     */
    public SplashScreen() {
        splashScreen = new JWindow();
        splashScreen.setBackground(new Color(0, 0, 0, 0));

        // Load image from resources
        ImageIcon icon = loadImage();

        // Create a label with the image
        JLabel imageLabel = new JLabel(icon);

        // Set layout and add image to window
        splashScreen.getContentPane().add(imageLabel, BorderLayout.CENTER);
        splashScreen.setSize(icon.getIconWidth(), icon.getIconHeight());
        splashScreen.setLocationRelativeTo(null);
    }

    /**
     * Displays splash screen to user by making it visible
     */
    public void showSplash() {
        splashScreen.setVisible(true);
    }

    /**
     * Hides splash screen and disposes of it to free resources
     */
    public void closeSplash() {
        splashScreen.dispose();
    }

    /**
     * Loads the image to be displayed in the splash screen
     * @return {@link ImageIcon} representing the splash screen image. If the image resource cannot be loaded an empty {@link ImageIcon} is returned
     */
    private ImageIcon loadImage() {
        try (InputStream stream = getClass().getResourceAsStream(Paths.LOGO_PATH)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + Paths.LOGO_PATH);
            }

            Image image = ImageIO.read(stream);
            return new ImageIcon(image);

        } catch (IOException e) {
            return new ImageIcon();
        }
    }
}
