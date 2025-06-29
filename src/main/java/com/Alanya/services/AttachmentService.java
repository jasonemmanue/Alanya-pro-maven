// Fichier : ClientserverAlanya/src/com/Alanya/services/AttachmentService.java
package com.Alanya.services;

import com.Alanya.App;
import com.Alanya.Mainfirstclientcontroller;
import com.Alanya.Message;
import com.Alanya.model.AttachmentInfo;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.function.BiConsumer;

public class AttachmentService {

    private File selectedFile;
    private AttachmentInfo currentAttachmentInfo;
    private Mainfirstclientcontroller mainController;

    public static final String DEFAULT_SAVE_DIRECTORY_NAME = "AlanyaChatFiles";
    public static final String DEFAULT_SAVE_DIRECTORY = System.getProperty("user.home") + File.separator + DEFAULT_SAVE_DIRECTORY_NAME;
    private static final String PREF_KEY_DEFAULT_DOWNLOAD_PATH = "defaultDownloadPathAlanya";
    private static final String PREF_NODE_DOWNLOADED_FILES = "downloadedFilePaths";
    private String currentDefaultDownloadPath;
    private Preferences prefsRoot;

    public AttachmentService() {
        this.prefsRoot = Preferences.userNodeForPackage(AttachmentService.class);
        this.currentDefaultDownloadPath = prefsRoot.get(PREF_KEY_DEFAULT_DOWNLOAD_PATH, DEFAULT_SAVE_DIRECTORY);
        ensureDirectoryExists(this.currentDefaultDownloadPath);
    }
    
    public void ensureDirectoryExists(String pathStr) {
        Path path = Paths.get(pathStr);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("Répertoire créé : " + pathStr);
            } catch (IOException e) {
                System.err.println("Impossible de créer le répertoire : " + pathStr + " - " + e.getMessage());
                // Fallback to default directory if custom one fails
                this.currentDefaultDownloadPath = DEFAULT_SAVE_DIRECTORY;
                prefsRoot.put(PREF_KEY_DEFAULT_DOWNLOAD_PATH, this.currentDefaultDownloadPath);
                ensureDirectoryExists(this.currentDefaultDownloadPath);
            }
        }
    }

    private Preferences getFileDownloadPrefs() {
        return this.prefsRoot.node(PREF_NODE_DOWNLOADED_FILES);
    }
    
    // CORRECTION : AJOUTÉ - Méthode pour sauvegarder le chemin d'un fichier téléchargé
    public void saveDownloadedFilePathPref(long messageId, String originalFileName, String receiverLocalPath) {
        if (receiverLocalPath == null || receiverLocalPath.isEmpty() || originalFileName == null || originalFileName.isEmpty() || messageId <= 0) {
            return;
        }
        Preferences prefs = getFileDownloadPrefs();
        String prefKey = messageId + "!" + originalFileName;
        prefs.put(prefKey, receiverLocalPath);
        try {
            prefs.flush(); // Sauvegarde immédiate
        } catch (BackingStoreException e) {
            System.err.println("Erreur lors de la sauvegarde de la préférence du chemin pour " + prefKey + ": " + e.getMessage());
        }
    }

    // CORRECTION : AJOUTÉ - Méthode pour récupérer un chemin sauvegardé
    public String getDownloadedFilePathPref(long messageId, String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty() || messageId <= 0) return null;
        Preferences prefs = getFileDownloadPrefs();
        String prefKey = messageId + "!" + originalFileName;
        return prefs.get(prefKey, null);
    }

    // CORRECTION : AJOUTÉ - Méthode pour supprimer une préférence si le fichier est invalide
    public void removeDownloadedFilePathPref(long messageId, String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty() || messageId <= 0) return;
        Preferences prefs = getFileDownloadPrefs();
        String prefKey = messageId + "!" + originalFileName;
        prefs.remove(prefKey);
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            System.err.println("Erreur lors de la suppression de la préférence du chemin pour " + prefKey + ": " + e.getMessage());
        }
    }

    public void setMainController(Mainfirstclientcontroller mainController) {
        this.mainController = mainController;
    }

    public AttachmentInfo getCurrentAttachmentInfo() { return currentAttachmentInfo; }
    public File getSelectedFile() { return selectedFile; }
    public String getCurrentDefaultDownloadPath() { return currentDefaultDownloadPath; }

    public void selectFile(Stage ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier à envoyer");
        this.selectedFile = fileChooser.showOpenDialog(ownerStage);

        if (this.selectedFile != null) {
            try {
                this.currentAttachmentInfo = new AttachmentInfo(
                    selectedFile.getName(),
                    Files.probeContentType(selectedFile.toPath()),
                    selectedFile.length(),
                    selectedFile.getAbsolutePath()
                );
                if (mainController != null) {
                    mainController.displayAttachmentPreview(this.currentAttachmentInfo);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (mainController != null) mainController.showAlert(Alert.AlertType.ERROR, "Erreur Fichier", "Impossible de lire les informations du fichier.");
                clearSelection();
            }
        } else {
            clearSelection();
        }
    }

    private void clearSelection() {
        this.selectedFile = null;
        this.currentAttachmentInfo = null;
        if (mainController != null) {
            mainController.displayAttachmentPreview(null);
        }
    }

    public void cancelFileSelection() {
        clearSelection();
    }

    public Message prepareMessageWithAttachment(String senderUsername, String receiverUsername, String textContent) {
        Message message = new Message(senderUsername, receiverUsername, textContent);
        if (currentAttachmentInfo != null && selectedFile != null && selectedFile.exists()) {
            currentAttachmentInfo.setLocalPath(selectedFile.getAbsolutePath());
            message.setAttachmentInfo(currentAttachmentInfo);
        }
        return message;
    }

    // CORRECTION : Logique de startFileDownload entièrement revue pour plus de robustesse
    public void startFileDownload(Message messageContainingAttachment, Stage ownerStage, Runnable onDownloadSuccessUIUpdate, Runnable onDownloadFailureUIUpdate) {
        if (messageContainingAttachment == null || messageContainingAttachment.getAttachmentInfo() == null || mainController == null) {
            if (onDownloadFailureUIUpdate != null) Platform.runLater(onDownloadFailureUIUpdate);
            return;
        }
        AttachmentInfo attachmentToDownload = messageContainingAttachment.getAttachmentInfo();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choisir où sauvegarder : " + attachmentToDownload.getFileName());
        
        File initialDirectory = new File(currentDefaultDownloadPath);
        if (!initialDirectory.exists() || !initialDirectory.isDirectory()) {
            initialDirectory = new File(DEFAULT_SAVE_DIRECTORY);
            ensureDirectoryExists(DEFAULT_SAVE_DIRECTORY);
        }
        directoryChooser.setInitialDirectory(initialDirectory);

        File chosenDirectory = directoryChooser.showDialog(ownerStage);

        if (chosenDirectory != null) {
            String chosenSavePath = new File(chosenDirectory, attachmentToDownload.getFileName()).getAbsolutePath();

            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Répertoire par Défaut");
            confirmDialog.setHeaderText("Confirmer le téléchargement");
            
            DialogPane dialogPane = confirmDialog.getDialogPane();
            VBox contentVBox = new VBox(10);
            Label saveLocationLabel = new Label("Sauvegarder dans : " + chosenSavePath);
            CheckBox setDefaultCheckBox = new CheckBox("Toujours utiliser ce répertoire par défaut.");
            contentVBox.getChildren().addAll(saveLocationLabel, setDefaultCheckBox);
            dialogPane.setContent(contentVBox);
            
            Optional<ButtonType> result = confirmDialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (setDefaultCheckBox.isSelected()) {
                    this.currentDefaultDownloadPath = chosenDirectory.getAbsolutePath();
                    prefsRoot.put(PREF_KEY_DEFAULT_DOWNLOAD_PATH, this.currentDefaultDownloadPath);
                    try {
                        prefsRoot.flush(); // Forcer la sauvegarde immédiate
                    } catch (BackingStoreException e) {
                       System.err.println("Erreur flush preferences: " + e.getMessage());
                    }
                }
            
                mainController.initiateP2PFileDownloadRequest(
                    messageContainingAttachment.getSender(),
                    messageContainingAttachment.getDatabaseId(),
                    attachmentToDownload, 
                    chosenSavePath, 
                    (success, downloadedFile) -> {
                        if (success && downloadedFile != null) {
                            if (onDownloadSuccessUIUpdate != null) Platform.runLater(onDownloadSuccessUIUpdate);
                        } else {
                            if (onDownloadFailureUIUpdate != null) Platform.runLater(onDownloadFailureUIUpdate);
                        }
                    }
                );
            } else {
                 if (onDownloadFailureUIUpdate != null) Platform.runLater(onDownloadFailureUIUpdate);
            }
        } else {
             if (onDownloadFailureUIUpdate != null) Platform.runLater(onDownloadFailureUIUpdate);
        }
    }

    public void openAttachment(String localPath) {
        if (localPath == null || localPath.isEmpty()) {
            if (mainController != null) mainController.showAlert(Alert.AlertType.WARNING, "Ouverture Fichier", "Chemin de fichier invalide.");
            return;
        }
        File fileToOpen = new File(localPath);
        if (!fileToOpen.exists()) {
            if (mainController != null) mainController.showAlert(Alert.AlertType.WARNING, "Fichier Introuvable", "Le fichier n'existe plus à l'emplacement : " + localPath);
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(fileToOpen);
            } else {
                HostServices hostServices = getHostServices();
                if (hostServices != null) {
                    hostServices.showDocument(fileToOpen.toURI().toString());
                } else {
                     if (mainController != null) mainController.showAlert(Alert.AlertType.ERROR, "Erreur Ouverture", "Impossible d'ouvrir le fichier automatiquement.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (mainController != null) mainController.showAlert(Alert.AlertType.ERROR, "Erreur Ouverture", "Impossible d'ouvrir le fichier : " + e.getMessage());
        }
    }

    private HostServices getHostServices() {
        if (mainController != null && mainController.getHostServicesFromApplication() != null) {
            return mainController.getHostServicesFromApplication();
        }
        if (App.getInstance() != null) {
            return App.getInstance().getHostServices();
        }
        return null;
    }
    
    public void setSelectedFileAndInfo(File file, AttachmentInfo attachmentInfo) {
        this.selectedFile = file;
        this.currentAttachmentInfo = attachmentInfo;
        if (mainController != null) {
            mainController.updateSendButtonStateBasedOnAttachment();
        }
    }
}