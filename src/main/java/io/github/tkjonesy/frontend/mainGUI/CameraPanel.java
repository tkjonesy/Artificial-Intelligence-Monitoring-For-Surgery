package io.github.tkjonesy.frontend.mainGUI;

import io.github.tkjonesy.frontend.App;
import io.github.tkjonesy.frontend.utils.AspectRatioCalculator;
import io.github.tkjonesy.utils.settings.ProgramSettings;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

@Getter
public class CameraPanel extends JPanel {

    private static final ProgramSettings settings = ProgramSettings.getCurrentSettings();

    private final App appInstance;
    private final JLabel cameraFeed;
    private Dimension cameraSize;
    private final JPanel cameraContainer;
    private final JPanel centeringPanel;
    private int aspectRatioWidth = 1; // Default aspect ratio components
    private int aspectRatioHeight = 1;
    private boolean resizeInProgress = false;
    private boolean useAspectRatio = true; // Flag to determine if aspect ratio should be maintained

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

            @Override
            public void componentShown(ComponentEvent e) {
                // Also handle resize when component becomes visible
                handleResize();
            }
        });

        // Initialize aspect ratio from settings
        updateAspectRatio();
    }

    /**
     * Handles resizing of the panel by calculating the appropriate camera size
     * based on the available space and maintaining aspect ratio if needed.
     * This method is now public to allow external classes to trigger resize.
     */
    public void handleResize() {
        if (resizeInProgress) {
            return; // Prevent recursive calls
        }

        resizeInProgress = true;

        // Calculate available space in the centering panel
        Insets insets = getInsets();
        int availableWidth = getWidth() - insets.left - insets.right - 20; // Add some padding
        int availableHeight = getHeight() - insets.top - insets.bottom - 20;

        // Don't resize if dimensions are too small or component is not visible
        if (availableWidth <= 0 || availableHeight <= 0 || !isVisible()) {
            resizeInProgress = false;
            return;
        }

        Dimension newSize;

        if (useAspectRatio && aspectRatioWidth > 0 && aspectRatioHeight > 0) {
            // Calculate the optimal size while maintaining aspect ratio
            newSize = AspectRatioCalculator.calculateRatio(
                    availableWidth,
                    availableHeight,
                    aspectRatioWidth,
                    aspectRatioHeight
            );
        } else {
            // Fill panel completely in case of no aspect ratio constraint
            newSize = new Dimension(availableWidth, availableHeight);
        }

        // Only update if there's a significant change in size
        if (Math.abs(cameraSize.width - newSize.width) > 5 ||
                Math.abs(cameraSize.height - newSize.height) > 5) {
            // Update camera size
            setCameraSize(newSize);

            // Force immediate container updates
            cameraContainer.invalidate();
            centeringPanel.revalidate();
            centeringPanel.repaint();
        }

        resizeInProgress = false;

        // Schedule another resize after a short delay to catch any post-layout changes
        SwingUtilities.invokeLater(() -> {
            // This helps with certain window managers that perform resize in multiple steps
            if (!resizeInProgress && isVisible()) {
                resizeInProgress = true;
                centeringPanel.revalidate();
                centeringPanel.repaint();
                resizeInProgress = false;
            }
        });
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

    /**
     * Updates the aspect ratio based on the settings and triggers a UI update
     */
    public void updateAspectRatio() {
        switch(settings.getAspectRatio()) {
            case "16:9":
                aspectRatioWidth = 16;
                aspectRatioHeight = 9;
                useAspectRatio = true;
                break;
            case "4:3":
                aspectRatioWidth = 4;
                aspectRatioHeight = 3;
                useAspectRatio = true;
                break;
            default:
                // Default case: fill the panel completely
                useAspectRatio = false;
                aspectRatioWidth = 0;
                aspectRatioHeight = 0;
        }

        // Trigger resize handling to update the UI immediately
        handleResize();
    }
}