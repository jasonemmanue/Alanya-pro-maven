package com.Alanya;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.Alanya.DAO.MessageDAO;
import com.Alanya.DAO.UserDAO;
import com.Alanya.services.PresenceService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainAthentificationcontroller {

	 @FXML private ProgressIndicator loadingIndicator;
	    @FXML private VBox loginFormVBox;

	    @FXML
	    private Label connectionlabel;
	    @FXML
	    private Button connectbutton;

	    @FXML
	    private Button inscriptmenu;
	    @FXML
	    private Button inscriptionbutton;

	    @FXML
	    private VBox principalbox;
	    @FXML
	    private Label createlabel;

	    @FXML
	    private VBox inscriptionpage;
	    @FXML
	    private Button returnbutton;

	    @FXML
	    private TextField usernameRegField;
	    @FXML
	    private TextField emailField;
	    @FXML
	    private TextField phoneField;
	    @FXML
	    private PasswordField passwordRegField;
	    @FXML
	    private PasswordField confirmPasswordField;

	    @FXML
	    private TextField loginIdentifierField;
	    @FXML
	    private PasswordField passwordLoginField;

	    private static final String MAIN_APP_FXML_PATH = "/com/Alanya/AcceuilAlanya.fxml"; // Assurez-vous que ce chemin est correct

	    // Instances pour mettre à jour le statut après connexion
	    private UserDAO userDAO;
	    private PresenceService presenceService;
    
    @FXML
    public void initialize() {
        this.userDAO = new UserDAO();
        this.presenceService = new PresenceService(this.userDAO);
    }

    @FXML
    void connection(ActionEvent event) {
        String identifier = loginIdentifierField.getText().trim();
        String password = passwordLoginField.getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "Champs Vides", "Veuillez entrer vos informations.", password);
            return;
        }

        // Affiche l'animation de chargement
        loginFormVBox.setVisible(false);
        loadingIndicator.setVisible(true);
        connectionlabel.setText("Connexion en cours, veuillez patienter...");

        // Crée une tâche d'arrière-plan pour ne pas geler l'interface
        Task<Map<String, Object>> loginTask = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                Map<String, Object> finalResult = new HashMap<>();
                
                // Tâche 1: Authentification
                ResultatOperation authResult = Client.connexion(DatabaseConnection.getConnection(), identifier, password);
                finalResult.put("authResult", authResult);
                
                if (!authResult.estSucces()) {
                    return finalResult; // Arrête tout si l'authentification échoue
                }

                Client loggedInClient = (Client) authResult.getDonnees();
                
                // Tâche 2: Mise à jour du statut en "actif"
                presenceService.userConnected(loggedInClient.getId());
                loggedInClient.setStatut("actif");
                
                // Tâche 3: Pré-chargement des contacts et des messages non lus
                List<Mainfirstclientcontroller.ClientDisplayWrapper> contacts = fetchUserContactsFromDB(loggedInClient.getId());
                Map<Integer, Integer> unreadCounts = new MessageDAO().getUnreadMessageCountsPerSender(loggedInClient.getId());
                
                finalResult.put("contacts", contacts);
                finalResult.put("unreadCounts", unreadCounts);
                
                return finalResult;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Map<String, Object> resultData = getValue();
                ResultatOperation authResult = (ResultatOperation) resultData.get("authResult");

                if (authResult.estSucces()) {
                    // Si succès, on passe à l'interface principale
                    Client loggedInClient = (Client) authResult.getDonnees();
                    @SuppressWarnings("unchecked")
                    List<Mainfirstclientcontroller.ClientDisplayWrapper> contacts = (List<Mainfirstclientcontroller.ClientDisplayWrapper>) resultData.get("contacts");
                    @SuppressWarnings("unchecked")
                    Map<Integer, Integer> unreadCounts = (Map<Integer, Integer>) resultData.get("unreadCounts");

                    switchToMainInterface(event, loggedInClient, password, contacts, unreadCounts);
                } else { 
                    // Si échec, on gère l'erreur et on ré-affiche le formulaire
                     handleFailure(null, authResult.getMessage());
                }
            }

            @Override
            protected void failed() {
                 super.failed();
                 handleFailure(getException(), "Une erreur inattendue s'est produite.");
            }
            
            private void handleFailure(Throwable exception, String message) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loginFormVBox.setVisible(true);
                    connectionlabel.setText("");
                    showAlert(AlertType.ERROR, "Échec de la Connexion", message, message);
                    if (exception != null) {
                        exception.printStackTrace();
                    }
                });
            }
        };

        new Thread(loginTask).start();
    }
    @FXML
    void switchtofirstmenu(ActionEvent event) {
        try {
            Node sourceNode = (Node) event.getSource();
            if (sourceNode == null || sourceNode.getScene() == null) {
                System.err.println("Impossible de déterminer la scène actuelle pour retourner au menu principal.");
                if (Stage.getWindows().isEmpty() || !(Stage.getWindows().get(0) instanceof Stage)) {
                     showAlert(AlertType.ERROR, "Erreur de Navigation", "Erreur Interne", null);
                     return;
                }
                sourceNode = Stage.getWindows().get(0).getScene().getRoot(); 
                if (sourceNode == null) {
                     showAlert(AlertType.ERROR, "Erreur de Navigation", "Erreur Interne", null);
                     return;
                }
            }


            URL fxmlUrl = getClass().getResource("/com/Alanya/Interfaceauthentification.fxml");
             if (fxmlUrl == null) {
                  System.err.println("Impossible de trouver Interfaceauthentification.fxml !");
                  showAlert(AlertType.ERROR, "Erreur de Chargement", "Fichier Manquant", null);
                  return;
             }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setTitle("ALANYA");
            Scene scene = new Scene(root, 1000, 600); 
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de Interfaceauthentification.fxml : " + e.getMessage());
            e.printStackTrace();
             showAlert(AlertType.ERROR, "Erreur de Chargement", "Erreur FXML", null);
        }
    }
    private void switchToMainInterface(ActionEvent event, Client user, String password, List<Mainfirstclientcontroller.ClientDisplayWrapper> contacts, Map<Integer, Integer> unreadCounts) {
        try {
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource(MAIN_APP_FXML_PATH));
            Parent mainRoot = mainLoader.load();
            Mainfirstclientcontroller mainController = mainLoader.getController();

            mainController.setupApplication(user, password, contacts, unreadCounts);

            Stage stage = (Stage) loginFormVBox.getScene().getWindow();
            stage.setTitle("ALANYA");
            Scene mainScene = new Scene(mainRoot,1000,600); 
            
            stage.setScene(mainScene);
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.setOnCloseRequest(e -> {
                mainController.disconnectFromServer();
                Platform.exit();
                System.exit(0);
            });

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur Critique", "Impossible de charger l'interface principale.", password);
        }
    }

    private static List<Mainfirstclientcontroller.ClientDisplayWrapper> fetchUserContactsFromDB(int ownerUserId) {
        List<Mainfirstclientcontroller.ClientDisplayWrapper> contacts = new ArrayList<>();
        String sql = "SELECT u.id, u.nom_utilisateur, u.email, u.telephone, u.statut, u.est_admin, u.last_known_ip, u.last_known_port, u.profile_picture, uc.nom_personnalise "
                + "FROM UserContacts uc JOIN Utilisateurs u ON uc.contact_user_id = u.id "
                + "WHERE uc.owner_user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Client contactUser = new Client(
                        rs.getInt("u.id"), rs.getString("u.nom_utilisateur"), rs.getString("u.email"),
                        rs.getString("u.telephone"), rs.getString("u.statut"), rs.getBoolean("u.est_admin"),
                        rs.getString("u.last_known_ip"), rs.getInt("u.last_known_port")
                    );
                    byte[] profilePicBytes = rs.getBytes("u.profile_picture");
                    if (profilePicBytes != null) {
                        contactUser.setProfilePicture(profilePicBytes);
                    }
                    String nomPersonnalise = rs.getString("nom_personnalise");
                    contacts.add(new Mainfirstclientcontroller.ClientDisplayWrapper(contactUser, nomPersonnalise));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Impossible de charger les contacts personnels pour l'utilisateur ID: " + ownerUserId);
        }
        return contacts;
    }

    private void showAlert(AlertType type, String title, String header, String content) {
        if (Platform.isFxApplicationThread()) {
             Alert alert = new Alert(type);
             alert.setTitle(title);
             alert.setHeaderText(header);
             alert.setContentText(content);
             alert.showAndWait();
        } else {
             Platform.runLater(() -> {
                 Alert alert = new Alert(type);
                 alert.setTitle(title);
                 alert.setHeaderText(header);
                 alert.setContentText(content);
                 alert.showAndWait();
             });
        }
    }
    
    @FXML
    void switchtoinscriptmenu(ActionEvent event) {
        
        try {
            URL fxmlUrl = getClass().getResource("/com/Alanya/Inscription.fxml");
             if (fxmlUrl == null) {
                  System.err.println("Impossible de trouver Inscription.fxml !");
                  showAlert(AlertType.ERROR, "Erreur de Chargement", "Fichier Manquant", null);
                  return;
             }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("ALANYA INSCRIPTION");
            Scene scene = new Scene(root, 1000, 600); 
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de Inscription.fxml : " + e.getMessage());
            e.printStackTrace();
             showAlert(AlertType.ERROR, "Erreur de Chargement", "Erreur FXML", null);
        }
    }
    
    
    @FXML
    void inscription(ActionEvent event) {
        // ... (code de la méthode inscription inchangé)
        String username = usernameRegField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordRegField.getText(); 
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(AlertType.WARNING, "Validation échouée", "Champs Incomplets", "Veuillez remplir tous les champs obligatoires (nom d'utilisateur, mot de passe).");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(AlertType.WARNING, "Validation échouée", "Mots de Passe Différents", "Les mots de passe ne correspondent pas.");
            passwordRegField.clear();
            confirmPasswordField.clear();
            return;
        }

        boolean emailProvided = !email.isEmpty();
        boolean phoneProvided = !phone.isEmpty();

        if (!emailProvided && !phoneProvided) {
            showAlert(AlertType.WARNING, "Validation échouée", "Information Manquante", "Veuillez fournir soit un email, soit un numéro de téléphone.");
            return;
        }

        if (emailProvided && !Client.validerEmailFormat(email)) { 
            showAlert(AlertType.WARNING, "Validation échouée", "Format Email Incorrect", "Le format de l'email est invalide.");
            return;
        }

        if (phoneProvided && !Client.validerTelephoneFormatInscriptionCM(phone)) { 
             showAlert(AlertType.WARNING, "Validation échouée", "Format Téléphone Incorrect", "Le format du numéro de téléphone est invalide. Il doit être au format +237xxxxxxxxx.");
             return;
        }
        inscriptionbutton.setDisable(true);
        Task<ResultatOperation> inscriptionTask = new Task<>() {
            @Override
            protected ResultatOperation call() throws Exception {
                Connection conn = null;
                try {
                    conn = DatabaseConnection.getConnection();
                    if (conn == null) {
                         throw new SQLException("Impossible d'établir la connexion à la base de données.");
                    }

                    Client newClient = new Client();
                    newClient.setNomUtilisateur(username);
                    if (emailProvided) {
                        newClient.setEmail(email);
                    }
                    if (phoneProvided) {
                         newClient.setTelephone(phone);
                    }
                    newClient.setMotDePasse(password); 
                    return newClient.inscrire(conn); 

                } finally {
                    if (conn != null && !conn.isClosed()) {
                        try {
                            conn.close();
                        } catch (SQLException e) {
                            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
                        }
                    }
                }
            }

             @Override
            protected void succeeded() {
                super.succeeded(); 
                ResultatOperation result = getValue();
                inscriptionbutton.setDisable(false);

                AlertType alertType = result.estSucces() ? AlertType.INFORMATION : AlertType.ERROR;
                showAlert(alertType, result.estSucces() ? "Inscription Réussie" : "Erreur d'Inscription",
                          result.estSucces() ? "Succès" : "Échec", result.getMessage());

                if (result.estSucces()) {
                    usernameRegField.clear();
                    emailField.clear();
                    phoneField.clear();
                    passwordRegField.clear();
                    confirmPasswordField.clear();
                    Platform.runLater(() -> switchtofirstmenu(new ActionEvent(inscriptmenu, null))); 
                }
            }

            @Override
            protected void failed() {
                 super.failed(); 
                 inscriptionbutton.setDisable(false);
                 Throwable exception = getException();
                 showAlert(AlertType.ERROR, "Erreur d'Inscription", "Une erreur inattendue s'est produite.", exception != null ? exception.getMessage() : "Cause inconnue");
                 if (exception != null) exception.printStackTrace();
            }

            @Override
            protected void cancelled() {
                 super.cancelled(); 
                 inscriptionbutton.setDisable(false);
                 showAlert(AlertType.WARNING, "Inscription Annulée", null, "L'opération d'inscription a été annulée.");
             }
        };
         new Thread(inscriptionTask).start();
    }

    
}