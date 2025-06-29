package com.Alanya;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
// import java.util.regex.Pattern; // Déplacé vers Client.java

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Addclientcontroller implements Initializable {
    @FXML private TextField usernameField; // Nom personnalisé pour contact, nom d'utilisateur pour inscription
    @FXML private TextField phoneField;    // Numéro de téléphone
    @FXML private TextField emailField;    // Champ email (à ajouter dans le FXML si ce n'est pas déjà fait pour le mode inscription)
    @FXML private TextField passwordField; // Champ mot de passe (à ajouter dans le FXML pour le mode inscription)

    @FXML private Button registerButton;

    @FXML private Label oldNameLabelInfo;
    @FXML private TextField oldNameField;
    @FXML private VBox phoneFieldContainer;
    @FXML private VBox emailFieldContainer;    // Conteneur pour le champ email (à ajouter au FXML)
    @FXML private VBox passwordFieldContainer; // Conteneur pour le champ mot de passe (à ajouter au FXML)


    private Client clientToEdit = null;
    private Client newRegisteredClient = null; // Utilisé pour retourner le client ajouté/modifié/inscrit
    private String mode = "contact"; // Modes: "contact", "edit_contact_name", "registration"
    private int currentUserId = -1;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // L'UI sera adaptée par setMode
    }

    public void setMode(String mode) {
        this.mode = mode;
        Platform.runLater(() -> {
            // Visibilité par défaut
            if (phoneFieldContainer != null) phoneFieldContainer.setVisible(false);
            if (phoneFieldContainer != null) phoneFieldContainer.setManaged(false);
            if (emailFieldContainer != null) emailFieldContainer.setVisible(false);
            if (emailFieldContainer != null) emailFieldContainer.setManaged(false);
            if (passwordFieldContainer != null) passwordFieldContainer.setVisible(false);
            if (passwordFieldContainer != null) passwordFieldContainer.setManaged(false);
            if (oldNameLabelInfo != null) oldNameLabelInfo.setVisible(false);
            if (oldNameLabelInfo != null) oldNameLabelInfo.setManaged(false);
            if (oldNameField != null) oldNameField.setVisible(false);
            if (oldNameField != null) oldNameField.setManaged(false);
            if (usernameField != null) usernameField.setPromptText("Nom"); // Prompt par défaut

            if ("contact".equals(mode)) {
                if (registerButton != null) registerButton.setText("Ajouter Contact");
                if (phoneFieldContainer != null) phoneFieldContainer.setVisible(true);
                if (phoneFieldContainer != null) phoneFieldContainer.setManaged(true);
                if (phoneField != null) phoneField.setEditable(true);
                if (usernameField != null) usernameField.setPromptText("Nom personnalisé pour le contact");

            } else if ("edit_contact_name".equals(mode)) {
                if (registerButton != null) registerButton.setText("Enregistrer Nouveau Nom");
                if (oldNameLabelInfo != null) oldNameLabelInfo.setVisible(true);
                if (oldNameLabelInfo != null) oldNameLabelInfo.setManaged(true);
                if (oldNameField != null) oldNameField.setVisible(true);
                if (oldNameField != null) oldNameField.setManaged(true);
                if (usernameField != null) usernameField.setPromptText("Nouveau nom personnalisé");

            } else if ("registration".equals(mode)) {
                if (registerButton != null) registerButton.setText("S'inscrire");
                if (usernameField != null) usernameField.setPromptText("Nom d'utilisateur");
                if (phoneFieldContainer != null) phoneFieldContainer.setVisible(true);
                if (phoneFieldContainer != null) phoneFieldContainer.setManaged(true);
                if (phoneField != null) phoneField.setEditable(true);
                if (emailFieldContainer != null) emailFieldContainer.setVisible(true);
                if (emailFieldContainer != null) emailFieldContainer.setManaged(true);
                if (passwordFieldContainer != null) passwordFieldContainer.setVisible(true);
                if (passwordFieldContainer != null) passwordFieldContainer.setManaged(true);
            }
        });
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public void setDataForEdit(Client contactToEdit, String currentCustomName) {
        this.clientToEdit = contactToEdit;
        setMode("edit_contact_name"); // Assure que l'UI est correcte pour l'édition
        Platform.runLater(() -> {
            if (usernameField != null) {
                 usernameField.setText(currentCustomName != null ? currentCustomName : "");
            }
            if (oldNameField != null && clientToEdit != null) {
                // Afficher le nom personnalisé actuel comme "ancien nom"
                oldNameField.setText(currentCustomName != null ? currentCustomName : clientToEdit.getNomUtilisateur());
            }
            // Le numéro de téléphone n'est pas montré/édité ici pour la modification de nom
        });
    }


    @FXML
    private void handleRegisterOrUpdateContact(ActionEvent event) throws SQLException {
        String usernameOrCustomName = usernameField.getText().trim();
        String phone = (phoneField != null && phoneField.isVisible()) ? phoneField.getText().trim() : "";
        String email = (emailField != null && emailField.isVisible()) ? emailField.getText().trim() : "";
        String password = (passwordField != null && passwordField.isVisible()) ? passwordField.getText() : ""; // Ne pas trimmer le mdp

        if ("contact".equals(mode)) {
            if (currentUserId <= 0) {
                showAlert(AlertType.ERROR, "Erreur Utilisateur", "Utilisateur actuel non identifié."); return;
            }
            if (usernameOrCustomName.isEmpty()) {
                showAlert(AlertType.ERROR, "Champ Requis", "Veuillez entrer un nom personnalisé pour le contact."); return;
            }
            if (phone.isEmpty()) {
                showAlert(AlertType.ERROR, "Champ Requis", "Le numéro de téléphone est obligatoire pour ajouter un contact."); return;
            }
            // Utiliser la validation générique pour l'ajout de contact
            if (!Client.validerTelephoneFormatGeneric(phone)) {
                showAlert(AlertType.ERROR, "Format Invalide", "Le numéro de téléphone doit être au format international (ex: +237xxxxxxxxx ou autre format international valide)."); return;
            }
            addContactByPhoneAndName(usernameOrCustomName, phone);

        } else if ("edit_contact_name".equals(mode)) {
            if (currentUserId <= 0) {
                showAlert(AlertType.ERROR, "Erreur Utilisateur", "Utilisateur actuel non identifié."); return;
            }
            if (usernameOrCustomName.isEmpty()) {
                showAlert(AlertType.ERROR, "Champ Requis", "Veuillez entrer le nouveau nom personnalisé."); return;
            }
            if (clientToEdit == null) {
                showAlert(AlertType.ERROR, "Erreur Interne", "Aucun contact sélectionné pour la modification."); return;
            }
            updateContactCustomName(clientToEdit, usernameOrCustomName);

        } else if ("registration".equals(mode)) {
            if (usernameOrCustomName.isEmpty()) {
                showAlert(AlertType.ERROR, "Champ Requis", "Le nom d'utilisateur est obligatoire."); return;
            }
            if (password.isEmpty()) {
                showAlert(AlertType.ERROR, "Champ Requis", "Le mot de passe est obligatoire."); return;
            }
            // Valider qu'au moins email ou téléphone est fourni
            boolean emailProvided = email != null && !email.isEmpty();
            boolean phoneProvided = phone != null && !phone.isEmpty();

            if (!emailProvided && !phoneProvided) {
                showAlert(AlertType.ERROR, "Champ Requis", "Un email OU un numéro de téléphone est requis."); return;
            }
            // Valider format email si fourni
            if (emailProvided && !Client.validerEmailFormat(email)) {
                 showAlert(AlertType.ERROR, "Format Invalide", "Le format de l'email est invalide."); return;
            }
            // Valider format téléphone camerounais si fourni
            if (phoneProvided && !Client.validerTelephoneFormatInscriptionCM(phone)) {
                 showAlert(AlertType.ERROR, "Format Invalide", "Le numéro de téléphone doit être au format +237xxxxxxxxx."); return;
            }
            registerNewUser(usernameOrCustomName, password, email, phone);
        }
    }

    private void registerNewUser(String username, String password, String email, String phone) throws SQLException {
        Client newUser = new Client();
        newUser.setNomUtilisateur(username);
        newUser.setMotDePasse(password); // La méthode setMotDePasse s'occupe du hachage

        if (email != null && !email.isEmpty()) {
            newUser.setEmail(email);
        }
        if (phone != null && !phone.isEmpty()) {
            newUser.setTelephone(phone);
        }
        // Les autres champs (statut, estAdmin) ont des valeurs par défaut dans le constructeur de Client

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            ResultatOperation resultat = newUser.inscrire(conn); // La méthode inscrire gère les vérifications d'unicité et la validation

            if (resultat.estSucces()) {
                this.newRegisteredClient = (Client) resultat.getDonnees();
                showAlert(AlertType.INFORMATION, "Inscription Réussie", "Utilisateur " + this.newRegisteredClient.getNomUtilisateur() + " inscrit avec succès !");
                closeStage();
            } else {
                showAlert(AlertType.ERROR, "Échec de l'inscription", resultat.getMessage());
            }
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }


    private void addContactByPhoneAndName(String customName, String phone) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // 1. Rechercher si un utilisateur existe déjà avec ce numéro de téléphone DANS LA TABLE UTILISATEURS
            Client contactUser = findClientByPhone(conn, phone);

            if (contactUser == null) {
                // Conformément à la nouvelle règle: on ne peut pas ajouter un contact si son numéro n'existe pas dans Utilisateurs.
                showAlert(AlertType.ERROR, "Contact Inconnu", "Aucun utilisateur enregistré avec le numéro de téléphone : " + phone + ". Le contact doit d'abord s'inscrire à l'application.");
                return; // Arrêter le processus d'ajout
            }

            // 2. Si l'utilisateur existe, ajouter ou mettre à jour la relation dans UserContacts
            if (addOrUpdateContactRelation(conn, currentUserId, contactUser.getId(), customName)) {
                this.newRegisteredClient = contactUser; // Le contact trouvé et ajouté à la liste de l'utilisateur
                showAlert(AlertType.INFORMATION, "Succès", "Contact '" + customName + "' ("+phone+") ajouté/mis à jour avec succès.");
                closeStage();
            } else {
                // L'alerte d'erreur est déjà gérée dans addOrUpdateContactRelation si besoin
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur Base de Données", "Erreur lors de l'ajout du contact: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }


    private Client findClientByPhone(Connection conn, String phone) throws SQLException {
        // Cette méthode recherche un utilisateur par son numéro de téléphone dans la table Utilisateurs
        String sql = "SELECT id, nom_utilisateur, email, telephone, statut, est_admin, last_known_ip, last_known_port FROM Utilisateurs WHERE telephone = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Client(
                        rs.getInt("id"),
                        rs.getString("nom_utilisateur"),
                        rs.getString("email"),
                        rs.getString("telephone"),
                        rs.getString("statut"),
                        rs.getBoolean("est_admin"),
                        rs.getString("last_known_ip"),
                        rs.getInt("last_known_port")
                    );
                }
            }
        }
        return null; // Retourne null si aucun utilisateur n'est trouvé avec ce numéro
    }

    private boolean addOrUpdateContactRelation(Connection conn, int ownerId, int contactId, String customName) throws SQLException {
        if (ownerId == contactId) {
            showAlert(AlertType.WARNING, "Action Impossible", "Vous ne pouvez pas vous ajouter vous-même comme contact.");
            return false;
        }

        String checkSql = "SELECT id FROM UserContacts WHERE owner_user_id = ? AND contact_user_id = ?";
        try (PreparedStatement pstmtCheck = conn.prepareStatement(checkSql)) {
            pstmtCheck.setInt(1, ownerId);
            pstmtCheck.setInt(2, contactId);
            try (ResultSet rs = pstmtCheck.executeQuery()) {
                if (rs.next()) {
                    String updateSql = "UPDATE UserContacts SET nom_personnalise = ? WHERE owner_user_id = ? AND contact_user_id = ?";
                    try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateSql)) {
                        pstmtUpdate.setString(1, customName);
                        pstmtUpdate.setInt(2, ownerId);
                        pstmtUpdate.setInt(3, contactId);
                        pstmtUpdate.executeUpdate();
                        return true;
                    }
                } else {
                    String insertSql = "INSERT INTO UserContacts (owner_user_id, contact_user_id, nom_personnalise) VALUES (?, ?, ?)";
                    try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSql)) {
                        pstmtInsert.setInt(1, ownerId);
                        pstmtInsert.setInt(2, contactId);
                        pstmtInsert.setString(3, customName);
                        int rows = pstmtInsert.executeUpdate();
                        return rows > 0;
                    }
                }
            }
        }
    }

    private void updateContactCustomName(Client contactToEdit, String newCustomName) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // La méthode addOrUpdateContactRelation peut être réutilisée ici
            if (addOrUpdateContactRelation(conn, currentUserId, contactToEdit.getId(), newCustomName)) {
                 showAlert(AlertType.INFORMATION, "Succès", "Le nom du contact a été mis à jour.");
                 this.newRegisteredClient = contactToEdit; // Signaler succès pour rafraîchissement
                 closeStage();
            } else {
                 showAlert(AlertType.WARNING, "Échec Mise à Jour", "Le nom du contact n'a pas pu être mis à jour.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur Base de Données", "Erreur lors de la mise à jour du nom: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    public Client getNewRegisteredClient() {
        return newRegisteredClient;
    }

    private void closeStage() {
        Platform.runLater(() -> {
            if (registerButton != null && registerButton.getScene() != null) {
                Stage stage = (Stage) registerButton.getScene().getWindow();
                stage.close();
            }
        });
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeStage();
    }

    private void showAlert(AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
