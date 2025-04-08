package io.github.tkjonesy.frontend.settingsGUI.panels;

import io.github.tkjonesy.frontend.settingsGUI.SettingsUI;
import io.github.tkjonesy.frontend.settingsGUI.SettingsWindow;
import io.github.tkjonesy.ONNX.enums.colorChangeEnum;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.utils.settings.ProgramSettings;
import io.github.tkjonesy.utils.settings.SettingsLoader;


import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static io.github.tkjonesy.frontend.settingsGUI.SettingsWindow.updateApplyButtonState;
import static io.github.tkjonesy.utils.Paths.AIMS_MODELS_DIRECTORY;
import static io.github.tkjonesy.frontend.settingsGUI.SettingsWindow.addSettingChangeListener;


public class AISettingsPanel extends JPanel implements SettingsUI {
    private static final ProgramSettings settings = ProgramSettings.getCurrentSettings();
    private static final HashMap<String, Object> settingsUpdates = SettingsWindow.getSettingsUpdates();

    private final JLabel modelLabel;
    private final JLabel labelLabel;
    private final JLabel colorLabel;
    private final JLabel boundBoxCheckboxLabel;
    private final JLabel showLabelsCheckboxLabel;
    private final JLabel showConfidencesCheckboxLabel;
    private final JLabel processNthLabel;
    private final JLabel bufferThresholdLabel;
    private final JLabel confThresholdLabel;
    private final JLabel noticeLabel;
    private final JLabel logFontSizeLabel;



    private int[] boundingBoxColor = new int[3];
    private final JTextField rInputTextField;
    private final JTextField gInputTextField;
    private final JTextField bInputTextField;
    private final JButton colorPreviewButton;

    private int[] logAddedColor = new int[3];
    private final JTextField logAddedRInputTextField;
    private final JTextField logAddedGInputTextField;
    private final JTextField logAddedBInputTextField;
    private final JButton logAddedColorPreviewButton;

    private int[] logRemovedColor = new int[3];
    private final JTextField logRemovedRInputTextField;
    private final JTextField logRemovedGInputTextField;
    private final JTextField logRemovedBInputTextField;
    private final JButton logRemovedColorPreviewButton;

    private final JLabel logAddedColorLabel = new JLabel("Log Added Color (RGB):");
    private final JLabel logRemovedColorLabel = new JLabel("Log Removed Color (RGB):");

    private final JComboBox<String> modelSelector;
    private final JComboBox<String> labelSelector;
    private final JCheckBox boundingBoxCheckbox;
    private final JCheckBox showLabelsCheckbox;
    private final JCheckBox showConfidencesCheckbox;
    private final JSpinner processEveryNthFrameSpinner;
    private final JSpinner bufferThresholdSpinner;
    private final JSlider confThresholdSlider;
    private final JTextField confThresholdTextField;
    private final JTextPane logFontPreviewPane;
    private final JButton openFolderButton;
    private final JSpinner logFontSizeSpinner;
    private final JScrollPane logFontPreviewScrollPane;

    private final List<AISettingsListener> listeners = new ArrayList<>();

    public interface AISettingsListener {
        void onColorChanged(String settingKey, int[] newColor);
    }

    public AISettingsPanel() {
        this.noticeLabel = new JLabel("<html><b>Only YOLOv8+ models in .onnx format are supported.</b></html>");
        this.noticeLabel.setForeground(Color.GRAY);

        this.openFolderButton = new JButton("Open AI Models Folder");
        openFolderButton.addActionListener(e -> openAIDirectory());

        this.modelLabel = new JLabel("AI Model:");
        this.modelSelector = new JComboBox<>(getFilesWithExtension(".onnx"));
        modelSelector.setSelectedItem(new File(settings.getModelPath()).getName());

        this.labelLabel = new JLabel("Label File:");
        this.labelSelector = new JComboBox<>(getFilesWithExtension(".names"));
        labelSelector.setSelectedItem(new File(settings.getLabelPath()).getName());

        this.colorLabel = new JLabel("Bounding box color (RGB):");

        int[] boundingBoxColor = settings.getBoundingBoxColor();
        int r;
        int g;
        int b;
        try{
            r = boundingBoxColor[0];
            g = boundingBoxColor[1];
            b = boundingBoxColor[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            int[] defaultColor = SettingsLoader.getDEFAULT_SETTINGS().getBoundingBoxColor();
            r = defaultColor[0];
            g = defaultColor[1];
            b = defaultColor[2];
        }

        this.rInputTextField = new JTextField(String.valueOf(r), 3);
        this.gInputTextField = new JTextField(String.valueOf(g), 3);
        this.bInputTextField = new JTextField(String.valueOf(b), 3);

        rInputTextField.addActionListener(e -> updateColor(colorChangeEnum.BOUNDINGBOX.getCode()));
        gInputTextField.addActionListener(e -> updateColor(colorChangeEnum.BOUNDINGBOX.getCode()));
        bInputTextField.addActionListener(e -> updateColor(colorChangeEnum.BOUNDINGBOX.getCode()));

        this.colorPreviewButton = new JButton() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(30, 30);
            }
        };
        colorPreviewButton.setBackground(new Color(r, g, b));
        colorPreviewButton.setMinimumSize(new Dimension(30, 30));
        colorPreviewButton.setMaximumSize(new Dimension(30, 30));
        colorPreviewButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        colorPreviewButton.addActionListener(e -> openColorChooser());

        // Initialize Log Added Color
        int[] logAddedColor = settings.getLogAddedColor();
        int r1;
        int g1;
        int b1;
        try{
            r1 = logAddedColor[0];
            g1 = logAddedColor[1];
            b1 = logAddedColor[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            int[] defaultColor = SettingsLoader.getDEFAULT_SETTINGS().getLogAddedColor();
            r1 = defaultColor[0];
            g1 = defaultColor[1];
            b1 = defaultColor[2];
        }

        this.logAddedRInputTextField = new JTextField(String.valueOf(r1), 3);
        this.logAddedGInputTextField = new JTextField(String.valueOf(g1), 3);
        this.logAddedBInputTextField = new JTextField(String.valueOf(b1), 3);

        logAddedRInputTextField.addActionListener(e -> updateColor(colorChangeEnum.LOGADDED.getCode()));
        logAddedGInputTextField.addActionListener(e -> updateColor(colorChangeEnum.LOGADDED.getCode()));
        logAddedBInputTextField.addActionListener(e -> updateColor(colorChangeEnum.LOGADDED.getCode()));

        this.logAddedColorPreviewButton = new JButton() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(30, 30);
            }
        };
        logAddedColorPreviewButton.setBackground(new Color(r1, g1, b1));
        logAddedColorPreviewButton.setMinimumSize(new Dimension(30, 30));
        logAddedColorPreviewButton.setMaximumSize(new Dimension(30, 30));
        logAddedColorPreviewButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        logAddedColorPreviewButton.addActionListener(e -> openLogAddedColorChooser());


        // Initialize Log Removed Color
        int[] logRemovedColor = settings.getLogRemovedColor();
        int r2;
        int g2;
        int b2;
        try{
            r2 = logRemovedColor[0];
            g2 = logRemovedColor[1];
            b2 = logRemovedColor[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            int[] defaultColor = SettingsLoader.getDEFAULT_SETTINGS().getLogRemovedColor();
            r2 = defaultColor[0];
            g2 = defaultColor[1];
            b2 = defaultColor[2];
        }

        this.logRemovedRInputTextField = new JTextField(String.valueOf(r2), 3);
        this.logRemovedGInputTextField = new JTextField(String.valueOf(g2), 3);
        this.logRemovedBInputTextField = new JTextField(String.valueOf(b2), 3);

        logRemovedRInputTextField.addActionListener(e -> updateColor(colorChangeEnum.LOGREMOVED.getCode()));
        logRemovedGInputTextField.addActionListener(e -> updateColor(colorChangeEnum.LOGREMOVED.getCode()));
        logRemovedBInputTextField.addActionListener(e -> updateColor(colorChangeEnum.LOGREMOVED.getCode()));

        this.logRemovedColorPreviewButton = new JButton() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(30, 30);
            }
        };
        logRemovedColorPreviewButton.setBackground(new Color(r2, g2, b2));
        logRemovedColorPreviewButton.setMinimumSize(new Dimension(30, 30));
        logRemovedColorPreviewButton.setMaximumSize(new Dimension(30, 30));
        logRemovedColorPreviewButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        logRemovedColorPreviewButton.addActionListener(e -> openLogRemovedColorChooser());


        this.boundBoxCheckboxLabel = new JLabel("Show Bounding Boxes:");
        this.boundingBoxCheckbox = new JCheckBox("", settings.isShowBoundingBoxes());

        this.showLabelsCheckboxLabel = new JLabel("Show Labels:");
        this.showLabelsCheckbox = new JCheckBox("", settings.isShowLabels());

        this.showConfidencesCheckboxLabel = new JLabel("Show Confidences:");
        this.showConfidencesCheckbox = new JCheckBox("", settings.isShowConfidences());

        this.processNthLabel = new JLabel("Process Every Nth Frame:");
        this.processEveryNthFrameSpinner = new JSpinner(new SpinnerNumberModel(settings.getProcessEveryNthFrame(), 15, 1000, 1));

        this.bufferThresholdLabel = new JLabel("Buffer Threshold:");
        this.bufferThresholdSpinner = new JSpinner(new SpinnerNumberModel(settings.getBufferThreshold(), 0, 100, 1));

        this.confThresholdLabel = new JLabel("Confidence Threshold:");
        this.confThresholdSlider = new JSlider(0, 100, (int) (settings.getConfThreshold() * 100));
        this.confThresholdSlider.setMajorTickSpacing(10);
        this.confThresholdSlider.setMinorTickSpacing(5);
        this.confThresholdSlider.setPaintTicks(true);
        this.confThresholdSlider.setPaintLabels(true);

        this.confThresholdTextField = new JTextField(String.format("%.2f", settings.getConfThreshold()), 4);
        this.confThresholdTextField.setHorizontalAlignment(JTextField.CENTER);

        this.logFontSizeLabel = new JLabel("Log Font Size:");
        this.logFontSizeSpinner = new JSpinner(new SpinnerNumberModel(settings.getLogFontSize(), 8, 48, 1)); // min 8, max 48

        this.logFontPreviewPane = new JTextPane();
        logFontPreviewPane.setContentType("text/html");
        logFontPreviewPane.setEditable(false);
        logFontPreviewPane.setBackground(Color.BLACK);
        logFontPreviewPane.setText(generateLogPreviewHTML(settings.getLogFontSize()));

        this.logFontPreviewScrollPane = new JScrollPane(logFontPreviewPane);
        logFontPreviewScrollPane.setPreferredSize(new Dimension(400, 80));


        setLayout();
        initListeners();
    }


    @Override
    public void initListeners() {
        this.addAISettingsListener((key, newColor) -> {
            switch (key) {
                case "boundingBoxColor" -> handleColorChange(key, newColor, settings.getBoundingBoxColor());
                case "logAddedColor" -> handleColorChange(key, newColor, settings.getLogAddedColor());
                case "logRemovedColor" -> handleColorChange(key, newColor, settings.getLogRemovedColor());
                default -> AIMsLogger.WARN("Unknown color key: " + key);
            }
        });

        // Sync text field to slider (with validation)
        confThresholdTextField.addActionListener(e -> {
            try {
                float typedValue = Float.parseFloat(confThresholdTextField.getText());

                if (typedValue < 0.01f) {
                    typedValue = 0.01f;
                } else if (typedValue > 1.0f) {
                    typedValue = 1.0f;
                }

                confThresholdSlider.setValue((int) (typedValue * 100));
                confThresholdTextField.setText(String.format("%.2f", typedValue));
            } catch (NumberFormatException ex) {
                confThresholdTextField.setText(String.format("%.2f", confThresholdSlider.getValue() / 100.0));
            }
        });

        confThresholdSlider.addChangeListener(e -> {
            int value = confThresholdSlider.getValue();
            confThresholdTextField.setText(String.format("%.2f", value / 100.0));
        });

        addSettingChangeListener(modelSelector, (ActionListener)
                e -> {
                    String value = (String) modelSelector.getSelectedItem();
                    String path = AIMS_MODELS_DIRECTORY + "/" + value;
                    AIMsLogger.TRACE("Selected model: " + value);
                    settingsUpdates.put("modelPath", path);
                    if(settings.getModelPath().equals(path))
                        settingsUpdates.remove("modelPath");
                }
        );

        addSettingChangeListener(labelSelector, (ActionListener)
                e -> {
                    String value = (String) labelSelector.getSelectedItem();
                    String path = AIMS_MODELS_DIRECTORY + "/" + value;
                    AIMsLogger.TRACE("Selected label file: " + value);
                    settingsUpdates.put("labelPath", path);
                    if(settings.getLabelPath().equals(path))
                        settingsUpdates.remove("labelPath");
                }
        );

        addSettingChangeListener(boundingBoxCheckbox, (ActionListener)
                e -> {
                    boolean value = boundingBoxCheckbox.isSelected();
                    AIMsLogger.TRACE("Bounding box visibility changed to: " + value);
                    settingsUpdates.put("showBoundingBoxes", value);
                    if(settings.isShowBoundingBoxes() == value)
                        settingsUpdates.remove("showBoundingBoxes");
                }
        );

        addSettingChangeListener(showLabelsCheckbox, (ActionListener)
                e -> {
                    boolean value = showLabelsCheckbox.isSelected();
                    AIMsLogger.TRACE("Label visibility changed to: " + value);
                    settingsUpdates.put("showLabels", value);
                    if(settings.isShowLabels() == value)
                        settingsUpdates.remove("showLabels");
                }
        );

        addSettingChangeListener(showConfidencesCheckbox, (ActionListener)
                e -> {
                    boolean value = showConfidencesCheckbox.isSelected();
                    AIMsLogger.TRACE("Confidence visibility changed to: " + value);
                    settingsUpdates.put("showConfidences", value);
                    if(settings.isShowConfidences() == value)
                        settingsUpdates.remove("showConfidences");
                }
        );

        addSettingChangeListener(processEveryNthFrameSpinner, (ChangeListener)
                e -> {
                    int value = (int) processEveryNthFrameSpinner.getValue();
                    AIMsLogger.TRACE("Processing every Nth frame changed to: " + value);
                    settingsUpdates.put("processEveryNthFrame", value);
                    if(settings.getProcessEveryNthFrame() == value)
                        settingsUpdates.remove("processEveryNthFrame");
                }
        );

        addSettingChangeListener(bufferThresholdSpinner, (ChangeListener)
                e -> {
                    int value = (int) bufferThresholdSpinner.getValue();
                    AIMsLogger.TRACE("Buffer threshold changed to: " + value);
                    settingsUpdates.put("bufferThreshold", value);
                    if(settings.getBufferThreshold() == value)
                        settingsUpdates.remove("bufferThreshold");
                }
        );

        addSettingChangeListener(confThresholdSlider, (ChangeListener)
                e -> {
                    float value = confThresholdSlider.getValue() / 100f;
                    AIMsLogger.TRACE("Confidence threshold changed to: " + value);
                    settingsUpdates.put("confThreshold", value);
                    if(settings.getConfThreshold() == value)
                        settingsUpdates.remove("confThreshold");
                }
        );

        addSettingChangeListener(logFontSizeSpinner, (ChangeListener) e -> {
            int value = (int) logFontSizeSpinner.getValue();
            AIMsLogger.TRACE("Log font size changed to: " + value);
            settingsUpdates.put("logFontSize", value);

            updateLogPreview();

            // Update preview content
            logFontPreviewPane.setText(generateLogPreviewHTML(value));

            if (settings.getLogFontSize() == value)
                settingsUpdates.remove("logFontSize");
        });
    }

    @Override
    public void setLayout() {
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(noticeLabel)
                        .addComponent(openFolderButton)
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(modelLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(modelSelector, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(labelLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelSelector, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(colorLabel)
                                .addComponent(rInputTextField)
                                .addComponent(gInputTextField)
                                .addComponent(bInputTextField)
                                .addComponent(colorPreviewButton))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(logAddedColorLabel)
                                .addComponent(logAddedRInputTextField)
                                .addComponent(logAddedGInputTextField)
                                .addComponent(logAddedBInputTextField)
                                .addComponent(logAddedColorPreviewButton))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(logRemovedColorLabel)
                                .addComponent(logRemovedRInputTextField)
                                .addComponent(logRemovedGInputTextField)
                                .addComponent(logRemovedBInputTextField)
                                .addComponent(logRemovedColorPreviewButton))
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(boundBoxCheckboxLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(boundingBoxCheckbox)
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(showLabelsCheckboxLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(showLabelsCheckbox)
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(showConfidencesCheckboxLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(showConfidencesCheckbox)
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(processNthLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(processEveryNthFrameSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(bufferThresholdLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bufferThresholdSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(confThresholdLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(confThresholdSlider, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(confThresholdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(logFontSizeLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(logFontSizeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                        .addComponent(logFontPreviewScrollPane)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(noticeLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(openFolderButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(modelLabel)
                                        .addComponent(modelSelector)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(labelLabel)
                                        .addComponent(labelSelector)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(colorLabel)
                                .addComponent(rInputTextField)
                                .addComponent(gInputTextField)
                                .addComponent(bInputTextField)
                                .addComponent(colorPreviewButton))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(logAddedColorLabel)
                                .addComponent(logAddedRInputTextField)
                                .addComponent(logAddedGInputTextField)
                                .addComponent(logAddedBInputTextField)
                                .addComponent(logAddedColorPreviewButton))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(logRemovedColorLabel)
                                .addComponent(logRemovedRInputTextField)
                                .addComponent(logRemovedGInputTextField)
                                .addComponent(logRemovedBInputTextField)
                                .addComponent(logRemovedColorPreviewButton))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(boundBoxCheckboxLabel)
                                        .addComponent(boundingBoxCheckbox)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(showLabelsCheckboxLabel)
                                        .addComponent(showLabelsCheckbox)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(showConfidencesCheckboxLabel)
                                        .addComponent(showConfidencesCheckbox)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(processNthLabel)
                                        .addComponent(processEveryNthFrameSpinner)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(bufferThresholdLabel)
                                        .addComponent(bufferThresholdSpinner)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(confThresholdLabel)
                                        .addComponent(confThresholdSlider)
                                        .addComponent(confThresholdTextField)
                        )
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(logFontSizeLabel)
                                        .addComponent(logFontSizeSpinner)
                        )
                        .addComponent(logFontPreviewScrollPane)
        );
    }

    private JButton createColorPreviewButton(int r, int g, int b, Runnable action) {
        JButton button = new JButton() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(30, 30);
            }
        };
        button.setBackground(new Color(r, g, b));
        button.setMinimumSize(new Dimension(30, 30));
        button.setMaximumSize(new Dimension(30, 30));
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        button.addActionListener(e -> action.run());
        return button;
    }

    private String[] getFilesWithExtension(String extension) {
        File dir = new File(AIMS_MODELS_DIRECTORY);
        if (!dir.exists() || !dir.isDirectory()) return new String[]{};
        List<String> files = new ArrayList<>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isFile() && file.getName().endsWith(extension)) {
                files.add(file.getName());
            }
        }
        return files.toArray(new String[0]);
    }

    private void openAIDirectory() {
        File dir = new File(AIMS_MODELS_DIRECTORY);
        if (!dir.exists()) {
            JOptionPane.showMessageDialog(this, "AI Models directory does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Desktop.getDesktop().open(dir);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to open AI Models directory!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Open Color Chooser Dialog
    private void openColorChooser() {
        JColorChooser colorChooser = new JColorChooser(colorPreviewButton.getBackground());

        removeUnwantedTabs(colorChooser);

        // Create and show a custom color chooser dialog
        JDialog colorDialog = JColorChooser.createDialog(
                this,
                "Choose Bounding Box Color",
                true,
                colorChooser,
                e -> {
                    Color selectedColor = colorChooser.getColor();
                    if (selectedColor != null) {
                        colorPreviewButton.setBackground(selectedColor);
                        rInputTextField.setText(String.valueOf(selectedColor.getRed()));
                        gInputTextField.setText(String.valueOf(selectedColor.getGreen()));
                        bInputTextField.setText(String.valueOf(selectedColor.getBlue()));

                        this.boundingBoxColor = new int[]{selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue()};
                        fireColorChangedEvent("boundingBoxColor", this.boundingBoxColor);                    }
                },
                null
        );

        colorDialog.setVisible(true);
    }

    // Open color chooser for Log Added Color
    private void openLogAddedColorChooser() {
        JColorChooser colorChooser = new JColorChooser(logAddedColorPreviewButton.getBackground());
        removeUnwantedTabs(colorChooser);

        JDialog colorDialog = JColorChooser.createDialog(
                this,
                "Choose Log Added Color",
                true,
                colorChooser,
                e -> {
                    Color selectedColor = colorChooser.getColor();
                    if (selectedColor != null) {
                        logAddedColorPreviewButton.setBackground(selectedColor);
                        logAddedRInputTextField.setText(String.valueOf(selectedColor.getRed()));
                        logAddedGInputTextField.setText(String.valueOf(selectedColor.getGreen()));
                        logAddedBInputTextField.setText(String.valueOf(selectedColor.getBlue()));

                        updateLogPreview();

                        this.logAddedColor = new int[]{
                                selectedColor.getRed(),
                                selectedColor.getGreen(),
                                selectedColor.getBlue()
                        };
                        fireColorChangedEvent("logAddedColor", this.logAddedColor);
                    }
                },
                null
        );

        colorDialog.setVisible(true);
    }

    // Open color chooser for Log Removed Color
    private void openLogRemovedColorChooser() {
        JColorChooser colorChooser = new JColorChooser(logRemovedColorPreviewButton.getBackground());
        removeUnwantedTabs(colorChooser);

        JDialog colorDialog = JColorChooser.createDialog(
                this,
                "Choose Log Removed Color",
                true,
                colorChooser,
                e -> {
                    Color selectedColor = colorChooser.getColor();
                    if (selectedColor != null) {
                        logRemovedColorPreviewButton.setBackground(selectedColor);
                        logRemovedRInputTextField.setText(String.valueOf(selectedColor.getRed()));
                        logRemovedGInputTextField.setText(String.valueOf(selectedColor.getGreen()));
                        logRemovedBInputTextField.setText(String.valueOf(selectedColor.getBlue()));

                        updateLogPreview();

                        this.logRemovedColor = new int[]{
                                selectedColor.getRed(),
                                selectedColor.getGreen(),
                                selectedColor.getBlue()
                        };
                        fireColorChangedEvent("logRemovedColor", this.logRemovedColor);
                    }
                },
                null
        );

        colorDialog.setVisible(true);
    }

    // Ensure only the "Swatches" and "RGB" tabs are visible
    private void removeUnwantedTabs(JColorChooser colorChooser) {
        for (Component comp : colorChooser.getComponents()) {
            if (comp instanceof JTabbedPane tabbedPane) {
                for (int i = tabbedPane.getTabCount() - 1; i >= 0; i--) {
                    String title = tabbedPane.getTitleAt(i);
                    if (!title.equals("Swatches") && !title.equals("RGB")) {
                        tabbedPane.remove(i);
                    }
                }
            }
        }
    }

    private void updateColor(int key) {
        try {
            int[] colorValues = new int[3];

            switch (key) {
                case 1 -> {  // Bounding Box Color
                    colorValues[0] = Integer.parseInt(rInputTextField.getText());
                    colorValues[1] = Integer.parseInt(gInputTextField.getText());
                    colorValues[2] = Integer.parseInt(bInputTextField.getText());
                    boundingBoxColor = validateRGB(colorValues);

                    colorPreviewButton.setBackground(new Color(boundingBoxColor[0], boundingBoxColor[1], boundingBoxColor[2]));
                    fireColorChangedEvent("boundingBoxColor", boundingBoxColor);
                }
                case 2 -> {  // Log Added Color
                    colorValues[0] = Integer.parseInt(logAddedRInputTextField.getText());
                    colorValues[1] = Integer.parseInt(logAddedGInputTextField.getText());
                    colorValues[2] = Integer.parseInt(logAddedBInputTextField.getText());
                    logAddedColor = validateRGB(colorValues);

                    logAddedColorPreviewButton.setBackground(new Color(logAddedColor[0], logAddedColor[1], logAddedColor[2]));
                    fireColorChangedEvent("logAddedColor", logAddedColor);
                }
                case 3 -> {  // Log Removed Color
                    colorValues[0] = Integer.parseInt(logRemovedRInputTextField.getText());
                    colorValues[1] = Integer.parseInt(logRemovedGInputTextField.getText());
                    colorValues[2] = Integer.parseInt(logRemovedBInputTextField.getText());
                    logRemovedColor = validateRGB(colorValues);

                    logRemovedColorPreviewButton.setBackground(new Color(logRemovedColor[0], logRemovedColor[1], logRemovedColor[2]));
                    fireColorChangedEvent("logRemovedColor", logRemovedColor);
                }
                default -> AIMsLogger.WARN("Unknown color key: " + key);
            }

        } catch (NumberFormatException ex) {
            System.err.println("Invalid RGB input. Must be a number between 0-255.");
        }
    }

    // Ensures RGB values are within valid range (0-255)
    private int[] validateRGB(int[] colorValues) {
        for (int i = 0; i < colorValues.length; i++) {
            colorValues[i] = Math.max(0, Math.min(255, colorValues[i]));
        }
        return colorValues;
    }


    public void addAISettingsListener(AISettingsListener listener) {
        listeners.add(listener);
    }

    /**
     * Fires an event to all registered listeners when any color changes.
     */
    private void fireColorChangedEvent(String key, int[] newColor) {
        for (AISettingsListener listener : listeners) {
            listener.onColorChanged(key, newColor);
        }
        updateLogPreview();
    }

    private void handleColorChange(String key, int[] newColor, int[] originalColor) {
        settingsUpdates.put(key, newColor);
        AIMsLogger.TRACE(key + " changed to: " + Arrays.toString(newColor));

        if (Arrays.equals(originalColor, newColor)) {
            settingsUpdates.remove(key);
        }

        updateApplyButtonState();
    }

    private String generateLogPreviewHTML(int fontSize) {
        // Read colors directly from the input fields/buttons instead of ProgramSettings
        int logAddedR = Integer.parseInt(logAddedRInputTextField.getText());
        int logAddedG = Integer.parseInt(logAddedGInputTextField.getText());
        int logAddedB = Integer.parseInt(logAddedBInputTextField.getText());

        int logRemovedR = Integer.parseInt(logRemovedRInputTextField.getText());
        int logRemovedG = Integer.parseInt(logRemovedGInputTextField.getText());
        int logRemovedB = Integer.parseInt(logRemovedBInputTextField.getText());

        String addedHex = String.format("#%02x%02x%02x", logAddedR, logAddedG, logAddedB);
        String removedHex = String.format("#%02x%02x%02x", logRemovedR, logRemovedG, logRemovedB);

        return "<html><body style='color:white; font-size:" + fontSize + "pt; font-family:monospace;'>"
                + "<span style='color:" + addedHex + ";'>[ADDED] 12:35:20 - Tool added: scissors</span><br>"
                + "<span style='color:"+ removedHex  +";'>[REMOVED] 12:35:10 - Tool removed: scissors</span><br>"
                + "<span style='color:" + addedHex + ";'>[ADDED] 12:35:20 - Tool added: scissors</span><br>"
                + "<span style='color:" + addedHex + ";'>[ADDED] 12:35:20 - Tool added: scissors</span><br>"
                + "<span style='color:"+ removedHex  +";'>[REMOVED] 12:35:10 - Tool removed: scissors</span><br>"
                + "</body></html>";
    }

    private void updateLogPreview() {
        int fontSize = (int) logFontSizeSpinner.getValue();
        logFontPreviewPane.setText(generateLogPreviewHTML(fontSize));
    }
}