package com.Alanya;
import javafx.animation.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {
    private static final String WELCOME_TEXT = "Welcome";
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // CHARGEMENT DE LA POLICE PACIFICO
        Font pacificoFont = Font.loadFont(
            getClass().getResourceAsStream("/com/Ressources/fonts/Pacifico.ttf"), 
            70  // Taille de la police
        );
        
        Text welcomeText = new Text();
        welcomeText.setFill(Color.web("#2196F3")); // Couleur bleu-gris foncé
        
        // APPLICATION DE LA POLICE PACIFICO
        if (pacificoFont != null) {
            welcomeText.setFont(pacificoFont);
        } else {
            // Police de fallback si Pacifico ne charge pas
            welcomeText.setFont(Font.font("Arial", FontWeight.BOLD, 70));
            System.out.println("Attention: Police Pacifico non trouvée, utilisation d'Arial");
        }
        
        welcomeText.setEffect(new DropShadow(5, Color.web("#222")));  // Ombre douce
        
        StackPane welcomeRoot = new StackPane(welcomeText);
        welcomeRoot.setStyle("-fx-background-color: #cce7ff;");  // fond bleu clair
        Scene welcomeScene = new Scene(welcomeRoot, 800, 400);
        
        primaryStage.setTitle("Welcome");
        primaryStage.setScene(welcomeScene);
        primaryStage.show();
        
        // --- Animation : affichage lettre par lettre ---
        Timeline typingTimeline = new Timeline();
        for (int i = 0; i <= WELCOME_TEXT.length(); i++) {
            final int idx = i;
            KeyFrame kf = new KeyFrame(Duration.millis(250 * i), e -> {
                welcomeText.setText(WELCOME_TEXT.substring(0, idx));
            });
            typingTimeline.getKeyFrames().add(kf);
        }
        
        typingTimeline.setOnFinished(e -> {
            // Effet d'agrandissement + pause
            ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(1), welcomeText);
            scaleUp.setToX(1.8);
            scaleUp.setToY(1.8);
            scaleUp.setAutoReverse(true);
            scaleUp.setCycleCount(2);
            
            PauseTransition pause = new PauseTransition(Duration.seconds(0.3));
            SequentialTransition seq = new SequentialTransition(scaleUp, pause);
            
            seq.setOnFinished(ev -> {
                // Après welcome, on charge la page PrescriptionView.fxml
                showPrescriptionPage(primaryStage);
            });
            seq.play();
        });
        
        typingTimeline.play();
    }
    
    // Méthode pour charger la nouvelle scene
    private void showPrescriptionPage(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/Ressources/Acceuil.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setTitle("Acceuil");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}