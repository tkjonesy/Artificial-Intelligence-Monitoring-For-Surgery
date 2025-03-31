package io.github.tkjonesy.frontend;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import io.github.tkjonesy.ONNX.models.OnnxRunner;
import io.github.tkjonesy.frontend.mainGUI.ButtonPanel;
import io.github.tkjonesy.frontend.mainGUI.CameraPanel;
import io.github.tkjonesy.frontend.mainGUI.DebugConsole;
import io.github.tkjonesy.frontend.mainGUI.LoggingPanel;
import io.github.tkjonesy.frontend.utils.*;
import io.github.tkjonesy.frontend.miscGUI.SplashScreen;
import io.github.tkjonesy.frontend.utils.cameraGrabber.CameraGrabber;
import io.github.tkjonesy.utils.AppVersion;
import io.github.tkjonesy.utils.DialogManager;
import io.github.tkjonesy.utils.ErrorUtils;
import io.github.tkjonesy.utils.Paths;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.utils.models.LogHandler;
import io.github.tkjonesy.utils.models.SessionHandler;
import io.github.tkjonesy.utils.settings.ProgramSettings;
import io.github.tkjonesy.utils.settings.SettingsLoader;
import io.github.tkjonesy.ONNX.enums.InferenceLogEnum;
import lombok.Getter;
import lombok.Setter;

import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.javacpp.Loader;
import org.opencv.core.CvException;

public class App extends JFrame {

    @Getter
    private static App instance;
    @Getter
    private static OnnxRunner onnxRunner = null;
    @Getter
    @Setter
    private static VideoCapture camera;

    @Getter
    private ButtonPanel buttonPanel;

    public static HashMap<String, Integer> AVAILABLE_CAMERAS = null;
    private static final SplashScreen splashScreen;
    static {
        splashScreen = new SplashScreen();

        SwingUtilities.invokeLater(() -> {
            splashScreen.showSplash();
            if (ProgramSettings.getCurrentSettings() != null && ProgramSettings.getCurrentSettings().isDebugMode()) {
                DebugConsole.getInstance().setVisible(true);
            }
        });

        // Load OpenCV
        Loader.load(opencv_core.class);

        System.setProperty("org.bytedeco.javacpp.maxphysicalbytes", "0");
        System.setProperty("org.bytedeco.javacpp.maxbytes", "0");
        opencv_core.setNumThreads(1);
    }

    private Thread cameraFetcherThread;
    private CameraPanel cameraPanel;
    private LoggingPanel loggingPanel;

    @Getter
    private SessionHandler sessionHandler;
    private ProgramSettings settings;

    private FrameManager frameManager;
    @Getter
    @Setter
    private JTextPane logTextPane;

    public App() {
        instance = this;

        try {
            SettingsLoader.initializeAIMsDirectories();
        } catch (RuntimeException e) {
            AIMsLogger.ERROR("Failed to initialize AIMs directories. Exiting application.");
            DialogManager.displayErrorDialogFatal("Failed to initialize AIMs directories. Exiting application.");
        }

        new Thread(() -> {
            initializeSettingsAndLogger();

            if (settings.isDebugMode()) {
                SwingUtilities.invokeLater(() -> DebugConsole.getInstance().setVisible(true));
            }

            AIMsLogger.INFO("Application Version: " + AppVersion.getCOMMIT_ID_FULL());

            collectAvailableCameras();


            SwingUtilities.invokeLater(() -> {
                initComponents();
                initListeners();

                LogHandler logHandler = new LogHandler(loggingPanel.getLogTextPane());
                sessionHandler = new SessionHandler(logHandler);
                onnxRunner = new OnnxRunner(LogHandler.getINFERENCE_LOG_QUEUE());

                updateCamera(settings.getCameraDeviceId());

                frameManager = new FrameManager(cameraPanel.getCameraFeed(), camera, onnxRunner, sessionHandler);
                cameraFetcherThread = new Thread(frameManager);
                cameraFetcherThread.start();

                splashScreen.closeSplash();
                App.this.setVisible(true);
            });
        }).start();
    }

    private void initializeSettingsAndLogger() {
        this.settings = SettingsLoader.loadSettings();

        InferenceLogEnum.LOG_ADDED.updateColor(settings.getLogAddedColor());
        InferenceLogEnum.LOG_REMOVED.updateColor(settings.getLogRemovedColor());

        if (settings == null) {
            AIMsLogger.ERROR("Failed to load settings from file. Exiting application.");
            DialogManager.displayErrorDialogFatal("Failed to load settings from file. Exiting application.");
        }

        ProgramSettings.setCurrentSettings(settings);
        AIMsLogger.initialize(ProgramSettings.getCurrentSettings());

        AIMsLogger.INFO("Settings: " + settings);
    }

    private void collectAvailableCameras() {
        CameraGrabber grabber = CameraGrabber.createForPlatform();
        AVAILABLE_CAMERAS = grabber.getCameraNames();
    }

    private void initComponents() {
        updateTitle();
        this.setMinimumSize(new Dimension(746, 401));
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        try (InputStream stream = getClass().getResourceAsStream(Paths.LOGO32_PATH)) {
            if (stream == null) throw new IOException("Resource not found: " + Paths.LOGO32_PATH);
            ImageIcon appIcon = new ImageIcon(ImageIO.read(stream));
            this.setIconImage(appIcon.getImage());
        } catch (Exception ignored) {}

        cameraPanel = new CameraPanel(new BorderLayout(), instance);
        loggingPanel = new LoggingPanel();
        buttonPanel = new ButtonPanel(instance);

        this.setLayout(new GridBagLayout());
        this.add(cameraPanel, createConstraints(0, 0, 0.5, 1));
        this.add(loggingPanel, createConstraints(1, 0, 0.5, 0.5));
        GridBagConstraints buttonPanelConstraints = createConstraints(0, 1, 1, 0.05);
        buttonPanelConstraints.gridwidth = 2;
        buttonPanelConstraints.fill = GridBagConstraints.VERTICAL;
        this.add(buttonPanel, buttonPanelConstraints);
        this.pack();
        this.setLocationRelativeTo(null);
    }

    public void updateTitle() {
        String title = settings.isDebugMode() ? "AIMs - " + AppVersion.getCOMMIT_ID_ABBREV() : "AIMs";
        this.setTitle(title);
    }

    private GridBagConstraints createConstraints(int gridX, int gridY, double weightX, double weightY) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridX;
        c.gridy = gridY;
        c.weightx = weightX;
        c.weighty = weightY;
        c.fill = GridBagConstraints.BOTH;
        return c;
    }

    private void initListeners() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirmation = JOptionPane.showConfirmDialog(App.this,
                        "Are you sure you want to quit?",
                        "Confirm Exit", JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    SettingsLoader.saveSettings(settings);
                    AIMsLogger.INFO("Beginning cleanup Process...");
                    AIMsLogger.INFO("Stopping camera feed thread...");
                    if (cameraFetcherThread != null) cameraFetcherThread.interrupt();
                    AIMsLogger.INFO("Closing camera access...");
                    if (camera != null && camera.isOpened()) camera.release();
                    AIMsLogger.INFO("Done cleanup process.");
                    App.this.dispose();
                    System.exit(0);
                } else {
                    AIMsLogger.INFO("Exit cancelled.");
                }
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                loggingPanel.setPreferredSize(new Dimension(App.this.getWidth() / 3, loggingPanel.getHeight()));
            }
        });
    }

    public void updateCamera(int cameraId) {
        try {
            camera = new VideoCapture(cameraId);
            if (!camera.isOpened()) {
                cameraId = 0;
                camera = new VideoCapture(cameraId);
                if (!camera.isOpened()) {
                    throw new CvException("Unable to open camera with ID: " + cameraId);
                }
            }
            if (frameManager != null) {
                frameManager.setCamera(camera);
            }
        } catch (CvException e) {
            String errorMessage = "Failed to open camera with ID: " + cameraId;
            DialogManager.displayErrorDialog(errorMessage);
            AIMsLogger.ERROR(errorMessage);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
        } catch (Exception e) {
            AIMsLogger.ERROR("Unable to set Look and Feel to system default.");
        }

        try {
            SwingUtilities.invokeLater(() -> {
                try {
                    new App();
                } catch (Exception e) {
                    ErrorUtils.saveExceptionToFile(e);
                    DialogManager.displayErrorDialogFatal("An unknown error has occurred. The stacktrace has been saved to the error directory.");
                }
            });
        } catch (Exception e) {
            ErrorUtils.saveExceptionToFile(e);
            DialogManager.displayErrorDialogFatal("An unknown error has occurred. The stacktrace has been saved to the error directory.");
        }
    }
}