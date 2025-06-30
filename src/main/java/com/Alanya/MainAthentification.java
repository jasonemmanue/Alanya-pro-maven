package com.Alanya;

import javafx.animation.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;


public class MainAthentification extends Application {

    private MainAthentificationcontroller controller;
    private static final String WELCOME_TEXT = "Bienvenue Dans Alanya";

    @Override
    public void start(Stage primaryStage) throws Exception {
        // --- Chargement de la police Pacifico ---
        Font pacificoFont = null;
        try {
            pacificoFont = Font.loadFont(getClass().getResourceAsStream("Pacifico.ttf"), 70);
        } catch (Exception e) {
            System.err.println("Erreur de chargement de la police Pacifico.ttf : " + e.getMessage());
            // Police de fallback
            pacificoFont = Font.font("Tahoma", 70);
        }

        // --- Création de l'écran de bienvenue ---
        Text welcomeText = new Text();
        welcomeText.setFill(Color.web("#2196F3")); // Couleur bleue spécifiée
        welcomeText.setFont(pacificoFont); // Police Pacifico
        welcomeText.setEffect(new DropShadow(5, Color.web("#222")));  // Ombre douce

        StackPane welcomeRoot = new StackPane(welcomeText);
        welcomeRoot.setStyle("-fx-background-color:#34495E;");  // Fond bleu clair

        Scene welcomeScene = new Scene(welcomeRoot, 1000, 600);

        primaryStage.setTitle("Bienvenue");
        primaryStage.setScene(welcomeScene);
        
        // Ajout de l'icône à la fenêtre principale
        try {
            Image icon = new Image(getClass().getResourceAsStream("imgA.jpg"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Erreur de chargement de l'icône imgA.jpg : " + e.getMessage());
        }
        
        primaryStage.show();

        // --- Animation : affichage lettre par lettre ---
        Timeline typingTimeline = new Timeline();
        for (int i = 0; i <= WELCOME_TEXT.length(); i++) {
            final int idx = i;
            KeyFrame kf = new KeyFrame(Duration.millis(150 * i), e -> {
                welcomeText.setText(WELCOME_TEXT.substring(0, idx));
            });
            typingTimeline.getKeyFrames().add(kf);
        }

        // --- Déclenchement des animations suivantes après la saisie ---
        typingTimeline.setOnFinished(e -> {
            // Effet d'agrandissement + pause
            ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(1), welcomeText);
            scaleUp.setToX(1.5);
            scaleUp.setToY(1.5);
            scaleUp.setAutoReverse(true);
            scaleUp.setCycleCount(2);

            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));

            // Enchaînement des animations
            SequentialTransition seq = new SequentialTransition(scaleUp, pause);
            seq.setOnFinished(ev -> {
                // Après l'animation, on charge la page d'authentification
                showAuthPage(primaryStage);
            });
            seq.play();
        });

        typingTimeline.play();
    }

    private void showAuthPage(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/Alanya/Interfaceauthentification.fxml"));
            Parent root = loader.load();

            controller = loader.getController();

            stage.setTitle("ALANYA");

            Scene scene = new Scene(root, 1000, 600);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            // Gérer l'erreur de chargement FXML si nécessaire
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}