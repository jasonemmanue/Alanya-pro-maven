package com.Alanya.services;

import com.Alanya.Message;
import com.Alanya.DAO.MessageDAO;
import com.Alanya.DAO.UserDAO; // Ajout pour récupérer le nom d'utilisateur si besoin
import com.Alanya.Mainfirstclientcontroller; // Pour le callback
import javafx.application.Platform;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer; // Remplacé par un type plus spécifique si nécessaire

public class NotificationService {

    private final Map<Integer, Integer> unreadMessagesCount = new HashMap<>();
    private final MessageDAO messageDAO;
    private final UserDAO userDAO; // Pour résoudre les ID en noms d'utilisateur si nécessaire

    // Callback pour rafraîchir la ListView des contacts dans le Mainfirstclientcontroller
    private Consumer<Integer> contactListRefreshCallbackBySenderId; // Prend l'ID de l'expéditeur

    public NotificationService(MessageDAO messageDAO, UserDAO userDAO) {
        this.messageDAO = messageDAO;
        this.userDAO = userDAO;
    }

    public void setContactListRefreshCallback(Consumer<Integer> callback) {
        this.contactListRefreshCallbackBySenderId = callback;
    }

    public void onMessageReceived(Message message, int currentUserId, Integer contactCurrentlyBeingViewedId, int senderId) {
        if (senderId == currentUserId) { // Message envoyé par l'utilisateur actuel à lui-même (ou écho) // 
            return;
        }

        // Le message vient d'être reçu par currentUserId (qui est le destinataire).
        // On met à jour son statut à "Reçu" (1) dans la BDD.
        // L'expéditeur (senderId) devra être notifié pour mettre à jour son affichage à "✓✓" (deux justes gris).
        // Cette notification P2P est une logique supplémentaire à implémenter.
        try {
            if (message.getDatabaseId() > 0) { // S'assurer que le message a un ID de la BDD
                // Ici, currentUserId EST le destinataire du message.
                // senderId est celui qui a envoyé le message.
                messageDAO.updateMessageReadStatus(message.getDatabaseId(), 1); // 1 = Reçu par currentUserId
                message.setReadStatus(1); // Mettre à jour l'objet en mémoire aussi
                System.out.println("NotificationService: Message ID " + message.getDatabaseId() + " de " + senderId + " marqué comme Reçu (statut 1) pour " + currentUserId);

                // TODO: Envoyer un acquittement "reçu" à l'expéditeur (senderId) via P2P/Serveur
                // Ex: if(mainController != null) mainController.sendDeliveryReceipt(senderId, message.getDatabaseId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("NotificationService: Erreur lors du marquage du message ID " + message.getDatabaseId() + " comme Reçu.");
        }

        // Vérifier si la discussion avec cet expéditeur (senderId) est déjà ouverte
        if (contactCurrentlyBeingViewedId != null && contactCurrentlyBeingViewedId == senderId) {
            // Si la discussion est ouverte, marquer immédiatement comme "Lu" (2)
            try {
                if (message.getDatabaseId() > 0) {
                    // currentUserId est le destinataire qui a la discussion ouverte avec senderId
                    messageDAO.updateMessageReadStatus(message.getDatabaseId(), 2); // 2 = Lu par currentUserId
                    message.setReadStatus(2);
                    System.out.println("NotificationService: Message ID " + message.getDatabaseId() + " de " + senderId + " marqué comme Lu (statut 2) car discussion ouverte.");
                    
                    // TODO: Envoyer un acquittement "lu" à l'expéditeur (senderId) via P2P/Serveur
                    // Ex: if(mainController != null) mainController.sendReadReceipt(senderId, message.getDatabaseId(), 2);
                }
                unreadMessagesCount.put(senderId, 0); // Réinitialiser le compteur de non lus en mémoire // 
            } catch (SQLException e) {
                e.printStackTrace(); // 
                System.err.println("NotificationService: Erreur lors du marquage des messages comme lus pour " + senderId); // 
            }
        } else {
            // La discussion n'est pas ouverte, incrémenter le compteur de messages non lus
            int newCount = unreadMessagesCount.getOrDefault(senderId, 0) + 1; // 
            unreadMessagesCount.put(senderId, newCount); // 
            System.out.println("NotificationService: Message non lu reçu de " + senderId + ". Nouveau compte: " + newCount); // 
        }
        // Toujours rafraîchir l'UI pour ce contact pour mettre à jour le badge/statut
        updateContactNotificationUI(senderId); // 
    }

    public void onDiscussionOpened(int contactIdOpened, int currentUserId) {
        try {
            // Marquer tous les messages de contactIdOpened (expéditeur) à currentUserId (destinataire) comme lus (statut 2)
            messageDAO.markAllMessagesFromContactAsRead(contactIdOpened, currentUserId, 2);

            unreadMessagesCount.put(contactIdOpened, 0); // Réinitialiser le compteur en mémoire // 
            updateContactNotificationUI(contactIdOpened); // Mettre à jour l'UI pour ce contact // 
            System.out.println("NotificationService: Discussion ouverte avec " + contactIdOpened + ". Messages marqués comme lus (statut 2)."); // 

            // TODO: Envoyer des acquittements "lu" pour tous les messages concernés à l'expéditeur (contactIdOpened)
            // Ex: if(mainController != null) mainController.sendBulkReadReceipts(contactIdOpened, currentUserId);

        } catch (SQLException e) {
            e.printStackTrace(); // 
            System.err.println("NotificationService: Erreur lors du marquage des messages comme lus pour " + contactIdOpened + " lors de l'ouverture de la discussion."); // 
        }
    }

    public int getUnreadCount(int contactId) {
        return unreadMessagesCount.getOrDefault(contactId, 0);
    }

    private void updateContactNotificationUI(int contactIdToUpdate) {
        if (contactListRefreshCallbackBySenderId != null) {
            Platform.runLater(() -> contactListRefreshCallbackBySenderId.accept(contactIdToUpdate));
        }
    }

    public void loadInitialUnreadCounts(int currentUserId) {
        try {
            Map<Integer, Integer> countsFromDB = messageDAO.getUnreadMessageCountsPerSender(currentUserId);
            unreadMessagesCount.clear();
            unreadMessagesCount.putAll(countsFromDB);

            System.out.println("NotificationService: Compteurs initiaux de messages non lus chargés pour l'utilisateur ID " + currentUserId + ": " + unreadMessagesCount);

            // Rafraîchir tous les contacts visibles
            if (contactListRefreshCallbackBySenderId != null) {
                for (Integer senderId : unreadMessagesCount.keySet()) {
                    updateContactNotificationUI(senderId); // Rafraîchir chaque contact ayant des messages non lus
                }
                // Potentiellement, il faudrait un moyen de rafraîchir aussi ceux qui n'ont PAS de messages non lus
                // pour s'assurer que leur badge est bien à zéro si c'était le cas avant.
                // Un simple appel à contactListView.refresh() dans le MainController après cela pourrait être plus simple.
                // Ou le callback pourrait prendre un ID null pour signifier "tout rafraîchir".
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("NotificationService: Erreur lors du chargement des compteurs de messages non lus initiaux: " + e.getMessage());
        }
    }
}
