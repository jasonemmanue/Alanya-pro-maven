package com.Alanya;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.Alanya.DAO.UserDAO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

public class ProfilePictureSetupController {

    @FXML private ImageView previewImageView;
    private int userId;
    private File selectedImageFile;
    private UserDAO userDAO = new UserDAO();

    public void initData(int userId) {
        this.userId = userId;
    }

    @FXML
    void handleChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            selectedImageFile = file;
            previewImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    void handleTakePicture(ActionEvent event) {
        // Ici, vous appelleriez votre interface de capture caméra.
        // Pour simplifier, nous mettons une alerte, mais vous pouvez intégrer la logique de CameraCaptureController.
        showAlert(Alert.AlertType.INFORMATION, "Non implémenté", "La capture directe sera intégrée ici.");
    }

    @FXML
    void handleFinish(ActionEvent event) {
        if (selectedImageFile != null) {
            try {
                byte[] pictureData = Files.readAllBytes(selectedImageFile.toPath());
                userDAO.updateUserProfilePicture(userId, pictureData);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Photo de profil enregistrée.");
            } catch (IOException | SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer la photo de profil.");
            }
        }
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) previewImageView.getScene().getWindow();
        stage.close();
    }

    private Stage getStage() {
        return (Stage) previewImageView.getScene().getWindow();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}