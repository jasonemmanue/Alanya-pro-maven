package com.Alanya.DAO;

import com.Alanya.Client;
import com.Alanya.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserContactDAO {

    public void addOrUpdateContactRelation(int ownerUserId, int contactUserId, String customName) throws SQLException {
        String checkSql = "SELECT id FROM UserContacts WHERE owner_user_id = ? AND contact_user_id = ?";
        String insertSql = "INSERT INTO UserContacts (owner_user_id, contact_user_id, nom_personnalise) VALUES (?, ?, ?)";
        String updateSql = "UPDATE UserContacts SET nom_personnalise = ? WHERE owner_user_id = ? AND contact_user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean exists = false;
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, ownerUserId);
                checkPstmt.setInt(2, contactUserId);
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next()) {
                        exists = true;
                    }
                }
            }

            if (exists) {
                try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                    updatePstmt.setString(1, customName);
                    updatePstmt.setInt(2, ownerUserId);
                    updatePstmt.setInt(3, contactUserId);
                    updatePstmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                    insertPstmt.setInt(1, ownerUserId);
                    insertPstmt.setInt(2, contactUserId);
                    insertPstmt.setString(3, customName);
                    insertPstmt.executeUpdate();
                }
            }
        }
    }
    
    public void updateChatBackground(int ownerId, int contactId, byte[] imageData) throws SQLException {
        String sql = "UPDATE UserContacts SET chat_background_image = ? WHERE owner_user_id = ? AND contact_user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (imageData != null && imageData.length > 0) {
                pstmt.setBytes(1, imageData);
            } else {
                pstmt.setNull(1, java.sql.Types.BLOB);
            }
            pstmt.setInt(2, ownerId);
            pstmt.setInt(3, contactId);
            pstmt.executeUpdate();
        }
    }

    public byte[] getChatBackground(int ownerId, int contactId) throws SQLException {
        String sql = "SELECT chat_background_image FROM UserContacts WHERE owner_user_id = ? AND contact_user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerId);
            pstmt.setInt(2, contactId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("chat_background_image");
                }
            }
        }
        return null;
    }
    
    public boolean addContactRelationOnly(int ownerId, int contactId) throws SQLException {
        // Cette méthode est utilisée si aucun nom personnalisé n'est fourni initialement
        // ou si la logique d'ajout de nom personnalisé est gérée séparément.
        // Elle assume que nom_personnalise peut être NULL.
        String sql = "INSERT INTO UserContacts (owner_user_id, contact_user_id) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE owner_user_id = owner_user_id"; // Pour éviter erreur si déjà présent sans changer nom_personnalise
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerId);
            pstmt.setInt(2, contactId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }


    public String getCustomName(int ownerUserId, int contactId) throws SQLException {
        String sql = "SELECT nom_personnalise FROM UserContacts WHERE owner_user_id = ? AND contact_user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerUserId);
            pstmt.setInt(2, contactId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nom_personnalise");
                }
            }
        }
        return null;
    }

    public void deleteContactRelation(int ownerUserId, int contactId) throws SQLException {
        String sql = "DELETE FROM UserContacts WHERE owner_user_id = ? AND contact_user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerUserId);
            pstmt.setInt(2, contactId);
            pstmt.executeUpdate();
        }
    }

    public boolean checkIfContactRelationExists(int ownerUserId, int contactId) throws SQLException {
        String sql = "SELECT id FROM UserContacts WHERE owner_user_id = ? AND contact_user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerUserId);
            pstmt.setInt(2, contactId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Client> getUserContacts(int ownerUserId) throws SQLException {
        List<Client> contacts = new ArrayList<>();
        String sql = "SELECT u.id, u.nom_utilisateur, uc.nom_personnalise, u.email, u.telephone, u.statut, u.est_admin, u.last_known_ip, u.last_known_port " +
                     "FROM UserContacts uc " +
                     "JOIN Utilisateurs u ON uc.contact_user_id = u.id " +
                     "WHERE uc.owner_user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Client contact = new Client(
                        rs.getInt("u.id"),
                        rs.getString("u.nom_utilisateur"),
                        rs.getString("u.email"),
                        rs.getString("u.telephone"),
                        rs.getString("u.statut"),
                        rs.getBoolean("u.est_admin"),
                        rs.getString("u.last_known_ip"),
                        rs.getInt("u.last_known_port")
                    );
                    // Pour afficher le nom personnalisé dans la liste,
                    // vous pouvez le stocker dans un champ de l'objet Client (si vous ajoutez un champ `displayName` ou `customName`)
                    // ou le gérer dans la cellule de la ListView.
                    // Pour l'instant, on le récupère mais on ne le stocke pas dans Client directement ici.
                    // String nomPersonnalise = rs.getString("uc.nom_personnalise");
                    // if (nomPersonnalise != null && !nomPersonnalise.isEmpty()) {
                    //    contact.setDisplayName(nomPersonnalise); // Exemple si Client a setDisplayName
                    // }
                    contacts.add(contact);
                }
            }
        }
        return contacts;
    }
}