package com.Alanya;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.Alanya.model.AttachmentInfo; // Assurez-vous que le package est correct

public class Message implements Serializable {
    private static final long serialVersionUID = 5L; // Incrémenté à cause de databaseId

    private final String sender;
    private final String receiver;
    private final String content;
    private final String timestamp;
    private AttachmentInfo attachmentInfo;
    private long databaseId; // Pour stocker l'ID du message depuis la BDD
    private int readStatus; // 0 = sent, 1 = delivered, 2 = read

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Message(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(formatter);
        this.attachmentInfo = null;
        this.readStatus = 0; // Message sortant est "envoyé"
    }
    

    public int getReadStatus() { return readStatus; }
    
    public void setReadStatus(int readStatus) { this.readStatus = readStatus; }

    public Message(String sender, String receiver, String content, String preformattedTimestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = preformattedTimestamp;
        this.attachmentInfo = null;
    }

    // Getters
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    public AttachmentInfo getAttachmentInfo() { return attachmentInfo; }
    public long getDatabaseId() { return databaseId; }

    // Setters
    public void setAttachmentInfo(AttachmentInfo attachmentInfo) {
        this.attachmentInfo = attachmentInfo;
    }
    public void setDatabaseId(long databaseId) { this.databaseId = databaseId; }


    @Override
    public String toString() {
        String str = "[" + timestamp + "] " + sender + " -> " + receiver + ": " + content;
        if (attachmentInfo != null) {
            str += " [Fichier: " + attachmentInfo.getFileName() + "]";
        }
        return str;
    }
}
