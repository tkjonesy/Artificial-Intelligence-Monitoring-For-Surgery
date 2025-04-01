package io.github.tkjonesy.frontend.settingsGUI.panels;

import io.github.tkjonesy.frontend.settingsGUI.SettingsUI;
import io.github.tkjonesy.frontend.settingsGUI.SettingsWindow;
import io.github.tkjonesy.utils.DialogManager;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.utils.settings.ProgramSettings;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
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

        // Mirror Camera
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
}