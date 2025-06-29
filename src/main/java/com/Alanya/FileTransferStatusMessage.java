package com.Alanya; // Assurez-vous que le package est correct

import java.io.Serializable;

/**
 * Message pour communiquer le statut d'un transfert de fichier,
 * par exemple, si l'expéditeur ne trouve pas le fichier.
 */
public class FileTransferStatusMessage implements Serializable {
    private static final long serialVersionUID = 503L;

    private final String fileName;
    private final String status; // Ex: "ERROR_FILE_NOT_FOUND", "TRANSFER_STARTED", "TRANSFER_ABORTED_BY_SENDER"
    private final String details; // Optionnel: plus d'informations sur le statut

    public FileTransferStatusMessage(String fileName, String status) {
        this(fileName, status, null);
    }

    public FileTransferStatusMessage(String fileName, String status, String details) {
        this.fileName = fileName;
        this.status = status;
        this.details = details;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStatus() {
        return status;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "FileTransferStatusMessage{" +
               "fileName='" + fileName + '\'' +
               ", status='" + status + '\'' +
               (details != null ? ", details='" + details + '\'' : "") +
               '}';
    }
}
