package com.Alanya.services;

import com.Alanya.DAO.UserDAO;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class PresenceService {

    private final UserDAO userDAO;
    private static final DateTimeFormatter dtfDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
    private static final DateTimeFormatter dtfTime = DateTimeFormatter.ofPattern("HH:mm");

    public PresenceService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void userConnected(int userId) {
        try {
            userDAO.updateLastLoginTimestamp(userId); // Met aussi le statut à 'actif'
            System.out.println("PresenceService: Utilisateur ID " + userId + " connecté, timestamp et statut mis à jour.");
        } catch (SQLException e) {
            System.err.println("PresenceService: Erreur SQL lors de la mise à jour du statut de connexion pour l'utilisateur ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void userDisconnected(int userId, String statusToSet) {
        try {
            userDAO.updateLastLogoutTimestampAndStatus(userId, statusToSet);
            System.out.println("PresenceService: Utilisateur ID " + userId + " déconnecté, timestamp et statut '" + statusToSet + "' mis à jour.");
        } catch (SQLException e) {
            System.err.println("PresenceService: Erreur SQL lors de la mise à jour du statut de déconnexion pour l'utilisateur ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Récupère le statut de présence formaté pour un contact.
     * @param contactId L'ID du contact.
     * @param isCurrentlyOnline Indique si le contact est activement détecté comme en ligne (ex: via P2P info du serveur central).
     * @return Une chaîne décrivant le statut.
     */
    public String getPresenceStatusForContact(int contactId, boolean isCurrentlyOnline) {
        if (isCurrentlyOnline) {
            // On pourrait aussi vérifier le statut en BDD, mais l'info du serveur central est plus temps réel.
            return "En ligne";
        } else {
            try {
                // Si pas activement en ligne, vérifier le statut en BDD et la dernière déconnexion.
                String dbStatus = userDAO.getUserStatus(contactId);
                if ("actif".equalsIgnoreCase(dbStatus)) {
                    // L'utilisateur est marqué comme 'actif' en BDD mais pas détecté par le serveur central.
                    // Cela peut arriver si la déconnexion n'a pas été propre.
                    // On se fie à la dernière déconnexion s'il y en a une.
                }

                Timestamp lastSeenTimestamp = userDAO.getLastLogoutTimestamp(contactId);
                if (lastSeenTimestamp != null) {
                    LocalDateTime lastSeenDateTime = lastSeenTimestamp.toLocalDateTime();
                    long daysBetween = ChronoUnit.DAYS.between(lastSeenDateTime.toLocalDate(), LocalDateTime.now().toLocalDate());

                    if (daysBetween == 0) {
                        return "Vu aujourd'hui à " + lastSeenDateTime.format(dtfTime);
                    } else if (daysBetween == 1) {
                        return "Vu hier à " + lastSeenDateTime.format(dtfTime);
                    } else {
                        return "Vu le " + lastSeenDateTime.format(dtfDateTime);
                    }
                } else {
                    // Pas de timestamp de déconnexion, pourrait être un nouveau compte ou jamais déconnecté proprement.
                    // On pourrait vérifier la date d'inscription ou le statut en BDD.
                    if ("inactif".equalsIgnoreCase(dbStatus) || "hors-ligne".equalsIgnoreCase(dbStatus)) {
                         return "Hors ligne";
                    }
                    return "Statut inconnu"; // Ou "Hors ligne" par défaut
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "Statut indisponible";
            }
        }
    }
}
