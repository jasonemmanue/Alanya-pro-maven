package com.Alanya.services;

import com.Alanya.Message;
import com.Alanya.DAO.MessageDAO;
import com.Alanya.DAO.UserDAO; // Ajout pour récupérer le nom d'utilisateur si besoin
import com.Alanya.Mainfirstclientcontroller; // Pour le callback
import javafx.application.Platform;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer; 
import java.util.function.BiConsumer;

public class NotificationService {

    private final Map<Integer, Integer> unreadMessagesCount = new HashMap<>();
    private final MessageDAO messageDAO;
    private final UserDAO userDAO; // Pour résoudre les ID en noms d'utilisateur si nécessaire
    private BiConsumer<Message, Boolean> messageDisplayCallback;
    
    // Callback pour rafraîchir la ListView des contacts dans le Mainfirstclientcontroller
    private Consumer<Integer> contactListRefreshCallbackBySenderId; // Prend l'ID de l'expéditeur

    public NotificationService(MessageDAO messageDAO, UserDAO userDAO) {
        this.messageDAO = messageDAO;
        this.userDAO = userDAO;
    }
    
    public void setMessageDisplayCallback(BiConsumer<Message, Boolean> callback) {
        this.messageDisplayCallback = callback;
    }
    
    public void setInitialUnreadCounts(Map<Integer, Integer> counts) {
        this.unreadMessagesCount.clear();
        if (counts != null) {
            this.unreadMessagesCount.putAll(counts);
        }
    }

    public void setContactListRefreshCallback(Consumer<Integer> callback) {
        this.contactListRefreshCallbackBySenderId = callback;
    }

    public void onMessageReceived(Message message, int currentUserId, Integer contactCurrentlyBeingViewedId, int senderId) {
        if (senderId == currentUserId) {
            return; // Ignorer les messages de soi-même
        }

        // Vérifier si la discussion avec l'expéditeur est déjà ouverte
        if (contactCurrentlyBeingViewedId != null && contactCurrentlyBeingViewedId == senderId) {
            // CAS 1: La conversation est ouverte.
            try {
                // On marque le message comme LU (statut 2)
                if (message.getDatabaseId() > 0) {
                    messageDAO.updateMessageReadStatus(message.getDatabaseId(), 2);
                    message.setReadStatus(2);
                }
                // On s'assure que le compteur en mémoire est à zéro
                unreadMessagesCount.put(senderId, 0);
            } catch (SQLException e) {
                System.err.println("NotificationService: Erreur lors du marquage du message comme Lu.");
                e.printStackTrace();
            }

            // On affiche le message directement dans la fenêtre de chat
            if (messageDisplayCallback != null) {
                Platform.runLater(() -> messageDisplayCallback.accept(message, false));
            }
        } else {
            // CAS 2: La conversation N'EST PAS ouverte.
            try {
                // On marque le message comme REÇU (statut 1)
                if (message.getDatabaseId() > 0) {
                    messageDAO.updateMessageReadStatus(message.getDatabaseId(), 1);
                    message.setReadStatus(1);
                }
            } catch (SQLException e) {
                System.err.println("NotificationService: Erreur lors du marquage du message comme Reçu.");
                e.printStackTrace();
            }
            
            // On incrémente le compteur de messages non lus en mémoire
            int newCount = unreadMessagesCount.getOrDefault(senderId, 0) + 1;
            unreadMessagesCount.put(senderId, newCount);
            System.out.println("NotificationService: Message non lu reçu de " + senderId + ". Nouveau compte: " + newCount);
        }

        // --- CORRECTION FINALE ET CRUCIALE ---
        // Dans tous les cas (conversation ouverte ou non), on notifie l'interface
        // pour qu'elle rafraîchisse la liste des contacts. Cela mettra à jour le
        // badge de notification (soit à 0, soit au nouveau compte).
        updateContactNotificationUI(senderId);
    }
    public void onDiscussionOpened(int contactIdOpened, int currentUserId) {
        try {
            // Met à jour la BDD pour marquer les messages comme lus (statut 2)
            messageDAO.markAllMessagesFromContactAsRead(contactIdOpened, currentUserId, 2);

            // Réinitialise le compteur de messages non lus pour ce contact DANS LA MÉMOIRE du service
            unreadMessagesCount.put(contactIdOpened, 0);

            // --- CORRECTION CLÉ ---
            // Force le rafraîchissement de la liste des contacts dans l'interface graphique.
            // Cela mettra à jour le badge de notification pour qu'il disparaisse.
            updateContactNotificationUI(contactIdOpened);

            System.out.println("NotificationService: Discussion avec " + contactIdOpened + " ouverte. Messages marqués comme lus.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("NotificationService: Erreur lors du marquage des messages comme lus pour " + contactIdOpened);
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
