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
    private final JLabel preserveAspectRatioLabel;

    private final JComboBox<String> cameraSelector;
    private final JButton refreshCamerasButton;
    private final JSpinner cameraFpsSpinner;
    private final JSlider cameraRotationSlider;
    private final JCheckBox mirrorCameraCheckbox;
    private final JCheckBox preserveAspectRatioCheckbox;
    private final JLabel cameraFpsWarningLabel;

    // Animation components
    private Timer animationTimer;
    private int rotation = 0;
    private boolean isRefreshing = false;

    public CameraSettingsPanel(ProgramSettings settings, HashMap<String, Integer> availableCameras) {
        // Components
        this.cameraSelectorLabel = new JLabel("Camera Selection");
        this.cameraSelector = new JComboBox<>();
        this.cameraFpsLabel = new JLabel("Camera Frames Per Second");
        this.cameraFpsSpinner = new JSpinner(new SpinnerNumberModel(settings.getCameraFps(), 0, 60, 1));
        this.cameraFpsWarningLabel = new JLabel("");

        // Populate camera selection menu with available cameras
        int itemIndex = 0;
        for(String cameraName : availableCameras.keySet()) {
            cameraSelector.addItem(cameraName);
            if(availableCameras.get(cameraName) == settings.getCameraDeviceId()) {
                cameraSelector.setSelectedIndex(itemIndex);
            }
            itemIndex++;
        }

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

        this.preserveAspectRatioLabel = new JLabel("Preserve Aspect Ratio");
        this.preserveAspectRatioCheckbox = new JCheckBox();
        this.preserveAspectRatioCheckbox.setSelected(settings.isPreserveAspectRatio());

        setupAnimationTimer();

        setLayout();
        initListeners();
    }

    @Override
    public void initListeners() {
        // FPS Warning Label
        cameraFpsWarningLabel.setForeground(Color.RED);
        cameraFpsSpinner.addChangeListener(
                e -> {
                    if((int) cameraFpsSpinner.getValue() > 30)
                        DialogManager.displayWarningDialog("Values over 30 may not be supported by all cameras. Setting this value higher than 30 will not make the recording smoother if the camera does not have a refresh rate this high. Additionally, values over 60 may cause extreme performance issues.");
                    }
        );


        addSettingChangeListener(cameraSelector, (ActionListener)
                e -> {
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

        addSettingChangeListener(cameraFpsSpinner, (ChangeListener)
                e -> {
                    int value = (int) cameraFpsSpinner.getValue();
                    AIMsLogger.TRACE("FPS selected: " + value);
                    settingsUpdates.put("cameraFps", value);
                    if(settings.getCameraFps() == value)
                        settingsUpdates.remove("cameraFps");
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

        addSettingChangeListener(preserveAspectRatioCheckbox, (ActionListener)
                e -> {
                    boolean value = preserveAspectRatioCheckbox.isSelected();
                    AIMsLogger.TRACE("Preserve aspect ratio selected: " + value);
                    settingsUpdates.put("preserveAspectRatio", value);
                    if(settings.isPreserveAspectRatio() == value)
                        settingsUpdates.remove("preserveAspectRatio");
                }
        );
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
                                        .addComponent(preserveAspectRatioLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(preserveAspectRatioCheckbox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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
                                        .addComponent(preserveAspectRatioLabel)
                                        .addComponent(preserveAspectRatioCheckbox)
                        )
        );
    }

    /**
     * Creates a simple refresh icon
     */
    private Icon createRefreshIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Apply rotation transformation if refreshing
                if (isRefreshing) {
                    AffineTransform old = g2d.getTransform();
                    g2d.rotate(Math.toRadians(rotation), x + 10, y + 10);

                    // Draw refresh arrow
                    g2d.setColor(new Color(59, 89, 152));
                    g2d.setStroke(new BasicStroke(2));

                    // Draw circular arrow
                    g2d.drawArc(x + 3, y + 3, 14, 14, 40, 280);

                    // Draw arrowhead
                    g2d.drawLine(x + 15, y + 7, x + 17, y + 3);
                    g2d.drawLine(x + 15, y + 7, x + 19, y + 8);

                    g2d.setTransform(old);
                } else {
                    // Draw static refresh arrow when not animating
                    g2d.setColor(new Color(59, 89, 152));
                    g2d.setStroke(new BasicStroke(2));

                    // Draw circular arrow
                    g2d.drawArc(x + 3, y + 3, 14, 14, 40, 280);

                    // Draw arrowhead
                    g2d.drawLine(x + 15, y + 7, x + 17, y + 3);
                    g2d.drawLine(x + 15, y + 7, x + 19, y + 8);
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
        isRefreshing = false;
        animationTimer.stop();
        rotation = 0;
        refreshCamerasButton.repaint();
        refreshCamerasButton.setEnabled(true);
    }
}