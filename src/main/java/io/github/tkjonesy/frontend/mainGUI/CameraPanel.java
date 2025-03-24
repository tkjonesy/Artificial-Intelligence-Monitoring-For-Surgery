package io.github.tkjonesy.frontend.mainGUI;

import io.github.tkjonesy.frontend.App;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

@Getter
public class CameraPanel extends JPanel {
    private final App appInstance;
    private final JLabel cameraFeed;

    public CameraPanel(LayoutManager layoutManager, App appInstance){
        super(layoutManager);
        this.appInstance = appInstance;
        this.setBorder(BorderFactory.createTitledBorder("Camera"));
        cameraFeed = new JLabel("");
        cameraFeed.setMinimumSize(new Dimension(0, 0));
        cameraFeed.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        this.add(cameraFeed, BorderLayout.CENTER);
    }
}
