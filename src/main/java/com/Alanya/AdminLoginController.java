package com.Alanya;

import com.Alanya.Client;
import com.Alanya.DAO.UserDAO;
import com.Alanya.DatabaseConnection;
import com.Alanya.model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class AdminLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private UserDAO userDAO = new UserDAO();

    /**
     * Gère l'événement de clic sur le bouton de connexion.
     * Valide les informations d'identification de l'administrateur.
     */
    @FXML
    private void handleAdminLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Les champs ne peuvent pas être vides.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Utilise la méthode de connexion existante
            var authResult = Client.connexion(conn, username, password);

            // Vérifie si la connexion a réussi ET si l'utilisateur est bien un administrateur
            if (authResult.estSucces() && authResult.getClient().isEstAdmin()) {
                // Succès : ouvrir le panneau principal de l'admin
                openAdminPanel(authResult.getClient());
                
                // Fermer la fenêtre de connexion actuelle
                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                currentStage.close();
            } else {
                // Échec : afficher un message d'erreur
                Platform.runLater(() -> errorLabel.setText("Identifiants invalides ou non administrateur."));
            }
        } catch (SQLException e) {
            Platform.runLater(() -> errorLabel.setText("Erreur de base de données."));
            e.printStackTrace();
        }
    }

    /**
     * Ouvre la fenêtre du panneau d'administration après une connexion réussie.
     * @param adminClient L'objet Client de l'administrateur authentifié.
     */
    private void openAdminPanel(Client adminClient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/Alanya/AdminPanel.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur du panneau pour l'initialiser
            AdminPanelController adminController = loader.getController();

            // Créer un objet User simple pour le contrôleur du panneau
            User adminUser = new User();
            adminUser.setId(adminClient.getId());
            adminUser.setUsername(adminClient.getNomUtilisateur());
            adminUser.setAdmin(adminClient.isEstAdmin());

            // Passer l'objet admin pour déclencher le chargement des données
            adminController.setAdminUser(adminUser);

            // Créer et afficher la nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Alanya - Panneau d'Administration");
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'erreur de chargement FXML si nécessaire
        }
    }
}