package com.Alanya;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class CameraCaptureController implements Initializable {

    @FXML private ImageView cameraFrameView;
    @FXML private Button captureButton;
    @FXML private Button cancelButton;

    private Mainfirstclientcontroller mainController;
    private VideoCapture videoCapture;
    private volatile boolean isStreaming = false;
    private Mat currentFrameMat;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Utiliser le chargeur de bytedeco, qui est beaucoup plus fiable.
        	Loader.load(opencv_java.class);
            
            videoCapture = new VideoCapture(0); // Ouvre la caméra par défaut

            if (!videoCapture.isOpened()) {
                showAlertInController(Alert.AlertType.ERROR, "Erreur Caméra", "Impossible d'accéder à la caméra.");
                Platform.runLater(this::closeWindow);
                return;
            }
            startStreaming();
        } catch (Exception e) {
            e.printStackTrace();
            showAlertInController(Alert.AlertType.ERROR, "Erreur OpenCV", "Impossible de charger la bibliothèque native OpenCV.");
            Platform.runLater(this::closeWindow);
        }
    }
    

    public void setMainController(Mainfirstclientcontroller controller) {
        this.mainController = controller;
    }

    private void startStreaming() {
        isStreaming = true;
        new Thread(() -> {
            Mat frame = new Mat();
            try {
                while (isStreaming && videoCapture.isOpened()) {
                    if (videoCapture.read(frame)) {
                        if (!frame.empty()) {
                            if (currentFrameMat != null) currentFrameMat.release();
                            currentFrameMat = frame.clone();
                            Image fxImage = convertMatToFxImage(frame);
                            Platform.runLater(() -> cameraFrameView.setImage(fxImage));
                        }
                    }
                    Thread.sleep(33);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("Erreur dans le thread de streaming caméra: " + e.getMessage());
            } finally {
                if (frame != null) frame.release();
                System.out.println("Thread de streaming caméra arrêté.");
            }
        }, "CameraStreamThread").start();
    }

    @FXML
    void handleCaptureImage(ActionEvent event) {
        isStreaming = false;
        if (currentFrameMat != null && !currentFrameMat.empty()) {
            try {
                MatOfByte matOfByte = new MatOfByte();
                Imgcodecs.imencode(".png", currentFrameMat, matOfByte);
                byte[] byteArray = matOfByte.toArray();
                matOfByte.release();
                String savePath = mainController.attachmentService.getCurrentDefaultDownloadPath();
                mainController.ensureDirectoryExists(savePath);
                File imageFile = File.createTempFile("capture_", ".png", new File(savePath));
                Files.write(imageFile.toPath(), byteArray);
                if (mainController != null) {
                    mainController.processCapturedImage(imageFile);
                }
                closeWindow();
            } catch (IOException e) {
                e.printStackTrace();
                showAlertInController(Alert.AlertType.ERROR, "Erreur Capture", "Impossible de sauvegarder l'image capturée.");
            } finally {
                 if(currentFrameMat != null) currentFrameMat.release();
            }
        } else {
            showAlertInController(Alert.AlertType.WARNING, "Capture Échouée", "Aucune image à capturer.");
        }
    }

    @FXML
    void handleCancelCapture(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        isStreaming = false;
        if (videoCapture != null && videoCapture.isOpened()) {
            videoCapture.release();
            videoCapture = null;
        }
        if (currentFrameMat != null) {
            currentFrameMat.release();
            currentFrameMat = null;
        }
        Platform.runLater(() -> {
            if (cameraFrameView != null && cameraFrameView.getScene() != null) {
                Stage stage = (Stage) cameraFrameView.getScene().getWindow();
                if (stage != null) {
                    stage.close();
                }
            }
        });
    }

    private Image convertMatToFxImage(Mat mat) {
        if (mat == null || mat.empty()) return null;
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);
        Image image = new Image(new ByteArrayInputStream(buffer.toArray()));
        buffer.release();
        return image;
    }
    
    private void showAlertInController(Alert.AlertType type, String title, String content) {
        if (mainController != null) {
            mainController.showAlert(type, title, content);
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(type);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(content);
                alert.showAndWait();
            });
        }
    }
}
