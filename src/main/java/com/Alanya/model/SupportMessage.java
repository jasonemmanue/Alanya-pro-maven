package com.Alanya.model;

import java.io.Serializable; // <-- IMPORT OBLIGATOIRE
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// La classe et son énumération doivent être sérialisables
public class SupportMessage implements Serializable {

    // Bonne pratique : ajouter un identifiant de version pour la sérialisation
    private static final long serialVersionUID = 2L;

    private long id;
    private long userId;
    private long adminId;
    private String content;
    private SenderType senderType; // Cette énumération doit être sérialisable
    private String timestamp;
    private boolean isRead;

    // L'énumération est automatiquement sérialisable si elle est statique ou dans son propre fichier.
    // La définir ici est simple et fonctionne bien.
    public enum SenderType { USER, ADMIN }

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SupportMessage(long userId, long adminId, String content, SenderType senderType) {
        this.userId = userId;
        this.adminId = adminId;
        this.content = content;
        this.senderType = senderType;
        this.timestamp = LocalDateTime.now().format(formatter);
        this.isRead = false;
    }

    // Getters et Setters (inchangés)
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public String getContent() { return content; }
    public SenderType getSenderType() { return senderType; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public long getAdminId() { return adminId; }
}