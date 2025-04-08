package io.github.tkjonesy.frontend.mainGUI;

import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.utils.models.LogHandler;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Getter
public class LoggingPanel extends JPanel {
    private static final Color CHARCOAL = new Color(30, 31, 34);
    private static final Color BUTTON_COLOR = new Color(50, 50, 55);
    private static final Color BUTTON_TEXT_COLOR = new Color(200, 200, 200);

    private final JTextPane logTextPane;

    @Getter
    private final JButton clearLoggerButton;

    /**
     * Creates an instance of {@code LoggingPannel}, initializing the log display area
     */
    public LoggingPanel() {
        this.setBorder(BorderFactory.createTitledBorder("Tracking Log"));
        this.logTextPane = new JTextPane();
        this.logTextPane.setEditable(false);
        this.logTextPane.setContentType("text/html");
        this.logTextPane.setBackground(CHARCOAL);

        JScrollPane scrollPane = new JScrollPane(this.logTextPane);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Create the run button
        clearLoggerButton = new JButton("Clear Logger");
        clearLoggerButton.setBackground(BUTTON_COLOR);
        clearLoggerButton.setForeground(BUTTON_TEXT_COLOR);
        clearLoggerButton.setFocusPainted(false);
        clearLoggerButton.setBorderPainted(false);
        clearLoggerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AIMsLogger.INFO("Logger cleared.");
                LogHandler.clearLogPane();
            }
        });

        GroupLayout layout = new GroupLayout(this);
        layout.setAutoCreateContainerGaps(true);

        // Update the horizontal group to include both components
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(scrollPane)
                        .addComponent(clearLoggerButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        // Update the vertical group to stack components
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(scrollPane)
                        .addGap(5)
                        .addComponent(clearLoggerButton, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
        );

        this.setLayout(layout);
    }
}