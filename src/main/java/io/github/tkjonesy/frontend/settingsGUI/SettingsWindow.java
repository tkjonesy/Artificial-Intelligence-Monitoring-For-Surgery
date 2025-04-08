package io.github.tkjonesy.frontend.settingsGUI;

import io.github.tkjonesy.frontend.settingsGUI.panels.AISettingsPanel;
import io.github.tkjonesy.frontend.settingsGUI.panels.AdvancedSettingsPanel.AdvancedSettingsPanel;
import io.github.tkjonesy.frontend.settingsGUI.panels.CameraSettingsPanel;
import io.github.tkjonesy.frontend.settingsGUI.panels.StorageSettingsPanel;
import io.github.tkjonesy.utils.AppVersion;
import io.github.tkjonesy.utils.Paths;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.utils.settings.ProgramSettings;
import lombok.Getter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class SettingsWindow extends JDialog implements SettingsUI {

    private final ProgramSettings settings = ProgramSettings.getCurrentSettings();
    @Getter
    private static final HashMap<String, Object> settingsUpdates = new HashMap<>();

    private JButton confirmButton;
    private JButton cancelButton;
    private static JButton applyButton;
    private JPanel buttonPanel;

    private JTabbedPane settingSelector;

    private JLabel versionLabel;

    private static final Color OCEAN = new Color(55, 90, 129);

    /**
     * Constructs a new {@code SettingsWindow} with specified parent frame
     * @param parent The parent frame in which the settings window is to be displayed
     */
    public SettingsWindow(JFrame parent) {
        super(parent, "AIM Settings", true);
        initComponents();
        setLayout();
        initListeners();
        this.setVisible(true);
    }

    /**
     * Initializes the components of the settings window
     * Includes the layout, buttons, and tabs
     */
    private void initComponents() {
        // Sizing, and exit actions
        this.setMinimumSize(new Dimension(640, 720));
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Icon
        try (InputStream stream = getClass().getResourceAsStream(Paths.LOGO32_PATH)) {
            if (stream == null) throw new IOException("Resource not found: " + Paths.LOGO32_PATH);
            ImageIcon appIcon = new ImageIcon(ImageIO.read(stream));
            this.setIconImage(appIcon.getImage());
        } catch (Exception ignored) {}

        CameraSettingsPanel cameraPanel = new CameraSettingsPanel(settings);
        StorageSettingsPanel storagePanel = new StorageSettingsPanel();
        AISettingsPanel modelPanel = new AISettingsPanel();
        AdvancedSettingsPanel advancedPanel = new AdvancedSettingsPanel();

        this.buttonPanel = new JPanel();
        confirmButton = new JButton("OK");
        confirmButton.setBackground(OCEAN);
        cancelButton = new JButton("Cancel");
        applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        this.settingSelector = new JTabbedPane(SwingConstants.LEFT);
        settingSelector.addTab("Camera", cameraPanel);
        settingSelector.addTab("Storage", storagePanel);
        settingSelector.addTab("AI Model", modelPanel);
        settingSelector.addTab("Advanced", advancedPanel);

        this.versionLabel = new JLabel("v " + AppVersion.getCOMMIT_ID_FULL());
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        versionLabel.setForeground(Color.GRAY);
        versionLabel.setToolTipText("Click to copy version to clipboard");
    }

    /**
     * Method to enable/disable the Apply button
     */
    public static void updateApplyButtonState() {
        applyButton.setEnabled(!settingsUpdates.isEmpty());
    }

    /**
     * Initializes listeners for various components in the settings window
     */
    public void initListeners() {
        confirmButton.addActionListener(e -> {handleCloseAttempt();});

        cancelButton.addActionListener(e -> {handleCancelAttempt();});

        applyButton.addActionListener(e -> {applyChanges();});

        versionLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection selection = new StringSelection(AppVersion.getCOMMIT_ID_FULL());
                clipboard.setContents(selection, null);
                JOptionPane.showMessageDialog(null, "Version copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                versionLabel.setForeground(Color.LIGHT_GRAY);
                versionLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                versionLabel.setForeground(Color.GRAY);
            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleCloseAttempt();
            }
        });
    }

    @Override
    public void setLayout() {
        GroupLayout windowLayout = new GroupLayout(this.getContentPane());
        windowLayout.setAutoCreateContainerGaps(true);
        windowLayout.setHorizontalGroup(
                windowLayout.createSequentialGroup()
                        .addGroup(
                                windowLayout.createParallelGroup()
                                        .addComponent(settingSelector)
                                        .addComponent(buttonPanel)
                        )
        );
        windowLayout.setVerticalGroup(
                windowLayout.createSequentialGroup()
                        .addComponent(settingSelector)
                        .addComponent(buttonPanel)
        );
        this.setLayout(windowLayout);
        this.pack();
        this.setLocationRelativeTo(null);

        GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
        buttonPanelLayout.setAutoCreateContainerGaps(true);
        buttonPanelLayout.setHorizontalGroup(
                buttonPanelLayout.createSequentialGroup()
                        .addComponent(versionLabel)
                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(confirmButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(cancelButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(applyButton)
        );
        buttonPanelLayout.setVerticalGroup(
                buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(versionLabel)
                        .addComponent(confirmButton)
                        .addComponent(cancelButton)
                        .addComponent(applyButton)
        );
        buttonPanel.setLayout(buttonPanelLayout);
    }

    /**
     * Adds a change listener to a specified component
     * @param component The component which the listener will be added to
     * @param listener The change listener to be attached to the component
     * @param <T> Type of listener
     */
    public static <T extends EventListener> void addSettingChangeListener(JComponent component, T listener) {
        if (component instanceof AbstractButton button && listener instanceof ActionListener actionListener) {
            button.addActionListener(e -> {
                actionListener.actionPerformed(e);
                updateApplyButtonState();
            });
        } else if (component instanceof JCheckBox checkBox && listener instanceof ItemListener itemListener) {
            checkBox.addItemListener(e -> {
                itemListener.itemStateChanged(e);
                updateApplyButtonState();
            });
        } else if (component instanceof JTextField textField && listener instanceof PropertyChangeListener propertyChangeListener) {
            textField.addPropertyChangeListener(evt -> {
                propertyChangeListener.propertyChange(evt);
                updateApplyButtonState();
            });
        } else if (component instanceof JComboBox<?> comboBox && listener instanceof ActionListener actionListener) {
            comboBox.addActionListener(e -> {
                actionListener.actionPerformed(e);
                updateApplyButtonState();
            });
        } else if (component instanceof JSlider slider && listener instanceof ChangeListener changeListener) {
            slider.addChangeListener(e -> {
                changeListener.stateChanged(e);
                updateApplyButtonState();
            });
        } else if (component instanceof JSpinner spinner && listener instanceof ChangeListener changeListener) {
            spinner.addChangeListener(e -> {
                changeListener.stateChanged(e);
                updateApplyButtonState();
            });
        } else {
            throw new IllegalArgumentException("Unsupported listener type for component: " + component.getClass().getName());
        }
    }

    /**
     * Handles an attempt to close settings when there are unsaved changes
     * Shows a popup when closing window to confirm the action
     */
    private void handleCloseAttempt() {
        if (!settingsUpdates.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "You have unsaved changes. Do you want to save before exiting?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                applyChanges();
                dispose();
            } else if (choice == JOptionPane.NO_OPTION) {
                settingsUpdates.clear();
                dispose();
            }

        } else {
            dispose();
        }
    }

    /**
     * Handles attempt to cancel changes and close settings when there are unsaved changes
     * Shows popup to confirm the action
     */
    private void handleCancelAttempt() {
        if (!settingsUpdates.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Discard unsaved changes?",
                    "Cancel Changes",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                cancelChanges();
                dispose();
            }
        } else {
            dispose();
        }
    }

    /**
     * Applies the settings changes made in the window
     */
    private void applyChanges() {
        AIMsLogger.INFO("Applying settings...");
        settings.updateSettings(settingsUpdates);
        settingsUpdates.clear();
        updateApplyButtonState();
    }

    /**
     * Cancels any pending changes made in the settings window
     */
    private void cancelChanges() {
        settingsUpdates.clear();
    }
}

// Credit for the original settings GUI code goes to @HunterHerbst