package com.Alanya;

import com.Alanya.Mainfirstclientcontroller;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ThemeSelectionController {

    @FXML private VBox dialogRoot;
    private Mainfirstclientcontroller mainController;

    public void setMainController(Mainfirstclientcontroller controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleLightMode() {
        System.out.println("Bouton 'Thème Jour' cliqué !"); // Log de débogage
        if (mainController != null) {
            mainController.applyTheme("light", null);
        }
        closeDialog();
    }

    @FXML
    private void handleDarkMode() {
        System.out.println("Bouton 'Thème Sombre' cliqué !"); // Log de débogage
        if (mainController != null) {
            mainController.applyTheme("dark", null);
        }
        closeDialog();
    }

    @FXML
    private void handleCustomImage() {
        System.out.println("Bouton 'Image Personnalisée' cliqué !"); // Log de débogage
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image d'arrière-plan");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(dialogRoot.getScene().getWindow());

        if (selectedFile != null) {
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                if (mainController != null) {
                    mainController.applyTheme("custom_image", fileContent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) dialogRoot.getScene().getWindow();
        stage.close();
    }
}