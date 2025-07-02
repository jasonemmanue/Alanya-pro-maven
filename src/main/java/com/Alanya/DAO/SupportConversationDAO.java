package com.Alanya.DAO;

import com.Alanya.DatabaseConnection;
import com.Alanya.model.SupportMessage;
import com.Alanya.model.SupportMessage.SenderType;
import com.Alanya.services.EncryptionService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupportConversationDAO {

    public void insertMessage(SupportMessage message) throws SQLException {
        String sql = "INSERT INTO SupportConversations (user_id, admin_id, message_content, sender_type, timestamp, is_read) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, message.getUserId());
            pstmt.setLong(2, message.getAdminId());
            pstmt.setString(3, EncryptionService.encrypt(message.getContent()));
            pstmt.setString(4, message.getSenderType().name());
            pstmt.setTimestamp(5, Timestamp.valueOf(message.getTimestamp()));
            pstmt.setBoolean(6, message.isRead());
            
            pstmt.executeUpdate();
            try(ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if(generatedKeys.next()) {
                    message.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    
    public List<Long> getDistinctUserIdsInSupport() throws SQLException {
        List<Long> userIds = new ArrayList<>();
        // La requête sélectionne uniquement les user_id uniques pour éviter les doublons.
        String sql = "SELECT DISTINCT user_id FROM SupportConversations";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                userIds.add(rs.getLong("user_id"));
            }
        }
        return userIds;
    }
    public List<SupportMessage> getConversation(long userId, long adminId) throws SQLException {
        List<SupportMessage> conversation = new ArrayList<>();
        String sql = "SELECT * FROM SupportConversations WHERE user_id = ? AND admin_id = ? ORDER BY timestamp ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.setLong(2, adminId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                SupportMessage msg = new SupportMessage(
                    rs.getLong("user_id"),
                    rs.getLong("admin_id"),
                    EncryptionService.decrypt(rs.getString("message_content")),
                    SenderType.valueOf(rs.getString("sender_type"))
                );
                msg.setId(rs.getLong("id"));
                msg.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime().format(SupportMessage.formatter));
                msg.setRead(rs.getBoolean("is_read"));
                conversation.add(msg);
            }
        }
        return conversation;
    }
}