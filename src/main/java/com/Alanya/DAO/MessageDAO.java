package com.Alanya.DAO;

import com.Alanya.Message;
import com.Alanya.DatabaseConnection;
import com.Alanya.model.AttachmentInfo;
import com.Alanya.services.EncryptionService;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageDAO {

    public long saveMessage(Message message, int senderId, int receverid) throws SQLException {
        String sql = "INSERT INTO Messages (senderid, receverid, contenu, date_envoi, lu, nom_fichier, type_fichier, taille_fichier, chemin_local_fichier, statut_lecture) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        long messageId = -1;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receverid);

            // --- MODIFICATION : Le contenu est maintenant chiffré avant sauvegarde ---
            pstmt.setString(3, EncryptionService.encrypt(message.getContent()));

            try {
                pstmt.setTimestamp(4, Timestamp.valueOf(message.getTimestamp()));
            } catch (Exception e) {
                pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            }

            pstmt.setBoolean(5, false); // lu
            pstmt.setInt(10, message.getReadStatus()); // statut_lecture

            AttachmentInfo attachment = message.getAttachmentInfo();
            if (attachment != null) {
                pstmt.setString(6, attachment.getFileName());
                pstmt.setString(7, attachment.getFileType());
                pstmt.setLong(8, attachment.getFileSize());
                pstmt.setString(9, attachment.getLocalPath()); // Chemin local de l'expéditeur
            } else {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.BIGINT);
                pstmt.setNull(9, Types.VARCHAR);
            }

            if (pstmt.executeUpdate() > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        messageId = generatedKeys.getLong(1);
                        message.setDatabaseId(messageId);
                    }
                }
            }
        }
        return messageId;
    }

    public void updateMessageReadStatus(long messageId, int newStatus) throws SQLException {
        boolean isNowReadForOldLuColumn = (newStatus == 2);
        String sql = "UPDATE Messages SET statut_lecture = ?, lu = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newStatus);
            pstmt.setBoolean(2, isNowReadForOldLuColumn);
            pstmt.setLong(3, messageId);
            pstmt.executeUpdate();
        }
    }
    
	public List<Message> getMessageHistory(int userId1, int userId2) throws SQLException {
        List<Message> history = new ArrayList<>();
        String sql = "SELECT M.id as message_id, M.senderid, M.receverid, M.contenu, M.date_envoi, M.lu, M.statut_lecture, " +
                     "U1.nom_utilisateur AS sender_name, U2.nom_utilisateur AS receiver_name, " +
                     "M.nom_fichier, M.type_fichier, M.taille_fichier, M.chemin_local_fichier " +
                     "FROM Messages M " +
                     "JOIN Utilisateurs U1 ON M.senderid = U1.id " +
                     "JOIN Utilisateurs U2 ON M.receverid = U2.id " +
                     "WHERE (M.senderid = ? AND M.receverid = ?) OR (M.senderid = ? AND M.receverid = ?) " +
                     "ORDER BY M.date_envoi ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId1);
            pstmt.setInt(2, userId2);
            pstmt.setInt(3, userId2);
            pstmt.setInt(4, userId1);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // --- MODIFICATION : Le contenu est déchiffré après récupération ---
                    String decryptedContent = EncryptionService.decrypt(rs.getString("contenu"));

                    Message msg = new Message(
                        rs.getString("sender_name"),
                        rs.getString("receiver_name"),
                        decryptedContent, // Utilisation du contenu déchiffré
                        rs.getTimestamp("date_envoi").toLocalDateTime().format(Message.formatter)
                    );
                    msg.setDatabaseId(rs.getLong("message_id"));
                    msg.setReadStatus(rs.getInt("statut_lecture"));

                    String nomFichier = rs.getString("nom_fichier");
                    if (nomFichier != null) {
                        AttachmentInfo attachment = new AttachmentInfo(
                            nomFichier,
                            rs.getString("type_fichier"),
                            rs.getLong("taille_fichier"),
                            rs.getString("chemin_local_fichier")
                        );
                        msg.setAttachmentInfo(attachment);
                    }
                    history.add(msg);
                }
            }
        }
        return history;
    }

    public Map<Integer, Integer> getUnreadMessageCountsPerSender(int currentUserId) throws SQLException {
        Map<Integer, Integer> counts = new HashMap<>();
        String sql = "SELECT senderid, COUNT(*) as unread_count FROM Messages WHERE receverid = ? AND statut_lecture < 2 GROUP BY senderid";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getInt("senderid"), rs.getInt("unread_count"));
                }
            }
        }
        return counts;
    }

    public void markAllMessagesFromContactAsRead(int senderId, int receiverId, int newStatus) throws SQLException {
        boolean isNowReadForOldLuColumn = (newStatus == 2);
        String sql = "UPDATE Messages SET statut_lecture = ?, lu = ? WHERE senderid = ? AND receverid = ? AND statut_lecture < ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newStatus);
            pstmt.setBoolean(2, isNowReadForOldLuColumn);
            pstmt.setInt(3, senderId);
            pstmt.setInt(4, receiverId);
            pstmt.setInt(5, newStatus);
            pstmt.executeUpdate();
        }
    }
    
    public List<Message> getOfflineMessagesForUser(int userId) throws SQLException {
        List<Message> offlineMessages = new ArrayList<>();
        String sql = "SELECT M.id as message_id, M.contenu, M.date_envoi, M.lu, M.statut_lecture, " +
                     "U1.nom_utilisateur AS sender_name, U2.nom_utilisateur AS receiver_name, " +
                     "M.nom_fichier, M.type_fichier, M.taille_fichier, M.chemin_local_fichier " +
                     "FROM Messages M " +
                     "JOIN Utilisateurs U1 ON M.senderid = U1.id " +
                     "JOIN Utilisateurs U2 ON M.receverid = U2.id " +
                     "WHERE M.receverid = ? AND M.statut_lecture = 0 " +
                     "ORDER BY M.date_envoi ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // --- MODIFICATION : Le contenu est déchiffré après récupération ---
                String decryptedContent = EncryptionService.decrypt(rs.getString("contenu"));

                 Message msg = new Message(
                    rs.getString("sender_name"),
                    rs.getString("receiver_name"),
                    decryptedContent, // Utilisation du contenu déchiffré
                    rs.getTimestamp("date_envoi").toLocalDateTime().format(Message.formatter)
                );
                msg.setDatabaseId(rs.getLong("message_id"));
                msg.setReadStatus(rs.getInt("statut_lecture"));

                String nomFichier = rs.getString("nom_fichier");
                if (nomFichier != null) {
                    msg.setAttachmentInfo(new AttachmentInfo(nomFichier, rs.getString("type_fichier"), rs.getLong("taille_fichier"), rs.getString("chemin_local_fichier")));
                }
                offlineMessages.add(msg);
            }
        }
        return offlineMessages;
    }
    
    public void updateMessagesStatusToDelivered(List<Long> messageIds) throws SQLException {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }
        
        // Utilisation de StringBuilder pour construire la requête dynamiquement de manière plus sûre
        StringBuilder sqlBuilder = new StringBuilder("UPDATE Messages SET statut_lecture = 1 WHERE id IN (");
        for (int i = 0; i < messageIds.size(); i++) {
            sqlBuilder.append("?");
            if (i < messageIds.size() - 1) {
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.append(")");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < messageIds.size(); i++) {
                pstmt.setLong(i + 1, messageIds.get(i));
            }
            pstmt.executeUpdate();
        }
    }
}