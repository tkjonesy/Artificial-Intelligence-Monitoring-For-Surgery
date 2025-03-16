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
import io.github.tkjonesy.frontend.mainGUI.LoggingPanel;
import io.github.tkjonesy.frontend.utils.*;
import io.github.tkjonesy.frontend.miscGUI.SplashScreen;
import io.github.tkjonesy.frontend.utils.cameraGrabber.CameraGrabber;
import io.github.tkjonesy.frontend.utils.cameraGrabber.MacOSCameraGrabber;
import io.github.tkjonesy.frontend.utils.cameraGrabber.WindowsCameraGrabber;
import io.github.tkjonesy.utils.ErrorDialogManager;
import io.github.tkjonesy.utils.Paths;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.utils.models.LogHandler;
import io.github.tkjonesy.utils.models.SessionHandler;
import io.github.tkjonesy.utils.settings.ProgramSettings;
import io.github.tkjonesy.utils.settings.SettingsLoader;
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

        // Display the splash screen
        splashScreen = new SplashScreen();
        splashScreen.showSplash();

        // Load OpenCV
        Loader.load(opencv_core.class);

        System.getProperty("org.bytedeco.javacpp.maxphysicalbytes", "0");
        System.getProperty("org.bytedeco.javacpp.maxbytes", "0");
        opencv_core.setNumThreads(1);
    }

    private final Thread cameraFetcherThread;
    private CameraPanel cameraPanel;
    private LoggingPanel loggingPanel;

    @Getter
    private final SessionHandler sessionHandler;
    private ProgramSettings settings;

    private final FrameManager frameManager;
    @Getter
    @Setter
    private JTextPane logTextPane;

    public App() {
        instance = this;

        initializeSettings();

        collectAvailableCameras();

        // Initialize the directories for AIMs
        try{
            SettingsLoader.initializeAIMsDirectories();
        }catch (RuntimeException e){
            AIMsLogger.ERROR("Failed to initialize AIMs directories. Exiting application.");
            ErrorDialogManager.displayErrorDialogFatal("Failed to initialize AIMs directories. Exiting application.");
        }

        // Initialize the GUI components and listeners
        initComponents();
        initListeners();

        // Initialize the session handler, log handler, and ONNX runner
        LogHandler logHandler = new LogHandler(loggingPanel.getLogTextPane());
        this.sessionHandler = new SessionHandler(logHandler);
        onnxRunner = new OnnxRunner(LogHandler.getINFERENCE_LOG_QUEUE());

        updateCamera(settings.getCameraDeviceId());

        // Camera fetcher thread task
        frameManager = new FrameManager(this.cameraPanel.getCameraFeed(), camera, onnxRunner, sessionHandler);
        cameraFetcherThread = new Thread(frameManager);
        cameraFetcherThread.start();

        // Close the splash screen and display the application
        splashScreen.closeSplash();
        this.setVisible(true);
    }

    private void initializeSettings(){
        // Load settings from file
        this.settings = SettingsLoader.loadSettings();

        if(settings == null) {
            AIMsLogger.ERROR("Failed to load settings from file. Exiting application.");
            ErrorDialogManager.displayErrorDialogFatal("Failed to load settings from file. Exiting application.");
        }

        ProgramSettings.setCurrentSettings(settings);
        AIMsLogger.initialize(ProgramSettings.getCurrentSettings());

        AIMsLogger.INFO("Settings: " + settings);
    }

    private void collectAvailableCameras(){
        // Load the camera devices from the user's system
        CameraGrabber grabber = CameraGrabber.createForPlatform();

        AVAILABLE_CAMERAS = grabber.getCameraNames();
    }

    private void initComponents() {

        // Titling, sizing, and exit actions
        this.setTitle("AIM: Surgical");
        this.setMinimumSize(new Dimension(746, 401));
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Icon
        try(InputStream stream = getClass().getResourceAsStream(Paths.LOGO32_PATH)) {
            if(stream == null) {
                throw new IOException("Resource not found: " + Paths.LOGO32_PATH);
            }

            ImageIcon appIcon = new ImageIcon(ImageIO.read(stream));
            this.setIconImage(appIcon.getImage());
        } catch (Exception ignored) {}

        // GUI Panels
        cameraPanel = new CameraPanel(new BorderLayout(), instance);
        loggingPanel = new LoggingPanel();
        buttonPanel = new ButtonPanel(instance);

        // Window Layout
        this.setLayout(new GridBagLayout());
        this.add(cameraPanel, createConstraints(0, 0, 0.5, 1));
        this.add(loggingPanel, createConstraints(1, 0, 0.5, 0.5));
        GridBagConstraints buttonPanelConstraints = createConstraints(0, 1, 1, 0.05);
        buttonPanelConstraints.gridwidth = 2;
        buttonPanelConstraints.fill = GridBagConstraints.VERTICAL;
        this.add(buttonPanel, buttonPanelConstraints);
        this.pack();
        this.setLocationRelativeTo(null); // Center application
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
        // Window Event Listener
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
                    if(cameraFetcherThread != null) cameraFetcherThread.interrupt();
                    AIMsLogger.INFO("Closing camera access...");
                    if (camera != null && camera.isOpened())
                        camera.release();

                    AIMsLogger.INFO("Done cleanup process.");

                    App.this.dispose();
                    System.exit(0);
                } else {
                    AIMsLogger.INFO("Exit cancelled.");
                }
            }
        });

        this.addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        loggingPanel.setPreferredSize(new Dimension(App.this.getWidth() / 3, loggingPanel.getHeight()));
                    }
                }
        );
    }

    public void updateCamera(int cameraId) {
        try{
            camera = new VideoCapture(cameraId);
            if (!camera.isOpened()) {
                cameraId = 0;
                camera = new VideoCapture(cameraId);
                if (!camera.isOpened()) {
                    throw new CvException("Unable to open camera with ID: " + cameraId);
                }
            }
            if(frameManager != null){
                frameManager.setCamera(camera);
            }

        }catch (CvException e) {
            String errorMessage = "Failed to open camera with ID: " + cameraId;
            ErrorDialogManager.displayErrorDialog(errorMessage);
            AIMsLogger.ERROR(errorMessage);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
        } catch (Exception e) {
            AIMsLogger.ERROR("Unable to set Look and Feel to system default.");
        }

        SwingUtilities.invokeLater(App::new);
    }
}