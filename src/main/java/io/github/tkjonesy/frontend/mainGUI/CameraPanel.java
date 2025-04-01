package io.github.tkjonesy.frontend.mainGUI;

import io.github.tkjonesy.frontend.App;
import io.github.tkjonesy.frontend.utils.AspectRatioCalculator;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

@Getter
public class CameraPanel extends JPanel {
    private final App appInstance;
    private final JLabel cameraFeed;
    private Dimension cameraSize;
    private final JPanel cameraContainer;
    private final JPanel centeringPanel;
    private final int aspectRatioWidth = 4; // Default 4:3 aspect ratio
    private final int aspectRatioHeight = 3;
    private boolean resizeInProgress = false;

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

        // Create a panel to hold the camera feed with a dynamic size
        cameraContainer = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return cameraSize;
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(160, 120); // Minimum reasonable size
            }

            @Override
            public Dimension getMaximumSize() {
                return cameraSize;
            }
        };

        cameraContainer.setLayout(new BorderLayout());
        cameraContainer.add(cameraFeed, BorderLayout.CENTER);

        // Create a panel to center the camera container
        centeringPanel = new JPanel(new GridBagLayout());
        centeringPanel.add(cameraContainer);

        // Add the centering panel to the main panel
        this.add(centeringPanel, BorderLayout.CENTER);

        // Add component listener to handle resize events
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                handleResize();
            }
        });
    }

    /**
     * Handles resizing of the panel by calculating the appropriate camera size
     * based on the available space and maintaining aspect ratio
     */
    private void handleResize() {
        if (resizeInProgress) {
            return; // Prevent recursive calls
        }

        resizeInProgress = true;

        // Calculate available space in the centering panel
        Insets insets = getInsets();
        int availableWidth = getWidth() - insets.left - insets.right - 20; // Add some padding
        int availableHeight = getHeight() - insets.top - insets.bottom - 20;

        // Don't resize if dimensions are too small
        if (availableWidth <= 0 || availableHeight <= 0) {
            resizeInProgress = false;
            return;
        }

        // Calculate the optimal size while maintaining aspect ratio
        Dimension newSize = AspectRatioCalculator.calculateRatio(
                availableWidth,
                availableHeight,
                aspectRatioWidth,
                aspectRatioHeight
        );

        // Update camera size
        setCameraSize(newSize);

        resizeInProgress = false;
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