package io.github.tkjonesy.frontend.settingsGUI.panels.AdvancedSettingsPanel;

import io.github.tkjonesy.frontend.settingsGUI.SettingsUI;
import io.github.tkjonesy.frontend.settingsGUI.SettingsWindow;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.utils.settings.ProgramSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;

import static io.github.tkjonesy.frontend.settingsGUI.SettingsWindow.addSettingChangeListener;

public class DebuggingSection extends JPanel implements SettingsUI {
    private final ProgramSettings settings = ProgramSettings.getCurrentSettings();
    private static final HashMap<String, Object> settingsUpdates = SettingsWindow.getSettingsUpdates();

    // Debug settings components
    private final JLabel debugSectionLabel;
    private final JLabel debugModeLabel;
    private final JLabel continuousLoggingLabel;
    private final JLabel debugInfoLabel;
    private final JLabel showInferenceTimeLabel;

    private final JCheckBox debugModeCheckbox;
    private final JCheckBox continuousLoggingCheckbox;
    private final JCheckBox showInferenceTimeCheckbox;

    public DebuggingSection() {
        // Debug Section
        this.debugSectionLabel = new JLabel("<html><b>Debug Settings</b></html>");
        debugSectionLabel.setForeground(Color.WHITE);

        // Debug Mode (Checkbox)
        this.debugModeLabel = new JLabel("Enable Debug Mode:");
        debugModeCheckbox = new JCheckBox("");
        debugModeCheckbox.setSelected(settings.isDebugMode());
        debugModeCheckbox.setToolTipText("Enable debug mode to see detailed logging information");

        // Continuous Logging (Checkbox)
        this.continuousLoggingLabel = new JLabel("Continuous Background Logging:");
        continuousLoggingCheckbox = new JCheckBox("");
        continuousLoggingCheckbox.setSelected(settings.isContinuousLogging());
        continuousLoggingCheckbox.setToolTipText("Keep logging even when debug mode is off");

        // Info label
        this.debugInfoLabel = new JLabel("<html><small>Continuous logging keeps logs running in the background even when debug mode is off.<br>This allows you to see logs retrospectively when you enable debug mode later.</small></html>");
        debugInfoLabel.setForeground(new Color(180, 180, 180));

        // Show Inference Time (Checkbox)
        this.showInferenceTimeLabel = new JLabel("Show Inference Time:");
        showInferenceTimeCheckbox = new JCheckBox("");
        showInferenceTimeCheckbox.setSelected(settings.isShowInferenceTime());
        showInferenceTimeCheckbox.setToolTipText("Display the time taken for each inference");

        setLayout();
        initListeners();
    }

    @Override
    public void initListeners() {
        // Debug Settings Listeners
        addSettingChangeListener(debugModeCheckbox, (ActionListener)
                e -> {
                    boolean value = debugModeCheckbox.isSelected();
                    AIMsLogger.TRACE("Debug mode set to " + value);
                    settingsUpdates.put("debugMode", value);
                    if(settings.isDebugMode() == value)
                        settingsUpdates.remove("debugMode");
                }
        );

        addSettingChangeListener(continuousLoggingCheckbox, (ActionListener)
                e -> {
                    boolean value = continuousLoggingCheckbox.isSelected();
                    AIMsLogger.TRACE("Continuous logging set to " + value);
                    settingsUpdates.put("continuousLogging", value);
                    if(settings.isContinuousLogging() == value)
                        settingsUpdates.remove("continuousLogging");
                }
        );

        addSettingChangeListener(showInferenceTimeCheckbox, (ActionListener)
                e -> {
                    boolean value = showInferenceTimeCheckbox.isSelected();
                    AIMsLogger.TRACE("Show inference time set to " + value);
                    settingsUpdates.put("showInferenceTime", value);
                    if(settings.isShowInferenceTime() == value)
                        settingsUpdates.remove("showInferenceTime");
                }
        );
    }

    @Override
    public void setLayout() {
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(debugSectionLabel)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(debugModeLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(debugModeCheckbox))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(continuousLoggingLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(continuousLoggingCheckbox))
                        .addComponent(debugInfoLabel)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(showInferenceTimeLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(showInferenceTimeCheckbox))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(debugSectionLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(debugModeLabel)
                                .addComponent(debugModeCheckbox))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(continuousLoggingLabel)
                                .addComponent(continuousLoggingCheckbox))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(debugInfoLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(showInferenceTimeLabel)
                                .addComponent(showInferenceTimeCheckbox))
        );
    }
}