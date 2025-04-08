package io.github.tkjonesy.frontend.settingsGUI.panels;

import io.github.tkjonesy.frontend.App;
import io.github.tkjonesy.frontend.settingsGUI.SettingsUI;
import io.github.tkjonesy.frontend.settingsGUI.SettingsWindow;
import io.github.tkjonesy.utils.DialogManager;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.utils.settings.ProgramSettings;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Hashtable;

import static io.github.tkjonesy.frontend.App.AVAILABLE_CAMERAS;
import static io.github.tkjonesy.frontend.settingsGUI.SettingsWindow.addSettingChangeListener;

public class CameraSettingsPanel extends JPanel implements SettingsUI {

    private static final ProgramSettings settings = ProgramSettings.getCurrentSettings();
    private static final HashMap<String, Object> settingsUpdates = SettingsWindow.getSettingsUpdates();

    private final JLabel cameraSelectorLabel;
    private final JLabel cameraFpsLabel;
    private final JLabel cameraRotationLabel;
    private final JLabel mirrorCameraLabel;
    private final JLabel aspectRatioLabel;

    private final JComboBox<String> cameraSelector;
    private final JButton refreshCamerasButton;
    private final JSpinner cameraFpsSpinner;
    private final JSlider cameraRotationSlider;
    private final JCheckBox mirrorCameraCheckbox;
    private final JLabel cameraFpsWarningLabel;

    // Aspect ratio components
    private final JPanel aspectRatioPanel;
    private final ButtonGroup aspectRatioGroup;
    private final JRadioButton ratio16_9Button;
    private final JRadioButton ratio4_3Button;
    private final JRadioButton ratioFillButton;

    // Animation components
    private Timer animationTimer;
    private int rotation = 0;
    private boolean isRefreshing = false;

    /**
     * Constructs a new {@code CameraSettings Panel} that initializes UI components for camera settings
     * @param settings The {@link ProgramSettings} object containing current settings
     */
    public CameraSettingsPanel(ProgramSettings settings) {
        // Components
        this.cameraSelectorLabel = new JLabel("Camera Selection");
        this.cameraSelector = new JComboBox<>();
        this.cameraFpsLabel = new JLabel("Camera Frames Per Second");
        this.cameraFpsSpinner = new JSpinner(new SpinnerNumberModel(settings.getCameraFps(), 0, 60, 1));
        this.cameraFpsWarningLabel = new JLabel("");

        // Populate camera selection menu with available cameras
        populateCameraSelector();

        // Camera Rotation
        this.cameraRotationLabel = new JLabel("Camera Rotation:");
        this.cameraRotationSlider = new JSlider(0, 270, settings.getCameraRotation());
        cameraRotationSlider.setMajorTickSpacing(90);
        cameraRotationSlider.setSnapToTicks(true);
        cameraRotationSlider.setPaintTicks(true);
        cameraRotationSlider.setPaintLabels(true);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("0"));
        labelTable.put(90, new JLabel("90"));
        labelTable.put(180, new JLabel("180"));
        labelTable.put(270, new JLabel("270"));
        cameraRotationSlider.setLabelTable(labelTable);

        // Refresh Cameras Button
        this.refreshCamerasButton = new JButton(createRefreshIcon());
        refreshCamerasButton.setToolTipText("Refresh Camera List");
        refreshCamerasButton.setPreferredSize(new Dimension(24, 24));
        refreshCamerasButton.setFocusPainted(false);

        // Mirror & Aspect Ratio
        this.mirrorCameraLabel = new JLabel("Mirror Camera");
        this.mirrorCameraCheckbox = new JCheckBox();
        this.mirrorCameraCheckbox.setSelected(settings.isMirrorCamera());

        // Aspect Ratio Selection Group
        this.aspectRatioLabel = new JLabel("Aspect Ratio:");
        this.aspectRatioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        this.aspectRatioGroup = new ButtonGroup();

        this.ratio16_9Button = new JRadioButton("16:9");
        this.ratio4_3Button = new JRadioButton("4:3");
        this.ratioFillButton = new JRadioButton("Fill");

        // Add radio buttons to group
        aspectRatioGroup.add(ratio16_9Button);
        aspectRatioGroup.add(ratio4_3Button);
        aspectRatioGroup.add(ratioFillButton);

        // Add radio buttons to panel
        aspectRatioPanel.add(ratio16_9Button);
        aspectRatioPanel.add(ratio4_3Button);
        aspectRatioPanel.add(ratioFillButton);

        // Set default selection based on settings
        String aspectRatio = settings.getAspectRatio();
        if (aspectRatio == null) {
            aspectRatio = "4:3"; // Default if not set
        }

        switch (aspectRatio) {
            case "16:9":
                ratio16_9Button.setSelected(true);
                break;
            case "4:3":
                ratio4_3Button.setSelected(true);
                break;
            case "fill":
                ratioFillButton.setSelected(true);
                break;
            default:
                ratio4_3Button.setSelected(true); // Default to 4:3
                break;
        }

        setupAnimationTimer();

        setLayout();
        initListeners();
    }

    /**
     * Populates camera selector dropdown with currently available cameras
     */
    private void populateCameraSelector(){
        int itemIndex = 0;
        HashMap<String, Integer> availableCameras = AVAILABLE_CAMERAS;
        for(String cameraName : availableCameras.keySet()) {
            cameraSelector.addItem(cameraName);
            if(availableCameras.get(cameraName) == settings.getCameraDeviceId()) {
                cameraSelector.setSelectedIndex(itemIndex);
            }
            itemIndex++;
        }
    }

    @Override
    public void initListeners() {
        // FPS Warning Label
        cameraFpsWarningLabel.setForeground(Color.RED);

        addSettingChangeListener(cameraFpsSpinner, (ChangeListener)
                e -> {
                    int value = (int) cameraFpsSpinner.getValue();
                    if(value > 30) {
                        cameraFpsWarningLabel.setText("Warning: High FPS may not be supported by all cameras.");
                    } else {
                        cameraFpsWarningLabel.setText("");
                    }

                    AIMsLogger.TRACE("FPS selected: " + value);
                    settingsUpdates.put("cameraFps", value);
                    if(settings.getCameraFps() == value)
                        settingsUpdates.remove("cameraFps");

                }
        );

        addSettingChangeListener(cameraSelector, (ActionListener)
                e -> {
                    if(isRefreshing) return;
                    String value = (String) cameraSelector.getSelectedItem();
                    AIMsLogger.TRACE("Camera selected: " + value);
                    settingsUpdates.put("cameraDeviceId", AVAILABLE_CAMERAS.get(value));
                    if(settings.getCameraDeviceId() == AVAILABLE_CAMERAS.get(value))
                        settingsUpdates.remove("cameraDeviceId");
                }
        );

        refreshCamerasButton.addActionListener(
                e -> {
                    startRefreshAnimation();

                    new Thread(() -> {
                        App.getInstance().collectAvailableCameras();
                        SwingUtilities.invokeLater(this::stopRefreshAnimation);
                    }).start();
                }
        );

        addSettingChangeListener(cameraRotationSlider, (ChangeListener)
                e -> {
                    int value = cameraRotationSlider.getValue();
                    AIMsLogger.TRACE("Camera rotation selected: " + value);
                    settingsUpdates.put("cameraRotation", value);
                    if(settings.getCameraRotation() == value)
                        settingsUpdates.remove("cameraRotation");
                }
        );

        addSettingChangeListener(mirrorCameraCheckbox, (ActionListener)
                e -> {
                    boolean value = mirrorCameraCheckbox.isSelected();
                    AIMsLogger.TRACE("Mirror camera selected: " + value);
                    settingsUpdates.put("mirrorCamera", value);
                    if(settings.isMirrorCamera() == value)
                        settingsUpdates.remove("mirrorCamera");
                }
        );

        // Aspect Ratio radio button listeners
        ActionListener aspectRatioListener = e -> {
            String aspectRatio;
            if (ratio16_9Button.isSelected()) {
                aspectRatio = "16:9";
            } else if (ratio4_3Button.isSelected()) {
                aspectRatio = "4:3";
            } else {
                aspectRatio = "fill";
            }

            AIMsLogger.TRACE("Aspect ratio selected: " + aspectRatio);
            settingsUpdates.put("aspectRatio", aspectRatio);

            // Only remove if the setting matches the current value
            if (settings.getAspectRatio() != null && settings.getAspectRatio().equals(aspectRatio)) {
                settingsUpdates.remove("aspectRatio");
            }
        };

        addSettingChangeListener(ratio16_9Button, (ActionListener) aspectRatioListener);
        addSettingChangeListener(ratio4_3Button, (ActionListener) aspectRatioListener);
        addSettingChangeListener(ratioFillButton, (ActionListener) aspectRatioListener);
    }

    @Override
    public void setLayout() {
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(cameraSelectorLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cameraSelector, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(refreshCamerasButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(cameraFpsLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cameraFpsSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cameraFpsWarningLabel)
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(cameraRotationLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cameraRotationSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE) // Slider
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(mirrorCameraLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(mirrorCameraCheckbox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(aspectRatioLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(aspectRatioPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(cameraSelectorLabel)
                                        .addComponent(cameraSelector)
                                        .addComponent(refreshCamerasButton)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(cameraFpsLabel)
                                        .addComponent(cameraFpsSpinner)
                                        .addComponent(cameraFpsWarningLabel)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(cameraRotationLabel)
                                        .addComponent(cameraRotationSlider)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(mirrorCameraLabel)
                                        .addComponent(mirrorCameraCheckbox)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(aspectRatioLabel)
                                        .addComponent(aspectRatioPanel)
                        )
        );
    }

    /**
     * Creates a refresh icon from an image resource
     * The icon can be animated by rotating it when refreshing
     */
    private Icon createRefreshIcon() {
        ImageIcon originalIcon;

        try {
            originalIcon = new ImageIcon(getClass().getResource("/images/refresh.png"));

            if (originalIcon.getIconWidth() == -1) {
                throw new Exception("Resource not found");
            }

            Image img = originalIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            originalIcon = new ImageIcon(img);
        } catch (Exception e) {
            AIMsLogger.WARN("Refresh icon not found, using fallback");
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (isRefreshing) {
                        g2d.rotate(Math.toRadians(rotation), x + 10, y + 10);
                    }

                    // Draw a circular background
                    g2d.setColor(new Color(240, 240, 240));
                    g2d.fillOval(x + 2, y + 2, 16, 16);

                    // Draw border
                    g2d.setColor(new Color(59, 89, 152));
                    g2d.drawOval(x + 2, y + 2, 16, 16);

                    // Draw "R" letter
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    g2d.drawString("R", x + 7, y + 15);

                    if (isRefreshing) {
                        g2d.setTransform(new AffineTransform());
                    }

                    g2d.dispose();
                }

                @Override
                public int getIconWidth() {
                    return 20;
                }

                @Override
                public int getIconHeight() {
                    return 20;
                }
            };
        }

        // Return an icon that will handle the rotation animation
        final ImageIcon finalIcon = originalIcon;
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isRefreshing) {
                    // Rotate around the center of the icon
                    g2d.rotate(Math.toRadians(rotation),
                            x + (double) finalIcon.getIconWidth() / 2,
                            y + (double) finalIcon.getIconHeight() / 2);
                }

                // Draw the image
                finalIcon.paintIcon(c, g2d, x, y);

                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return finalIcon.getIconWidth();
            }

            @Override
            public int getIconHeight() {
                return finalIcon.getIconHeight();
            }
        };
    }

    /**
     * Sets up the animation timer for the refresh button
     */
    private void setupAnimationTimer() {
        animationTimer = new Timer(50, e -> {
            rotation = (rotation + 10) % 360;
            refreshCamerasButton.repaint();
        });
    }

    /**
     * Starts the refresh animation
     */
    public void startRefreshAnimation() {
        isRefreshing = true;
        animationTimer.start();
        refreshCamerasButton.setEnabled(false);
    }

    /**
     * Stops the refresh animation
     */
    public void stopRefreshAnimation() {
        this.cameraSelector.removeAllItems();
        populateCameraSelector();
        isRefreshing = false;
        animationTimer.stop();
        rotation = 0;
        refreshCamerasButton.repaint();
        refreshCamerasButton.setEnabled(true);
    }
}