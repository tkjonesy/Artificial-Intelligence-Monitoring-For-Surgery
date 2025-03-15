package io.github.tkjonesy.frontend.models;

import io.github.tkjonesy.ONNX.Detection;
import io.github.tkjonesy.ONNX.ImageUtil;
import io.github.tkjonesy.ONNX.models.OnnxOutput;
import io.github.tkjonesy.ONNX.models.OnnxRunner;

import io.github.tkjonesy.frontend.App;
import io.github.tkjonesy.utils.logging.AIMsLogger;
import io.github.tkjonesy.utils.models.FileSession;
import io.github.tkjonesy.utils.models.SessionHandler;
import io.github.tkjonesy.utils.settings.ProgramSettings;
import lombok.Setter;
import org.bytedeco.javacpp.BytePointer;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.opencv_videoio.VideoWriter;

import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_videoio.*;

import org.bytedeco.opencv.global.opencv_core;


import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;


public class FrameHandler implements Runnable {

    private final JLabel cameraFeed;
    @Setter
    private VideoCapture camera;
    private final Timer timer;
    private final SessionHandler sessionHandler;
    private final OnnxRunner onnxRunner;

    // Executors for inference, display, and recording
    private final ExecutorService inferenceExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService processExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService displayExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService recordingExecutor = Executors.newSingleThreadExecutor();

    // Queue for frames
    private final LinkedBlockingQueue<Mat> rawFrameQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Mat> processedFrameQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Mat> recordingFrameQueue = new LinkedBlockingQueue<>();

    private volatile boolean isRunning = true;
    private long lastFrameTimestamp = 0;
    private final Scalar redColor = new Scalar(0, 0, 255, 0);


    // Current state
    private volatile List<Detection> currentDetections = new ArrayList<>();
    private volatile int frameCounter = 0;


    private final ProgramSettings settings = ProgramSettings.getCurrentSettings();

    public FrameHandler(JLabel cameraFeed, VideoCapture camera, OnnxRunner onnxRunner, SessionHandler sessionHandler) {
        this.cameraFeed = cameraFeed;
        this.camera = camera;
        this.timer = new Timer();
        this.onnxRunner = onnxRunner;
        this.sessionHandler = sessionHandler;

        AIMsLogger.trace("Starting Frame Processing Thread");
        startFrameProcessingThread();
        AIMsLogger.trace("Starting Display Thread");
        startDisplayThread();
        AIMsLogger.trace("Starting Recording Thread");
        startRecordingThread();
    }

    /**
     * Convert Bytedeco Mat to BufferedImage
     */
    private static BufferedImage cvt2bi(Mat frame) {
        // Dimensions
        int width = frame.cols();
        int height = frame.rows();
        int channels = frame.channels();

        BytePointer dataPtr = frame.data();
        byte[] b = new byte[width * height * channels];
        dataPtr.get(b);

        // Determine the correct BufferedImage type
        int type = (channels > 1) ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage image = new BufferedImage(width, height, type);

        // Copy the raw bytes into the BufferedImage
        byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);

        return image;
    }

    @Override
    public void run() {
        // Configure camera resolution and FPS
        camera.set(CAP_PROP_FRAME_WIDTH, cameraFeed.getWidth());
        camera.set(CAP_PROP_FRAME_HEIGHT, cameraFeed.getHeight());
        camera.set(CAP_PROP_FPS, settings.getCameraFps());

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (Thread.currentThread().isInterrupted() || !isRunning) {
                    this.cancel();
                    return;
                }

                if (camera != App.getCamera()) {
                    AIMsLogger.info("Camera has been updated, changing to new camera");
                    camera = App.getCamera();

                    try{
                        camera.set(CAP_PROP_FRAME_WIDTH, cameraFeed.getWidth());
                        camera.set(CAP_PROP_FRAME_HEIGHT, cameraFeed.getHeight());
                        camera.set(CAP_PROP_FPS, settings.getCameraFps());
                    }catch (Exception e) {
                        AIMsLogger.error("Error updating camera: " + e.getMessage());
                    }


                    AIMsLogger.info("Camera updated");
                }

                try {
                    Mat frame = new Mat();
                    if (camera.read(frame)) {
                        if(!rawFrameQueue.offer(frame)){
                            frame.release();
                        }
                    }else{
                        frame.release();
                    }
                }catch (Exception e) {
                    AIMsLogger.error("Error capturing frame: " + e.getMessage());
                }
            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000 / settings.getCameraFps());
    }

    private void startFrameProcessingThread(){
        processExecutor.submit(() -> {
            while(isRunning && !Thread.currentThread().isInterrupted()){

                try(Mat frame = rawFrameQueue.poll(50, TimeUnit.MILLISECONDS)){
                    if(frame == null)
                        continue;
                    if(settings.isMirrorCamera()){
                        opencv_core.flip(frame, frame, 1);
                    }

                    if (++frameCounter % settings.getProcessEveryNthFrame() == 0) {
                        Mat inferenceFrame = frame.clone();
                        inferenceExecutor.submit(() -> {
                            try {

                                OnnxOutput onnxOutput = onnxRunner.runInference(inferenceFrame);
                                currentDetections = onnxOutput.getDetectionList();

                                if(sessionHandler.isSessionActive()) {
                                    onnxRunner.processDetections(currentDetections);
                                }
                            } finally {
                                inferenceFrame.release();
                            }
                        });
                        frameCounter = 0;
                    }

                    Mat processedFrame = frame.clone();

                    // Overlay predictions & resize
                    if(settings.isShowBoundingBoxes())
                        ImageUtil.drawPredictions(processedFrame, currentDetections);

                    resize(processedFrame, processedFrame, new Size(cameraFeed.getWidth(), cameraFeed.getHeight()));

                    int settingsRotation = settings.getCameraRotation();
                    int ROTA = 3;
                    switch (settingsRotation) {
                        case 90 -> ROTA = opencv_core.ROTATE_90_CLOCKWISE;
                        case 180 -> ROTA = opencv_core.ROTATE_180;
                        case 270 -> ROTA = opencv_core.ROTATE_90_COUNTERCLOCKWISE;
                    }

                    if (settingsRotation != 0) {
                        opencv_core.rotate(processedFrame, processedFrame, ROTA);
                    }

                    // TODO: This is temporary way to show the session time. It should be moved to a more appropriate place.
                    String sessionTime = sessionHandler.getSessionDuration();
                    putText(
                            processedFrame,
                            sessionTime,
                            new Point(processedFrame.cols()-100, 30),
                            FONT_HERSHEY_COMPLEX,
                            1.0,
                            redColor,
                            1,
                            LINE_8,
                            false
                    );

                    processedFrameQueue.offer(processedFrame);

                    if(sessionHandler.isSessionActive() && settings.isSaveVideo()) {
                        Mat recordingFrame = processedFrame.clone();
                        if(!recordingFrameQueue.offer(recordingFrame, 50, TimeUnit.MILLISECONDS)){
                            AIMsLogger.fatal("Failed to add frame to recording queue");
                            recordingFrame.release();
                        }
                    }

                } catch (InterruptedException e) {
                    AIMsLogger.fatal("Frame processing thread interrupted: " + e.getMessage());
                }
            }
        });
    }

    private void startDisplayThread(){
        displayExecutor.submit(() -> {
            while(isRunning && !Thread.currentThread().isInterrupted()){
                try {
                    Mat displayFrame = processedFrameQueue.poll(50, TimeUnit.MILLISECONDS);
                    if(displayFrame == null)
                        continue;

                    BufferedImage biFrame = cvt2bi(displayFrame);
                    SwingUtilities.invokeLater(() -> {
                        cameraFeed.setIcon(new ImageIcon(biFrame));
                        biFrame.flush();
                    });

                    displayFrame.release();
                } catch (Exception e) {
                    AIMsLogger.error("Error displaying frame: " + e.getMessage());
                }
            }
        });
    }

    private void startRecordingThread(){
        recordingExecutor.submit(() -> {
            while (isRunning || !Thread.currentThread().isInterrupted()) {
                try {
                    Mat recordingFrame = recordingFrameQueue.poll(50, TimeUnit.MILLISECONDS);
                    if (recordingFrame == null)
                        continue;

                    if (sessionHandler.isSessionActive() && settings.isSaveVideo()) {
                        FileSession fileSession = sessionHandler.getFileSession();

                        VideoWriter videoWriter = fileSession.getVideoWriter();
                        if(videoWriter == null || !videoWriter.isOpened()){
                            fileSession.initVideoWriter(recordingFrame);
                        }

                        try{
                            fileSession.writeVideoFrame(recordingFrame);
                        }catch (Exception e) {
                            AIMsLogger.error("Error writing video frame: " + e.getMessage());
                        } finally {
                            recordingFrame.release();
                        }
                    }
                }catch (Exception e) {
                    AIMsLogger.fatal("Error in recording thread: " + e.getMessage());
                }
            }
        });
    }

    public void shutdown(){
        isRunning = false;
        timer.cancel();

        clearFrameQueues();

        try{
            inferenceExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
            displayExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
            recordingExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
        }catch (InterruptedException e) {
           AIMsLogger.error("Error shutting down frame handler: " + e.getMessage());
        }

        if(!inferenceExecutor.isTerminated()) inferenceExecutor.shutdownNow();
        if(!displayExecutor.isTerminated()) displayExecutor.shutdownNow();
        if(!recordingExecutor.isTerminated()) recordingExecutor.shutdownNow();
    }

    private void clearFrameQueues() {
        Mat frame;
        while ((frame = rawFrameQueue.poll()) != null) frame.release();
        while ((frame = processedFrameQueue.poll()) != null) frame.release();
        while ((frame = recordingFrameQueue.poll()) != null) frame.release();
    }
}
