package com.Alanya; 

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class VideoCallController {
	
	@FXML private StackPane videoPane;
	
	@FXML private Label callTimerLabel;

    @FXML
    private ImageView remoteVideoView;

    @FXML
    private ImageView localVideoView;

    @FXML
    private ToggleButton muteButtonVideo;

    @FXML
    private Button hangUpButtonVideo;

    private Mainfirstclientcontroller mainController;
    private String callId;
    private Stage stage;
    private boolean isMuted = false;

    public void initializeCall(Mainfirstclientcontroller mainController, String contactName, String callId, Stage stage) {
        this.mainController = mainController;
        this.callId = callId;
        this.stage = stage;
        
        // Logique de redimensionnement et positionnement de la vidéo locale
        if (remoteVideoView != null && localVideoView != null && videoPane != null) {
            // Lier la taille de la vue locale à 1/5ème de la taille de la vue parente (le StackPane)
            // C'est plus robuste que de dépendre de la vue distante qui peut changer de taille.
            localVideoView.fitWidthProperty().bind(videoPane.widthProperty().divide(5));
            localVideoView.fitHeightProperty().bind(videoPane.heightProperty().divide(5));

            // S'assurer que la vue locale est dessinée par-dessus la vue distante
            localVideoView.toFront();
            if(callTimerLabel != null) {
                callTimerLabel.toFront();
            }


            // Positionner la vue locale en bas à droite du StackPane
            StackPane.setAlignment(localVideoView, Pos.BOTTOM_RIGHT);
            
            // Ajouter une marge pour un plus bel effet visuel
            StackPane.setMargin(localVideoView, new javafx.geometry.Insets(15));
        }
    }

    @FXML
    void handleHangUpVideo(ActionEvent event) {
        System.out.println("VCC: Clic sur Raccrocher pour l'appel ID: " + callId);
        if (mainController != null) {
            mainController.endCallLogic(true); // true pour notifier le pair
        }
        // La fenêtre sera fermée par la méthode closeCallUIAndStreams() appelée dans endCallLogic()
    }
    @FXML
    void handleMuteToggle(ActionEvent event) {
        isMuted = muteButtonVideo.isSelected();
        if (mainController != null) {
            mainController.toggleMute(isMuted);
        }
    }
    
    public Label getTimerLabel() {
        return this.callTimerLabel;
    }

    public void setLocalVideoFrame(Image image) {
        if (localVideoView != null) {
            Platform.runLater(() -> localVideoView.setImage(image));
        }
    }

    public void setRemoteVideoFrame(Image image) {
        if (remoteVideoView != null) {
            Platform.runLater(() -> remoteVideoView.setImage(image));
        }
    }

    // Méthode appelée par le Mainfirstclientcontroller pour fermer la fenêtre
    public void closeWindow() {
        if (stage != null) {
            Platform.runLater(() -> stage.close());
        }
    }
    
 // In VideoCallController.java

    public ImageView getLocalImageView() {
        return localVideoView;
    }

    public ImageView getRemoteImageView() {  
        return remoteVideoView;
    }

    // Méthode pour mettre à jour l'état du bouton mute si l'état change ailleurs
    public void updateMuteButtonState(boolean muted) {
        this.isMuted = muted;
        if (muteButtonVideo != null) {
            Platform.runLater(() -> {
                muteButtonVideo.setSelected(muted);
                muteButtonVideo.setText(muted ? "Réactiver Son" : "Couper Son");
            });
        }
    }
}