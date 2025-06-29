package com.Alanya;

import java.io.Serializable;

public class MessageStatusUpdateMessage implements Serializable {
    private static final long serialVersionUID = 600L;

    private final long messageDatabaseId;
    private final int newStatus; // 1 pour "reçu", 2 pour "lu"

    public MessageStatusUpdateMessage(long messageDatabaseId, int newStatus) {
        this.messageDatabaseId = messageDatabaseId;
        this.newStatus = newStatus;
    }

    public long getMessageDatabaseId() {
        return messageDatabaseId;
    }

    public int getNewStatus() {
        return newStatus;
    }
}