package com.Alanya;

import com.Alanya.DAO.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

public class ProfilePictureEditorController {

    @FXML private Circle profileImageView;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;
    @FXML private Button changeFileButton;
    @FXML private Button takePictureButton;

    private Mainfirstclientcontroller mainController;
    private Client currentUser;
    private File selectedImageFile;
    private UserDAO userDAO = new UserDAO();
    private byte[] newImageBytes = null;

    public void initData(Mainfirstclientcontroller mainController, Client currentUser) {
        this.mainController = mainController;
        this.currentUser = currentUser;

        byte[] currentPicture = currentUser.getProfilePicture();
        Image imageToShow;
        if (currentPicture != null && currentPicture.length > 0) {
            imageToShow = new Image(new ByteArrayInputStream(currentPicture));
        } else {
            try {
                imageToShow = new Image(getClass().getResourceAsStream("/com/Alanya/compte-utilisateur.png"));
            } catch (Exception e) {
                imageToShow = null;
                profileImageView.setFill(Color.LIGHTGRAY);
            }
        }
        
        if (imageToShow != null) {
            profileImageView.setFill(new ImagePattern(cropToSquare(imageToShow)));
        }
    }

    @FXML
    void handleChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            updatePreview(file);
        }
    }

    @FXML
    void handleTakePicture(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/Alanya/CameraCaptureView.fxml"));
            Parent root = loader.load();
            CameraCaptureController captureController = loader.getController();
            captureController.setMainController(mainController);

            Stage captureStage = new Stage();
            captureStage.setTitle("Prendre une Photo");
            captureStage.setScene(new Scene(root));
            captureStage.initModality(Modality.APPLICATION_MODAL);
            captureStage.initOwner(getStage());
            captureStage.showAndWait();

            File capturedFile = mainController.attachmentService.getSelectedFile();
            if (capturedFile != null) {
                updatePreview(capturedFile);
                mainController.attachmentService.cancelFileSelection();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'interface de capture photo.");
        }
    }

    private void updatePreview(File file) {
        selectedImageFile = file;
        try {
            newImageBytes = Files.readAllBytes(file.toPath());
            Image image = new Image(new ByteArrayInputStream(newImageBytes));
            // Appliquer le recadrage avant l'aperçu
            Image croppedImage = cropToSquare(image);
            profileImageView.setFill(new ImagePattern(croppedImage));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de Fichier", "Impossible de lire le fichier image sélectionné.");
            newImageBytes = null;
        }
    }

    /**
     * **NOUVELLE MÉTHODE**
     * Recadre une image en un carré centré.
     * @param image L'image originale.
     * @return Une nouvelle image carrée.
     */
    private Image cropToSquare(Image image) {
        double width = image.getWidth();
        double height = image.getHeight();
        double size = Math.min(width, height); // La taille du carré est le plus petit côté

        // Calcule le point de départ (x, y) pour le recadrage afin de centrer le carré
        double xOffset = (width - size) / 2;
        double yOffset = (height - size) / 2;

        ImageView imageView = new ImageView(image);
        Rectangle2D viewport = new Rectangle2D(xOffset, yOffset, size, size);
        imageView.setViewport(viewport);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        
        WritableImage croppedImage = new WritableImage((int) size, (int) size);
        imageView.snapshot(params, croppedImage);
        
        return croppedImage;
    }

    @FXML
    void handleConfirm(ActionEvent event) {
        if (newImageBytes == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune modification", "Veuillez choisir une nouvelle image avant de confirmer.");
            return;
        }

        try {
            userDAO.updateUserProfilePicture(currentUser.getId(), newImageBytes);
            currentUser.setProfilePicture(newImageBytes);

            // Créer une image à partir des bytes originaux pour la passer au contrôleur principal
            Image updatedImage = new Image(new ByteArrayInputStream(newImageBytes));
            // Le contrôleur principal se chargera de la recadrer à nouveau pour l'affichage
            mainController.updateProfilePictureUI(updatedImage);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre photo de profil a été mise à jour.");
            closeWindow();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", "Une erreur est survenue lors de la sauvegarde.");
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private Stage getStage() {
        return (Stage) profileImageView.getScene().getWindow();
    }

    private void closeWindow() {
        getStage().close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}