package com.Alanya;

import com.Alanya.DAO.SupportConversationDAO;
import com.Alanya.DAO.UserDAO;
import com.Alanya.model.SupportMessage;
import com.Alanya.model.SupportMessage.SenderType;
import com.Alanya.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminPanelController {

    @FXML private ListView<User> conversationList;
    @FXML private VBox selectedUserChatArea;
    @FXML private ScrollPane adminMessageScrollPane;
    @FXML private TextArea adminReplyInput;
    @FXML private Button sendReplyButton;
    @FXML private Label chatHeaderLabel;

    private User adminUser;
    private User currentlySelectedUser;
    private SupportConversationDAO supportDAO;
    private UserDAO userDAO;

    private static final int SUPPORT_PORT = 9001;
    private ServerSocket serverSocket;
    private final ExecutorService clientHandlerExecutor = Executors.newCachedThreadPool();
    private final Map<Long, ClientConnectionHandler> activeUserConnections = new ConcurrentHashMap<>();

    @FXML
    public void initialize() {
        this.supportDAO = new SupportConversationDAO();
        this.userDAO = new UserDAO();

        // Affiche un message par défaut dans la liste vide
        conversationList.setPlaceholder(new Label("Aucune conversation de support active."));

        conversationList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentlySelectedUser = newVal;
                chatHeaderLabel.setText("Conversation avec : " + currentlySelectedUser.getUsername());
                loadConversationForUser(currentlySelectedUser);
            } else {
                chatHeaderLabel.setText("Sélectionnez une conversation");
                selectedUserChatArea.getChildren().clear();
            }
        });

        sendReplyButton.setOnAction(event -> handleAdminReply());
        selectedUserChatArea.heightProperty().addListener((obs, oldVal, newVal) -> adminMessageScrollPane.setVvalue(1.0));
    }

    public void setAdminUser(User adminUser) {
        this.adminUser = adminUser;
        loadAllSupportConversations();
        startSupportServer();
    }

    /**
     * CORRIGÉ : Charge les conversations en arrière-plan pour ne pas bloquer l'interface.
     */
    private void loadAllSupportConversations() {
        // On lance le chargement dans un nouveau thread
        new Thread(() -> {
            try {
                // 1. Récupérer les ID des utilisateurs
                List<Long> userIds = supportDAO.getDistinctUserIdsInSupport();
                System.out.println("IDs de support trouvés : " + userIds.size()); // Log de débogage

                if (!userIds.isEmpty()) {
                    // 2. Récupérer les objets User correspondants
                    List<User> users = userDAO.getUsersByIds(userIds);
                    System.out.println("Utilisateurs chargés : " + users.size()); // Log de débogage

                    // 3. Mettre à jour l'interface graphique sur le thread JavaFX
                    Platform.runLater(() -> {
                        conversationList.getItems().setAll(users);
                        System.out.println("Liste des conversations mise à jour dans l'interface."); // Log de débogage
                    });
                } else {
                    // S'il n'y a aucun utilisateur, on s'assure que la liste est vide
                    Platform.runLater(() -> conversationList.getItems().clear());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // En cas d'erreur, on peut afficher une alerte à l'admin
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Impossible de charger les conversations : " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void loadConversationForUser(User user) {
        selectedUserChatArea.getChildren().clear();
        new Thread(() -> {
            try {
                List<SupportMessage> messages = supportDAO.getConversation(user.getId(), adminUser.getId());
                Platform.runLater(() -> {
                    for (SupportMessage msg : messages) {
                        addMessageToAdminDisplay(msg);
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    // --- Le reste de vos méthodes (startSupportServer, handleAdminReply, etc.) reste identique ---
    // ... (copiez-collez ici le reste des méthodes que je vous ai fournies précédemment) ...
    // ... startSupportServer(), handleAdminReply(), addMessageToAdminDisplay(), ClientConnectionHandler ...

    private void startSupportServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(SUPPORT_PORT);
                Platform.runLater(() -> System.out.println("Serveur de support démarré sur le port " + SUPPORT_PORT));
                while (!serverSocket.isClosed()) {
                    Socket userSocket = serverSocket.accept();
                    clientHandlerExecutor.submit(new ClientConnectionHandler(userSocket));
                }
            } catch (IOException e) {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    System.err.println("Erreur critique du serveur de support : " + e.getMessage());
                }
            }
        }).start();
    }

    @FXML
    private void handleAdminReply() {
        String content = adminReplyInput.getText().trim();
        if (content.isEmpty() || adminUser == null || currentlySelectedUser == null) return;

        SupportMessage replyMessage = new SupportMessage(currentlySelectedUser.getId(), adminUser.getId(), content, SenderType.ADMIN);

        try {
            supportDAO.insertMessage(replyMessage);
            addMessageToAdminDisplay(replyMessage);
            adminReplyInput.clear();

            ClientConnectionHandler userConnection = activeUserConnections.get(currentlySelectedUser.getId());
            if (userConnection != null) {
                userConnection.sendMessage(replyMessage);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addMessageToAdminDisplay(SupportMessage message) {
        HBox messageRow = new HBox();
        VBox messageBubble = new VBox();
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        Label timeLabel = new Label(message.getTimestamp().substring(11, 16));
        timeLabel.getStyleClass().add("message-timestamp");
        
        messageBubble.getChildren().addAll(contentLabel, timeLabel);
        messageBubble.getStyleClass().add("message-bubble");
        
        if (message.getSenderType() == SenderType.ADMIN) {
            messageRow.setAlignment(Pos.CENTER_RIGHT);
            messageBubble.getStyleClass().add("user-message");
        } else {
            messageRow.setAlignment(Pos.CENTER_LEFT);
            messageBubble.getStyleClass().add("admin-message");
        }
        
        messageRow.getChildren().add(messageBubble);
        Platform.runLater(() -> selectedUserChatArea.getChildren().add(messageRow));
    }
    private class ClientConnectionHandler implements Runnable {
        private final Socket userSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private Long connectedUserId = null;

        ClientConnectionHandler(Socket socket) {
            this.userSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(userSocket.getOutputStream());
                in = new ObjectInputStream(userSocket.getInputStream());

                this.connectedUserId = in.readLong();
                activeUserConnections.put(connectedUserId, this);
                System.out.println("Utilisateur ID " + connectedUserId + " connecté au support.");

                boolean isNewConversation = conversationList.getItems().stream()
                    .noneMatch(user -> user.getId() == connectedUserId);

                if (isNewConversation) {
                    Platform.runLater(() -> {
                        try {
                            // --- CORRECTION CLÉ ---
                            // 1. On récupère un objet Client complet depuis le DAO.
                            Client connectedClient = userDAO.findUserById(connectedUserId);

                            if (connectedClient != null) {
                                // 2. On crée un objet User simple pour l'interface.
                                User userForList = new User();
                                userForList.setId(connectedClient.getId());
                                userForList.setUsername(connectedClient.getNomUtilisateur());
                                userForList.setAdmin(connectedClient.isEstAdmin());

                                // 3. On ajoute l'objet User à la liste.
                                conversationList.getItems().add(userForList);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                }

                while (userSocket.isConnected()) {
                    SupportMessage receivedMessage = (SupportMessage) in.readObject();
                    try {
                        supportDAO.insertMessage(receivedMessage);
                    } catch (SQLException e) {
                        System.err.println("Erreur de sauvegarde du message de l'utilisateur " + connectedUserId);
                        e.printStackTrace();
                    }

                    if (currentlySelectedUser != null && currentlySelectedUser.getId() == receivedMessage.getUserId()) {
                        addMessageToAdminDisplay(receivedMessage);
                    } else {
                         System.out.println("Message reçu de " + receivedMessage.getUserId() + " (conversation non ouverte)");
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Utilisateur ID " + connectedUserId + " déconnecté du support.");
            } finally {
                if (connectedUserId != null) {
                    activeUserConnections.remove(connectedUserId);
                }
                try { userSocket.close(); } catch (IOException ignored) {}
            }
        }

        public void sendMessage(SupportMessage message) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                System.err.println("Impossible d'envoyer le message à l'ID " + connectedUserId);
            }
        }
    }
}