package com.Alanya;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

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
    private Mat currentFrameMat; // Pour stocker la frame au moment de la capture

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
            videoCapture = new VideoCapture(0); // Ouvre la caméra par défaut

            if (!videoCapture.isOpened()) {
                System.err.println("CameraCaptureController: Impossible d'ouvrir la caméra.");
                if (mainController != null) { // Peut être null si appelé avant setMainController
                     mainController.showAlert(Alert.AlertType.ERROR, "Erreur Caméra", "Impossible d'accéder à la caméra.");
                }
                Platform.runLater(this::closeWindow); // Fermer si la caméra ne s'ouvre pas
                return;
            }
            startStreaming();
        } catch (UnsatisfiedLinkError e) {
            System.err.println("CameraCaptureController: Bibliothèque OpenCV non trouvée. " + e.getMessage());
            if (mainController != null) {
                mainController.showAlert(Alert.AlertType.ERROR, "Erreur OpenCV", "Bibliothèque OpenCV manquante.");
            }
            Platform.runLater(this::closeWindow);
        } catch (Exception e) {
            System.err.println("CameraCaptureController: Erreur initialisation caméra: " + e.getMessage());
            e.printStackTrace();
            if (mainController != null) {
                mainController.showAlert(Alert.AlertType.ERROR, "Erreur Caméra", "Erreur d'initialisation: " + e.getMessage());
            }
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
                            // Stocker la frame actuelle pour la capture
                            if (currentFrameMat != null) currentFrameMat.release(); // Libérer l'ancienne
                            currentFrameMat = frame.clone(); // Cloner pour la capture

                            Image fxImage = convertMatToFxImage(frame); // Utiliser la méthode renommée
                            Platform.runLater(() -> cameraFrameView.setImage(fxImage));
                        }
                    }
                    Thread.sleep(33); // Environ 30 FPS
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("Erreur dans le thread de streaming caméra: " + e.getMessage());
            } finally {
                if (frame != null) frame.release();
                // Ne pas relâcher videoCapture ici pour permettre de redémarrer
                System.out.println("Thread de streaming caméra arrêté.");
            }
        }, "CameraStreamThread").start();
    }

    @FXML
    void handleCaptureImage(ActionEvent event) {
        isStreaming = false; // Arrêter le thread de streaming

        if (currentFrameMat != null && !currentFrameMat.empty()) {
            try {
                MatOfByte matOfByte = new MatOfByte();
                Imgcodecs.imencode(".png", currentFrameMat, matOfByte);
                byte[] byteArray = matOfByte.toArray();
                matOfByte.release();

                // Utiliser le chemin de téléchargement par défaut de AttachmentService
                String savePath = mainController.attachmentService.getCurrentDefaultDownloadPath();
                mainController.ensureDirectoryExists(savePath); // S'assurer que le dossier existe

                File imageFile = File.createTempFile("capture_", ".png", new File(savePath));
                Files.write(imageFile.toPath(), byteArray);

                if (mainController != null) {
                    mainController.processCapturedImage(imageFile);
                }
                closeWindow();

            } catch (IOException e) {
                e.printStackTrace();
                if (mainController != null) {
                    mainController.showAlert(Alert.AlertType.ERROR, "Erreur Capture", "Impossible de sauvegarder l'image capturée.");
                }
            } finally {
                 if(currentFrameMat != null) currentFrameMat.release();
            }
        } else {
            if (mainController != null) {
                mainController.showAlert(Alert.AlertType.WARNING, "Capture Échouée", "Aucune image à capturer.");
            }
        }
    }

    @FXML
    void handleCancelCapture(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        isStreaming = false; // Arrête le thread
        if (videoCapture != null && videoCapture.isOpened()) {
            videoCapture.release(); // Libérer la caméra
            videoCapture = null;
        }
        if (currentFrameMat != null) {
            currentFrameMat.release();
            currentFrameMat = null;
        }
        Platform.runLater(() -> {
            Stage stage = (Stage) cameraFrameView.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        });
    }

    private Image convertMatToFxImage(Mat mat) { // Assure-toi que c'est le bon nom
        if (mat == null || mat.empty()) return null;
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);
        Image image = new Image(new ByteArrayInputStream(buffer.toArray()));
        buffer.release();
        return image;
    }
}