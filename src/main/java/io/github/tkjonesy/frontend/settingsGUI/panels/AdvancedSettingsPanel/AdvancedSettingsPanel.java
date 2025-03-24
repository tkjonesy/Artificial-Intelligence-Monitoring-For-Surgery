package io.github.tkjonesy.frontend.settingsGUI.panels.AdvancedSettingsPanel;

import io.github.tkjonesy.frontend.settingsGUI.SettingsUI;

import javax.swing.*;
import java.awt.*;

public class AdvancedSettingsPanel extends JPanel implements SettingsUI {

    // Component sections
    private final AdvancedAISection advancedAISection;
    private final DebuggingSection debuggingSection;

    private final JScrollPane scrollPane;
    private final JPanel contentPanel;


    public AdvancedSettingsPanel() {
        // Create content panel to hold all sections
        contentPanel = new JPanel();

        // Create sections
        advancedAISection = new AdvancedAISection();
        debuggingSection = new DebuggingSection();

        // Set up the content panel
        setupContentPanel();

        // Create scroll pane with the content panel
        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null); // Remove border for cleaner look
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Set up this panel's layout
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        initListeners();
    }

    /**
     * Sets up the content panel with all sections
     */
    private void setupContentPanel() {
        // Create a separator for visual distinction between sections
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(80, 80, 80));

        // Set the layout for the content panel
        GroupLayout layout = new GroupLayout(contentPanel);
        contentPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(advancedAISection)
                        .addComponent(separator)
                        .addComponent(debuggingSection)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(advancedAISection)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(separator, GroupLayout.PREFERRED_SIZE, 1, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(debuggingSection)
        );
    }

    @Override
    public void initListeners() {
        // Add a component listener to update scroll bars when the panel is resized
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                // If content fits, hide scrollbars
                Dimension viewSize = scrollPane.getViewport().getExtentSize();
                Dimension contentSize = contentPanel.getPreferredSize();

                boolean needsVerticalScrollbar = contentSize.height > viewSize.height;
                scrollPane.setVerticalScrollBarPolicy(needsVerticalScrollbar ?
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED :
                        JScrollPane.VERTICAL_SCROLLBAR_NEVER);

                // Repaint to update the scrollbar visibility
                scrollPane.revalidate();
                scrollPane.repaint();
            }
        });
    }

    @Override
    public void setLayout() {
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        // Create a separator for visual distinction between sections
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(80, 80, 80));

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(advancedAISection)
                        .addComponent(separator)
                        .addComponent(debuggingSection)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(advancedAISection)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(separator, GroupLayout.PREFERRED_SIZE, 1, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(debuggingSection)
        );
    }
}