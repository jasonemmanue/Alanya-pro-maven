package com.Alanya;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
// Nouveaux imports pour UserDAO et PresenceService
import com.Alanya.DAO.UserDAO;
import com.Alanya.services.PresenceService;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainAthentificationcontroller {

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
        // Initialiser les services ici si nécessaire ou les passer via constructeur si possible
        userDAO = new UserDAO();
        presenceService = new PresenceService(userDAO); // PresenceService a besoin de UserDAO
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



    @FXML
    void connection(ActionEvent event) {
        String identifier = loginIdentifierField.getText().trim();
        String password = passwordLoginField.getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "Validation échouée", "Champs Vides", "Veuillez entrer votre nom d'utilisateur/email/téléphone et votre mot de passe.");
            return;
        }
        connectbutton.setDisable(true);
        Task<ResultatOperation> loginTask = new Task<>() {
            @Override
            protected ResultatOperation call() throws Exception {
                Connection conn = null;
                try {
                    conn = DatabaseConnection.getConnection();
                    if (conn == null) {
                         throw new SQLException("Impossible d'établir la connexion à la base de données. Vérifiez la configuration.");
                    }
                    // La méthode Client.connexion autorise déjà la connexion si statut = 'hors-ligne'
                    return Client.connexion(conn, identifier, password);
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
                connectbutton.setDisable(false);

                if (result.estSucces()) {
                    Client loggedInClient = (Client) result.getDonnees();
                    System.out.println("Utilisateur localement authentifié: " + loggedInClient.getNomUtilisateur() + " avec statut BDD: " + loggedInClient.getStatut());

                    if (presenceService != null && loggedInClient.getId() > 0) {
                        try {
                            presenceService.userConnected(loggedInClient.getId());
                            System.out.println("Statut de " + loggedInClient.getNomUtilisateur() + " mis à 'actif' en BDD.");
                            // Mettre à jour l'objet en mémoire aussi pour cohérence immédiate
                            loggedInClient.setStatut("actif"); 
                        } catch (Exception e) { // SQLException est déjà gérée dans PresenceService/UserDAO
                            System.err.println("Erreur lors de la mise à jour du statut en 'actif' pour " + loggedInClient.getNomUtilisateur() + ": " + e.getMessage());
                            // Continuer quand même, mais logger l'erreur.
                        }
                    } else {
                        System.err.println("PresenceService non initialisé ou ID client invalide, mise à jour du statut non effectuée localement.");
                    }


                    // Afficher l'alerte de succès après la tentative de mise à jour du statut
                    showAlert(AlertType.INFORMATION, "Connexion Réussie", "Bienvenue", result.getMessage());


                    Platform.runLater(() -> {
                         try {
                             URL mainAppFxmlUrl = getClass().getResource(MAIN_APP_FXML_PATH);
                             if (mainAppFxmlUrl == null) {
                                 System.err.println("Impossible de trouver le fichier FXML principal : " + MAIN_APP_FXML_PATH);
                                 showAlert(AlertType.ERROR, "Erreur de Chargement", "Fichier Manquant", "Impossible de charger l'interface principale (fichier FXML non trouvé).");
                                 return;
                             }

                             FXMLLoader mainLoader = new FXMLLoader(mainAppFxmlUrl);
                             Parent mainRoot = mainLoader.load();

                             Mainfirstclientcontroller mainController = mainLoader.getController();
                             if (mainController != null) {
                            	 mainController.setAuthenticatedUser(loggedInClient, password);
                                 mainController.connectToServer();
                             } else {
                                  System.err.println("Contrôleur principal (Mainfirstclientcontroller) non trouvé.");
                                  showAlert(AlertType.ERROR, "Erreur de Chargement", "Erreur Interne", "Impossible d'initialiser l'interface principale (contrôleur non trouvé).");
                                  return;
                             }

                             Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                             stage.setTitle("ALANYA");

                             Scene mainScene = new Scene(mainRoot,1000,600); // Dimensions souhaitées
                             stage.setScene(mainScene);
                             stage.setResizable(true);
                             stage.centerOnScreen();
                             stage.show();

                             stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                                 @Override
                                 public void handle(WindowEvent event) {
                                     System.out.println("Fermeture de la fenêtre principale demandée...");
                                     if (mainController != null) {
                                         mainController.stopCapture(); 
                                         mainController.disconnectFromServer(); // S'assurer que la déconnexion propre est appelée
                                     }
                                     Platform.exit(); // Quitter l'application JavaFX
                                     System.exit(0); // Forcer l'arrêt de la JVM
                                 }
                             });
                             loginIdentifierField.clear();
                             passwordLoginField.clear();
                         } catch (IOException e) {
                             System.err.println("Erreur lors du chargement de l'interface principale: " + e.getMessage());
                             e.printStackTrace();
                             showAlert(AlertType.ERROR, "Erreur de Chargement", "Erreur FXML", "Impossible de charger l'interface principale (erreur I/O).");
                         } catch (Exception e) {
                        	 System.err.println("Une erreur inattendue est survenue lors du chargement de l'interface principale: " + e.getMessage());
                             e.printStackTrace();
                             showAlert(AlertType.ERROR, "Erreur Inattendue", "Erreur Critique", "Une erreur est survenue lors du chargement de l'interface principale: " + e.getMessage());
                         }
                    });
                } else { // Échec de la connexion (result.estSucces() est false)
                     showAlert(AlertType.ERROR, "Échec de la Connexion", "Erreur", result.getMessage());
                     loginIdentifierField.clear();
                     passwordLoginField.clear();
                }
            }

            @Override
            protected void failed() {
                 super.failed();
                 connectbutton.setDisable(false);
                 Throwable exception = getException();
                 System.err.println("Erreur de connexion en arrière-plan: " + (exception != null ? exception.getMessage() : "Cause inconnue"));
                 if (exception != null) exception.printStackTrace();
                 showAlert(AlertType.ERROR, "Échec de la Connexion", "Erreur Serveur/BDD", "Une erreur inattendue s'est produite lors de la tentative de connexion.");
                 loginIdentifierField.clear();
                 passwordLoginField.clear();
            }

            @Override
            protected void cancelled() {
                 super.cancelled();
                 connectbutton.setDisable(false);
                 showAlert(AlertType.WARNING, "Connexion Annulée", null, "L'opération de connexion a été annulée.");
                 loginIdentifierField.clear();
                 passwordLoginField.clear();
            }
        };
         new Thread(loginTask).start();
    }


    @FXML
    void switchtoinscriptmenu(ActionEvent event) {
        // ... (code inchangé)
        try {
            URL fxmlUrl = getClass().getResource("/com/Alanya/Inscription.fxml");
             if (fxmlUrl == null) {
                  System.err.println("Impossible de trouver Inscription.fxml !");
                  showAlert(AlertType.ERROR, "Erreur de Chargement", "Fichier Manquant", "Impossible de charger la page d'inscription.");
                  return;
             }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("ALANYA INSCRIPTION");
            Scene scene = new Scene(root, 700, 500); 
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de Inscription.fxml : " + e.getMessage());
            e.printStackTrace();
             showAlert(AlertType.ERROR, "Erreur de Chargement", "Erreur FXML", "Une erreur est survenue lors de l'affichage de la page d'inscription.");
        }
    }

    @FXML
    void switchtofirstmenu(ActionEvent event) {
        // ... (code inchangé)
        try {
            Node sourceNode = (Node) event.getSource();
            if (sourceNode == null || sourceNode.getScene() == null) {
                System.err.println("Impossible de déterminer la scène actuelle pour retourner au menu principal.");
                if (Stage.getWindows().isEmpty() || !(Stage.getWindows().get(0) instanceof Stage)) {
                     showAlert(AlertType.ERROR, "Erreur de Navigation", "Erreur Interne", "Impossible de retourner à la page de connexion.");
                     return;
                }
                sourceNode = Stage.getWindows().get(0).getScene().getRoot(); 
                if (sourceNode == null) {
                     showAlert(AlertType.ERROR, "Erreur de Navigation", "Erreur Interne", "Impossible de retourner à la page de connexion (scène non trouvée).");
                     return;
                }
            }


            URL fxmlUrl = getClass().getResource("/com/Alanya/Interfaceauthentification.fxml");
             if (fxmlUrl == null) {
                  System.err.println("Impossible de trouver Interfaceauthentification.fxml !");
                  showAlert(AlertType.ERROR, "Erreur de Chargement", "Fichier Manquant", "Impossible de charger la page de connexion.");
                  return;
             }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setTitle("ALANYA");
            Scene scene = new Scene(root, 700, 500); 
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de Interfaceauthentification.fxml : " + e.getMessage());
            e.printStackTrace();
             showAlert(AlertType.ERROR, "Erreur de Chargement", "Erreur FXML", "Une erreur est survenue lors de l'affichage de la page de connexion.");
        }
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
}
