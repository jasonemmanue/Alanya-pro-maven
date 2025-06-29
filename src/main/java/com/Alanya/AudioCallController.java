package com.Alanya;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AudioCallController {

    @FXML
    private Label contactNameLabelAudio;

    @FXML
    private Button hangUpButtonAudio;
    
    // Nouvelle variable FXML pour le minuteur
    @FXML
    private Label callTimerLabel;

    private Mainfirstclientcontroller mainController;
    private String callId;
    private Stage stage;

    public void initializeCall(Mainfirstclientcontroller mainController, String contactName, String callId, Stage stage) {
        this.mainController = mainController;
        this.callId = callId;
        this.stage = stage;
        if (contactNameLabelAudio != null) {
            contactNameLabelAudio.setText(contactName);
        }
    }

    @FXML
    void handleHangUpAudio(ActionEvent event) {
        System.out.println("Tentative de raccrocher l'appel audio ID: " + callId);
        if (mainController != null) {
            mainController.endCallLogic(true); // true pour notifier le pair/serveur
        }
        // La fenêtre sera fermée par la logique de endCallLogic
    }

    /**
     * Nouvelle méthode pour permettre au contrôleur principal d'accéder au Label du minuteur.
     * @return Le Label qui affiche la durée de l'appel.
     */
    public Label getTimerLabel() {
        return this.callTimerLabel;
    }

    // Méthode appelée par le Mainfirstclientcontroller pour fermer la fenêtre
    public void closeWindow() {
        if (stage != null) {
            Platform.runLater(() -> stage.close());
        }
    }
}