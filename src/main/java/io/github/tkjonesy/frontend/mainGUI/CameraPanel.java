package io.github.tkjonesy.frontend.mainGUI;

import io.github.tkjonesy.frontend.App;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

@Getter
public class CameraPanel extends JPanel {
    private final App appInstance;
    private final JLabel cameraFeed;
    private Dimension cameraSize;

    /**
     * Creates a camera panel with the camera feed centered
     *
     * @param appInstance The main application instance
     */
    public CameraPanel(App appInstance) {
        super(new BorderLayout());
        this.appInstance = appInstance;
        this.setBorder(BorderFactory.createTitledBorder("Camera"));

        // Initialize with a default size
        this.cameraSize = new Dimension(640, 480);

        // Create the camera feed label
        cameraFeed = new JLabel();
        cameraFeed.setHorizontalAlignment(SwingConstants.CENTER);
        cameraFeed.setVerticalAlignment(SwingConstants.CENTER);

        // Create a panel to hold the camera feed with a fixed size
        JPanel cameraContainer = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return cameraSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return cameraSize;
            }

            @Override
            public Dimension getMaximumSize() {
                return cameraSize;
            }
        };

        cameraContainer.setLayout(new BorderLayout());
        cameraContainer.add(cameraFeed, BorderLayout.CENTER);

        // Create a panel to center the camera container
        JPanel centeringPanel = new JPanel(new GridBagLayout());
        centeringPanel.add(cameraContainer);

        // Add the centering panel to the main panel
        this.add(centeringPanel, BorderLayout.CENTER);
    }

    /**
     * Sets the size of the camera feed area
     *
     * @param width Width in pixels
     * @param height Height in pixels
     */
    public void setCameraSize(int width, int height) {
        this.cameraSize = new Dimension(width, height);
        revalidate();
        repaint();
    }

    /**
     * Sets the size of the camera feed area
     *
     * @param size Dimension object containing width and height
     */
    public void setCameraSize(Dimension size) {
        this.cameraSize = new Dimension(size);
        revalidate();
        repaint();
    }

    /**
     * Gets the current camera size
     *
     * @return The current camera size as a Dimension
     */
    public Dimension getCameraSize() {
        return new Dimension(cameraSize);
    }
}