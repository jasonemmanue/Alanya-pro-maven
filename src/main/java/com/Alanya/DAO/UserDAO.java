package com.Alanya.DAO;

import com.Alanya.Client;
import com.Alanya.DatabaseConnection;
import com.Alanya.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserDAO {

    public Client findUserById(int userId) throws SQLException {
        String sql = "SELECT id, nom_utilisateur, email, telephone, statut, est_admin, last_known_ip, last_known_port, profile_picture FROM Utilisateurs WHERE id = ?";
        Client client = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    client = new Client(
                        rs.getInt("id"),
                        rs.getString("nom_utilisateur"),
                        rs.getString("email"),
                        rs.getString("telephone"),
                        rs.getString("statut"),
                        rs.getBoolean("est_admin"),
                        rs.getString("last_known_ip"),
                        rs.getInt("last_known_port")
                    );
                    client.setProfilePicture(rs.getBytes("profile_picture"));
                }
            }
        }
        return client;
    }

    public byte[] getProfilePicture(int userId) throws SQLException {
        String sql = "SELECT profile_picture FROM Utilisateurs WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("profile_picture");
                }
            }
        }
        return null;
    }

    public Client findUserByUsername(String username) throws SQLException {
        String sql = "SELECT id, nom_utilisateur, email, telephone, statut, est_admin, last_known_ip, last_known_port, derniere_deconnexion_timestamp, profile_picture FROM Utilisateurs WHERE nom_utilisateur = ?";
        Client client = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    client = new Client(
                        rs.getInt("id"),
                        rs.getString("nom_utilisateur"),
                        rs.getString("email"),
                        rs.getString("telephone"),
                        rs.getString("statut"),
                        rs.getBoolean("est_admin"),
                        rs.getString("last_known_ip"),
                        rs.getInt("last_known_port")
                    );
                    client.setProfilePicture(rs.getBytes("profile_picture"));
                }
            }
        }
        return client;
    }
    
    public Client findUserById(long userId) throws SQLException {
        String sql = "SELECT id, nom_utilisateur, email, telephone, statut, est_admin, last_known_ip, last_known_port, profile_picture FROM Utilisateurs WHERE id = ?";
        Client client = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId); // Utilise setLong pour correspondre au type du paramètre

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    client = new Client(
                        rs.getInt("id"), // Le constructeur de Client attend un int
                        rs.getString("nom_utilisateur"),
                        rs.getString("email"),
                        rs.getString("telephone"),
                        rs.getString("statut"),
                        rs.getBoolean("est_admin"),
                        rs.getString("last_known_ip"),
                        rs.getInt("last_known_port")
                    );
                    client.setProfilePicture(rs.getBytes("profile_picture"));
                }
            }
        }
        return client;
    }
    
    public void saveThemePreference(long userId, String themeType, byte[] themeImageData) throws SQLException {
        // On met à jour la photo uniquement si le thème est 'custom_image'
        String sql = "UPDATE Utilisateurs SET theme_preference = ?, photo_arriere_plan_discussion = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, themeType);
            
            if ("custom_image".equals(themeType) && themeImageData != null) {
                pstmt.setBytes(2, themeImageData);
            } else {
                // Si ce n'est pas un thème custom, on efface l'ancienne image pour économiser l'espace
                pstmt.setNull(2, Types.BLOB);
            }
            
            pstmt.setLong(3, userId);
            pstmt.executeUpdate();
        }
    }
    
    public Map<String, Object> loadThemePreference(long userId) throws SQLException {
        Map<String, Object> preferences = new HashMap<>();
        String sql = "SELECT theme_preference, photo_arriere_plan_discussion FROM Utilisateurs WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    preferences.put("type", rs.getString("theme_preference"));
                    preferences.put("image", rs.getBytes("photo_arriere_plan_discussion"));
                }
            }
        }
        return preferences;
    }



    public Client findUserByEmail(String email) throws SQLException {
        String sql = "SELECT id FROM Utilisateurs WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Client(); // On retourne un objet non-null si trouvé
                }
            }
        }
        return null;
    }
    
    public int getUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT id FROM Utilisateurs WHERE nom_utilisateur = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    System.err.println("UserDAO: Utilisateur non trouvé avec le nom: " + username);
                    return -1;
                }
            }
        }
    }

    public Client findUserByPhoneNumber(String phoneNumber) throws SQLException {
        String sql = "SELECT id, nom_utilisateur, email, telephone, statut, est_admin, last_known_ip, last_known_port, profile_picture FROM Utilisateurs WHERE telephone = ?";
        Client client = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    client = new Client(
                        rs.getInt("id"),
                        rs.getString("nom_utilisateur"),
                        rs.getString("email"),
                        rs.getString("telephone"),
                        rs.getString("statut"),
                        rs.getBoolean("est_admin"),
                        rs.getString("last_known_ip"),
                        rs.getInt("last_known_port")
                    );
                    client.setProfilePicture(rs.getBytes("profile_picture"));
                }
            }
        }
        return client;
    }
    
    public void updateUserProfilePicture(int userId, byte[] pictureData) throws SQLException {
        String sql = "UPDATE Utilisateurs SET profile_picture = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (pictureData != null && pictureData.length > 0) {
                pstmt.setBytes(1, pictureData);
            } else {
                pstmt.setNull(1, java.sql.Types.BLOB);
            }
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }
    
    public List<Integer> getOwnerIdsForContact(int contactUserId) throws SQLException {
        List<Integer> ownerIds = new ArrayList<>();
        String sql = "SELECT owner_user_id FROM UserContacts WHERE contact_user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, contactUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ownerIds.add(rs.getInt("owner_user_id"));
                }
            }
        }
        return ownerIds;
    }

    public void updateLastLoginTimestamp(int userId) throws SQLException {
        String sql = "UPDATE Utilisateurs SET derniere_connexion_timestamp = CURRENT_TIMESTAMP, statut = 'actif' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    public void updateLastLogoutTimestampAndStatus(int userId, String status) throws SQLException {
         String sql = "UPDATE Utilisateurs SET derniere_deconnexion_timestamp = CURRENT_TIMESTAMP, statut = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }
    
    public Timestamp getLastLogoutTimestamp(int userId) throws SQLException {
        String sql = "SELECT derniere_deconnexion_timestamp FROM Utilisateurs WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("derniere_deconnexion_timestamp");
                }
            }
        }
        return null;
    }
    
    public String getUserStatus(int userId) throws SQLException {
        String sql = "SELECT statut FROM Utilisateurs WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("statut");
                }
            }
        }
        return "inconnu";
    }

    public void updateUserStatus(String username, String status) throws SQLException {
        String sql = "UPDATE Utilisateurs SET statut = ? WHERE nom_utilisateur = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, username);
            stmt.executeUpdate();
        }
    }

    public void updateClientP2PServerInfo(String username, String host, int port) throws SQLException {
        String sql = "UPDATE Utilisateurs SET last_known_ip = ?, last_known_port = ? WHERE nom_utilisateur = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, host);
            if (port > 0) {
                stmt.setInt(2, port);
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setString(3, username);
            stmt.executeUpdate();
        }
    }
    
    public boolean updateContactPhone(int contactId, String phone) throws SQLException {
        String sql = "UPDATE Utilisateurs SET telephone = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            pstmt.setInt(2, contactId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }

    public Client registerNewUser(String username, String hashedPassword, String email, String phone, String status, boolean isAdmin) throws SQLException {
        String sql = "INSERT INTO Utilisateurs (nom_utilisateur, mot_de_passe, email, telephone, statut, est_admin) VALUES (?, ?, ?, ?, ?, ?)";
        Client newClient = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            if (email != null && !email.isEmpty()) pstmt.setString(3, email); else pstmt.setNull(3, Types.VARCHAR);
            if (phone != null && !phone.isEmpty()) pstmt.setString(4, phone); else pstmt.setNull(4, Types.VARCHAR);
            pstmt.setString(5, status);
            pstmt.setBoolean(6, isAdmin);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        newClient = new Client(newId, username, email, phone, status, isAdmin, null, 0);
                    }
                }
            }
        }
        return newClient;
    }
    
    /**
     * **NOUVELLE MÉTHODE**
     * Vérifie si un ID utilisateur existe déjà dans la base de données.
     * @param userId L'ID à vérifier.
     * @return true si l'ID existe, false sinon.
     * @throws SQLException
     */
    public boolean userIdExists(int userId) throws SQLException {
        String sql = "SELECT id FROM Utilisateurs WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Si rs.next() est vrai, l'ID existe
            }
        }
    }

    public List<User> getUsersByIds(List<Long> userIds) throws SQLException {
        List<User> users = new ArrayList<>();
        if (userIds == null || userIds.isEmpty()) {
            return users; // Retourne une liste vide si aucun ID n'est fourni
        }

        // Création d'une chaîne de "?" pour la clause IN, ex: (?, ?, ?)
        String inClause = userIds.stream()
                                 .map(id -> "?")
                                 .collect(Collectors.joining(", "));

        String sql = "SELECT id, nom_utilisateur, est_admin FROM Utilisateurs WHERE id IN (" + inClause + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Assigner chaque ID à un placeholder "?" dans la requête
            int index = 1;
            for (Long id : userIds) {
                pstmt.setLong(index++, id);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Créer un objet User simple pour l'affichage dans la liste admin
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("nom_utilisateur"));
                    user.setAdmin(rs.getBoolean("est_admin"));
                    users.add(user);
                }
            }
        }
        return users;
    }
    public Client registerNewUserWithId(int userId, String username, String hashedPassword, String email, String phone) throws SQLException {
        String sql = "INSERT INTO Utilisateurs (id, nom_utilisateur, mot_de_passe, email, telephone, statut, est_admin) VALUES (?, ?, ?, ?, ?, 'actif', false)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, hashedPassword);
            
            if (email != null && !email.isEmpty()) {
                pstmt.setString(4, email);
            } else {
                pstmt.setNull(4, java.sql.Types.VARCHAR);
            }
            
            if (phone != null && !phone.isEmpty()) {
                pstmt.setString(5, phone);
            } else {
                pstmt.setNull(5, java.sql.Types.VARCHAR);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Création de l'objet client pour le retour
                return new Client(userId, username, email, phone, "actif", false, null, 0);
            }
        }
        return null;
    }
}