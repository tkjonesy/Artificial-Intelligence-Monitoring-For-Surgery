package io.github.tkjonesy.frontend.settingsGUI.panels.AdvancedSettingsPanel;

import ai.onnxruntime.OrtSession;
import io.github.tkjonesy.ONNX.YoloV8;
import io.github.tkjonesy.frontend.settingsGUI.SettingsUI;
import io.github.tkjonesy.frontend.settingsGUI.SettingsWindow;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.utils.settings.ProgramSettings;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.tkjonesy.frontend.settingsGUI.SettingsWindow.addSettingChangeListener;
import static io.github.tkjonesy.frontend.settingsGUI.SettingsWindow.updateApplyButtonState;

public class AdvancedAISection extends JPanel implements SettingsUI {
    private final ProgramSettings settings = ProgramSettings.getCurrentSettings();
    private static final HashMap<String, Object> settingsUpdates = SettingsWindow.getSettingsUpdates();

    // AI settings components
    private final JLabel aiSectionLabel;
    private final JLabel noticeLabel;
    private final JLabel useGPULabel;
    private final JLabel nmsThresholdLabel;
    private final JLabel optimizationLabel;
    private final JLabel inputSizeLabel;
    private final JLabel inputShapeLabel;

    private final HashMap<String, Integer> gpuDevices = new HashMap<>();
    private final JCheckBox useGPUCheckbox;
    private final JComboBox<String> gpuDeviceSelector;
    private final JSlider nmsThresholdSlider;
    private final JTextField nmsThresholdTextField;
    private final JComboBox<String> optimizationLevelComboBox;
    private final JSpinner inputSizeSpinner;
    private final JTextField inputShapeTextField;

    public AdvancedAISection() {
        // AI Section
        this.aiSectionLabel = new JLabel("<html><b>Advanced AI Settings</b></html>");
        aiSectionLabel.setForeground(Color.WHITE);

        // Notice label
        this.noticeLabel = new JLabel("<html><b>Only modify these settings if you truly understand their impact.</b></html>");
        noticeLabel.setForeground(Color.RED);

        // Use GPU (Checkbox)
        this.useGPULabel = new JLabel("Use GPU:");
        useGPUCheckbox = new JCheckBox("");
        useGPUCheckbox.setSelected(settings.isUseGPU() && YoloV8.isCudaAvailable());
        useGPUCheckbox.setToolTipText("Enable GPU for inferencing.");
        if (!YoloV8.isCudaAvailable()) {
            useGPUCheckbox.setEnabled(false);
            useGPUCheckbox.setSelected(false);
            useGPUCheckbox.setToolTipText("GPU is not available.");
        }

        // GPU Device Selector (Dropdown)
        List<String> availableGPUs = detectAvailableGPUs();
        gpuDeviceSelector = new JComboBox<>(availableGPUs.toArray(new String[0]));
        gpuDeviceSelector.setSelectedIndex(settings.getGpuDeviceId());
        gpuDeviceSelector.setToolTipText("Select the GPU device to use (0 for default).");
        gpuDeviceSelector.setVisible(useGPUCheckbox.isSelected());

        useGPUCheckbox.addItemListener(e -> {
            gpuDeviceSelector.setVisible(useGPUCheckbox.isSelected());
            revalidate();
            repaint();
        });

        // NMS Threshold (Slider + TextField)
        this.nmsThresholdLabel = new JLabel("NMS Threshold:");
        nmsThresholdSlider = new JSlider(0, 100, (int) (settings.getNmsThreshold() * 100));
        nmsThresholdSlider.setMajorTickSpacing(10);
        nmsThresholdSlider.setMinorTickSpacing(5);
        nmsThresholdSlider.setPaintTicks(true);
        nmsThresholdSlider.setPaintLabels(true);

        nmsThresholdTextField = new JTextField(String.format("%.2f", settings.getNmsThreshold()), 4);
        nmsThresholdTextField.setHorizontalAlignment(JTextField.CENTER);
        nmsThresholdTextField.setToolTipText("Non-Maximum Suppression threshold (0.01 - 1.00)");

        // Sync Slider & TextField
        nmsThresholdSlider.addChangeListener(e ->
                nmsThresholdTextField.setText(String.format("%.2f", nmsThresholdSlider.getValue() / 100.0))
        );
        nmsThresholdTextField.addActionListener(e -> {
            try {
                float value = Float.parseFloat(nmsThresholdTextField.getText());
                if (value < 0.01f) value = 0.01f;
                else if (value > 1.0f) value = 1.0f;
                nmsThresholdSlider.setValue((int) (value * 100));
            } catch (NumberFormatException ex) {
                nmsThresholdTextField.setText(String.format("%.2f", nmsThresholdSlider.getValue() / 100.0));
            }
        });

        // Optimization Level (Dropdown)
        this.optimizationLabel = new JLabel("Optimization Level:");
        optimizationLevelComboBox = new JComboBox<>(new String[]{"ALL_OPT", "EXTENDED_OPT", "BASIC_OPT", "NO_OPT"});
        optimizationLevelComboBox.setSelectedItem(settings.getOptimizationLevel().name());
        optimizationLevelComboBox.setToolTipText("Choose the level of ONNX Runtime optimizations.");

        // Input Size (Spinner)
        this.inputSizeLabel = new JLabel("Input Size:");
        inputSizeSpinner = new JSpinner(new SpinnerNumberModel(settings.getInputSize(), 1, Integer.MAX_VALUE, 1));
        inputSizeSpinner.setToolTipText("The input image size (e.g., 640 for YOLO).");

        // Input Shape (TextField)
        this.inputShapeLabel = new JLabel("Input Shape:");
        inputShapeTextField = new JTextField(
                Arrays.stream(settings.getInputShape()).mapToObj(String::valueOf).collect(Collectors.joining(", ")),
                20
        );
        inputShapeTextField.setToolTipText("Comma-separated input shape (e.g., 1,3,640,640)");

        setLayout();
        initListeners();
    }

    // Detect available GPUs using nvidia-smi
    private List<String> detectAvailableGPUs() {
        List<String> gpuList = new ArrayList<>();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("nvidia-smi", "--query-gpu=name", "--format=csv,noheader");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int index = 0;

            while ((line = reader.readLine()) != null) {
                gpuList.add("GPU " + index + ": " + line.trim());
                index++;
            }
            reader.close();
        } catch (IOException e) {
            gpuList.add("No GPU detected");
        }

        return gpuList;
    }

    @Override
    public void initListeners() {
        // AI Settings Listeners
        addSettingChangeListener(useGPUCheckbox, (ActionListener)
                e -> {
                    boolean value = useGPUCheckbox.isSelected();
                    AIMsLogger.TRACE("Use GPU: " + value);
                    settingsUpdates.put("useGPU", value);
                    if(settings.isUseGPU() == value)
                        settingsUpdates.remove("useGPU");
                }
        );

        addSettingChangeListener(gpuDeviceSelector, (ActionListener)
                e -> {
                    String value = (String) gpuDeviceSelector.getSelectedItem();
                    if (value != null && value.startsWith("GPU ")) {
                        try {
                            int deviceId = Integer.parseInt(value.substring(4, value.indexOf(':')));
                            AIMsLogger.TRACE("GPU Device ID: " + deviceId);
                            settingsUpdates.put("gpuDeviceId", deviceId);
                            if(settings.getGpuDeviceId() == deviceId)
                                settingsUpdates.remove("gpuDeviceId");
                        } catch (NumberFormatException | StringIndexOutOfBoundsException ex) {
                            // Handle parsing error
                            System.err.println("Failed to parse GPU device ID: " + ex.getMessage());
                        }
                    }
                }
        );

        addSettingChangeListener(nmsThresholdSlider, (ChangeListener)
                e -> {
                    float value = nmsThresholdSlider.getValue() / 100f;
                    AIMsLogger.TRACE("NMS Threshold: " + value);
                    settingsUpdates.put("nmsThreshold", value);
                    if(settings.getNmsThreshold() == value)
                        settingsUpdates.remove("nmsThreshold");
                }
        );

        addSettingChangeListener(optimizationLevelComboBox, (ActionListener)
                e -> {
                    String value = (String) optimizationLevelComboBox.getSelectedItem();
                    AIMsLogger.TRACE("Optimization Level: " + value);
                    settingsUpdates.put("optimizationLevel", OrtSession.SessionOptions.OptLevel.valueOf(value));
                    if(settings.getOptimizationLevel().toString().equals(value))
                        settingsUpdates.remove("optimizationLevel");
                }
        );

        addSettingChangeListener(inputSizeSpinner, (ChangeListener)
                e -> {
                    int value = (int) inputSizeSpinner.getValue();
                    AIMsLogger.TRACE("Input Size: " + value);
                    settingsUpdates.put("inputSize", value);
                    if(settings.getInputSize() == value)
                        settingsUpdates.remove("inputSize");
                }
        );

        inputShapeTextField.addActionListener(
                e -> {
                    String newValue = inputShapeTextField.getText().trim().replaceAll(" ", "");
                    String currentValue = Arrays.toString(settings.getInputShape()).replaceAll("[\\[\\] ]", " ").trim().replaceAll(" ", "");

                    if (!newValue.equals(currentValue)) {
                        try {
                            long[] inputShape = Arrays.stream(newValue.split(","))
                                    .mapToLong(Long::parseLong)
                                    .toArray();

                            long numInputElements = Arrays.stream(inputShape).reduce(1, (a, b) -> a * b);

                            AIMsLogger.TRACE("Input Shape: " + Arrays.toString(inputShape));

                            settingsUpdates.put("inputShape", inputShape);
                            settingsUpdates.put("numInputElements", (int) numInputElements);

                            updateApplyButtonState();
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this,
                                    "Invalid input shape format. Please use comma-separated numbers.",
                                    "Input Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        settingsUpdates.remove("inputShape");
                        settingsUpdates.remove("numInputElements");

                        updateApplyButtonState();
                    }
                }
        );
    }

    @Override
    public void setLayout() {
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        // AI Section
                        .addComponent(aiSectionLabel)
                        .addComponent(noticeLabel)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(useGPULabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(useGPUCheckbox)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(gpuDeviceSelector, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(nmsThresholdLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nmsThresholdSlider, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nmsThresholdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(optimizationLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(optimizationLevelComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(inputSizeLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(inputSizeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(inputShapeLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(inputShapeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        // AI Section
                        .addComponent(aiSectionLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(noticeLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(useGPULabel)
                                .addComponent(useGPUCheckbox)
                                .addComponent(gpuDeviceSelector))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(nmsThresholdLabel)
                                .addComponent(nmsThresholdSlider)
                                .addComponent(nmsThresholdTextField))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(optimizationLabel)
                                .addComponent(optimizationLevelComboBox))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(inputSizeLabel)
                                .addComponent(inputSizeSpinner))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(inputShapeLabel)
                                .addComponent(inputShapeTextField))
        );
    }
}