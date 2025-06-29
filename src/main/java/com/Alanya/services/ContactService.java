// Fichier : ClientserverAlanya/src/com/Alanya/services/ContactService.java
package com.Alanya.services;

import com.Alanya.Client;
import com.Alanya.Mainfirstclientcontroller;
import com.Alanya.Mainfirstclientcontroller.ClientDisplayWrapper;
import com.Alanya.DAO.UserContactDAO;
import com.Alanya.DAO.UserDAO;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import java.sql.SQLException;
import java.util.Optional;

public class ContactService {

    private final UserContactDAO userContactDAO;
    private final UserDAO userDAO;
    private Mainfirstclientcontroller mainController;

    public ContactService(UserContactDAO userContactDAO, UserDAO userDAO) {
        this.userContactDAO = userContactDAO;
        this.userDAO = userDAO;
    }

    public void setMainController(Mainfirstclientcontroller mainController) {
        this.mainController = mainController;
    }
    
    // ... gardez les méthodes setCustomContactName, getDisplayNameForContact, deleteContact, editContactNameDialog ...
    public boolean setCustomContactName(int ownerUserId, int contactClientId, String customName) {
        try {
            String nameToStore = (customName != null && customName.trim().isEmpty()) ? null : customName;
            userContactDAO.addOrUpdateContactRelation(ownerUserId, contactClientId, nameToStore);
            if (mainController != null) {
                Platform.runLater(() -> mainController.loadMyPersonalContacts());
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public String getDisplayNameForContact(int ownerUserId, Client contactClient) {
        if (contactClient == null) return "";
        try {
            String customName = userContactDAO.getCustomName(ownerUserId, contactClient.getId());
            return (customName != null && !customName.isEmpty()) ? customName : contactClient.getNomUtilisateur();
        } catch (SQLException e) {
            e.printStackTrace();
            return contactClient.getNomUtilisateur();
        }
    }
    public void deleteContact(int ownerUserId, ClientDisplayWrapper contactToDeleteWrapper) {
        if (contactToDeleteWrapper == null || mainController == null) return;
        Client clientToDelete = contactToDeleteWrapper.getClient();
        String displayName = contactToDeleteWrapper.getDisplayName();

        Platform.runLater(() -> {
            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + displayName + " ?", ButtonType.YES, ButtonType.NO);
            confirmationDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        userContactDAO.deleteContactRelation(ownerUserId, clientToDelete.getId());
                        mainController.loadMyPersonalContacts();
                        if (mainController.contactListView.getSelectionModel().getSelectedItem() == contactToDeleteWrapper) {
                            mainController.handleBack(null);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }
    public void editContactNameDialog(int ownerUserId, ClientDisplayWrapper contactWrapper) {
        if (contactWrapper == null || mainController == null) return;
        Client clientToEdit = contactWrapper.getClient();
        String currentDisplayName = contactWrapper.getDisplayName();
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog(currentDisplayName);
            dialog.setTitle("Modifier le nom");
            dialog.setHeaderText("Entrez le nouveau nom pour : " + clientToEdit.getNomUtilisateur());
            dialog.showAndWait().ifPresent(newName -> {
                setCustomContactName(ownerUserId, clientToEdit.getId(), newName.trim());
            });
        });
    }

    /**
     * CORRECTION : Logique revue pour ne pas perturber l'UI.
     * Ajoute un contact à la liste observable sans tout recharger.
     */
    public Client ensureContactExistsAndDisplay(int receiverUserId, String senderUsername) {
        if (mainController == null || senderUsername == null || senderUsername.isEmpty()) {
            return null;
        }

        try {
            Client sender = userDAO.findUserByUsername(senderUsername);

            if (sender == null || sender.getId() == receiverUserId) {
                return sender; // L'expéditeur n'existe pas ou c'est l'utilisateur lui-même
            }

            boolean alreadyExists = userContactDAO.checkIfContactRelationExists(receiverUserId, sender.getId());

            if (alreadyExists) {
                return sender; // Déjà un contact, rien à faire
            }

            // Le contact n'existe pas, on l'ajoute en BDD
            String initialCustomName = sender.getNomUtilisateur();
            userContactDAO.addOrUpdateContactRelation(receiverUserId, sender.getId(), initialCustomName);
            System.out.println("ContactService: Création auto du contact '" + sender.getNomUtilisateur() + "' pour l'utilisateur ID " + receiverUserId);

            // CORRECTION : Au lieu de recharger toute la liste, on l'ajoute directement au modèle de l'UI
            final Client finalSender = sender;
            Platform.runLater(() -> {
                // Créer le wrapper pour l'affichage
                Mainfirstclientcontroller.ClientDisplayWrapper newContactWrapper = 
                    new Mainfirstclientcontroller.ClientDisplayWrapper(finalSender, initialCustomName);
                
                // Ajouter à la liste observable sans clear()
                mainController.myPersonalContactsList.add(newContactWrapper);
                
                mainController.showAlert(AlertType.INFORMATION, "Nouveau Contact Ajouté",
                    "L'utilisateur " + finalSender.getNomUtilisateur() + " vous a envoyé un message et a été ajouté à vos contacts.");
            });

            return sender;

        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() -> mainController.showAlert(AlertType.ERROR, "Erreur Interne", "Erreur lors de la création du contact : " + e.getMessage()));
            return null;
        }
    }
}