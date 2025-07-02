package com.Alanya;

import com.Alanya.Client;
import com.Alanya.DAO.SupportConversationDAO;
import com.Alanya.DAO.UserDAO;
import com.Alanya.model.SupportMessage;
import com.Alanya.model.SupportMessage.SenderType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

public class SupportChatController {

    @FXML private ScrollPane messageScrollPane;
    @FXML private VBox messageDisplayArea;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton; // Assurez-vous d'avoir cet fx:id sur votre bouton

    private Client currentUser;
    private Client adminUser;
    private SupportConversationDAO supportDAO;
    private UserDAO userDAO;

    private static final String SUPPORT_SERVER_HOST = "localhost";
    private static final int SUPPORT_PORT = 9001;
    private Socket supportSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean isConnected = false;

    @FXML
    public void initialize() {
        this.supportDAO = new SupportConversationDAO();
        this.userDAO = new UserDAO();
        messageDisplayArea.heightProperty().addListener((obs, oldVal, newVal) -> messageScrollPane.setVvalue(1.0));
    }

    public void initData(Client user) {
        this.currentUser = user;
        try {
            this.adminUser = userDAO.findUserById(100000001L);
            if (adminUser == null) {
                disableChat("Service client indisponible.");
                return;
            }
            loadMessages();
            connectToSupport();
        } catch (SQLException e) {
            e.printStackTrace();
            disableChat("Erreur de base de données.");
        }
    }

    private void connectToSupport() {
        new Thread(() -> {
            try {
                supportSocket = new Socket(SUPPORT_SERVER_HOST, SUPPORT_PORT);
                out = new ObjectOutputStream(supportSocket.getOutputStream());
                in = new ObjectInputStream(supportSocket.getInputStream());
                isConnected = true;

                out.writeLong(currentUser.getId());
                out.flush();

                while (isConnected) {
                    try {
                        SupportMessage adminReply = (SupportMessage) in.readObject();
                        addMessageToDisplay(adminReply);
                    } catch (IOException | ClassNotFoundException e) {
                        if (isConnected) { // Ne montrer l'erreur que si on ne s'attend pas à la déconnexion
                            Platform.runLater(() -> disableChat("Déconnecté du support."));
                            isConnected = false;
                        }
                        break; // Sortir de la boucle
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> disableChat("Impossible de joindre le support."));
            }
        }).start();
    }
    
    @FXML
    private void handleSendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || !isConnected) {
            System.err.println("Envoi annulé. Message vide ou non connecté.");
            return;
        }

        SupportMessage message = new SupportMessage(currentUser.getId(), adminUser.getId(), content, SenderType.USER);

        try {
            out.writeObject(message); // Envoyer d'abord sur le réseau
            out.flush();

            supportDAO.insertMessage(message); // Puis sauvegarder en BDD
            addMessageToDisplay(message); // Et afficher localement
            
            messageInput.clear();
        } catch (IOException e) {
            e.printStackTrace();
            disableChat("Erreur de connexion, impossible d'envoyer le message.");
        } catch (SQLException e) {
            e.printStackTrace();
            // Gérer l'erreur de BDD, mais le message est déjà parti
        }
    }

    private void addMessageToDisplay(SupportMessage message) {
        HBox messageRow = new HBox();
        VBox messageBubble = new VBox();
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        Label timeLabel = new Label(message.getTimestamp().substring(11, 16));
        timeLabel.getStyleClass().add("message-timestamp");
        
        messageBubble.getChildren().addAll(contentLabel, timeLabel);
        messageBubble.getStyleClass().add("message-bubble");
        
        if (message.getSenderType() == SenderType.USER) {
            messageRow.setAlignment(Pos.CENTER_RIGHT);
            messageBubble.getStyleClass().add("user-message");
        } else {
            messageRow.setAlignment(Pos.CENTER_LEFT);
            messageBubble.getStyleClass().add("admin-message");
        }
        
        messageRow.getChildren().add(messageBubble);
        Platform.runLater(() -> messageDisplayArea.getChildren().add(messageRow));
    }
    
    private void disableChat(String reason) {
        messageDisplayArea.getChildren().add(new Label(reason));
        messageInput.setDisable(true);
        sendButton.setDisable(true);
    }
    private void loadMessages() {
        try {
            List<SupportMessage> messages = supportDAO.getConversation(currentUser.getId(), adminUser.getId());
            messageDisplayArea.getChildren().clear();
            for (SupportMessage msg : messages) {
                addMessageToDisplay(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

    

  

    