package com.Alanya; // Assurez-vous que le package est correct

import java.io.Serializable;

/**
 * Message envoyé par un client (demandeur) à un autre client (possesseur du fichier)
 * pour demander le téléchargement d'un fichier spécifique.
 */
public class FileRequestMessage implements Serializable {
    private static final long serialVersionUID = 501L; // Gardez une UID cohérente ou incrémentez si la structure change

    private final String fileNameToGet;        // Le nom du fichier que le demandeur souhaite obtenir
    private final String senderOriginalFilePath; // Le chemin ABSOLU où le fichier était stocké chez l'expéditeur original du message contenant la pièce jointe
    private final String requesterUsername;      // Le nom d'utilisateur de celui qui demande le fichier
    private final String originalSenderOfFile;   // Le nom d'utilisateur de celui qui a initialement envoyé le message avec ce fichier (et qui est supposé l'avoir)

    public FileRequestMessage(String fileNameToGet, String senderOriginalFilePath, String requesterUsername, String originalSenderOfFile) {
        this.fileNameToGet = fileNameToGet;
        this.senderOriginalFilePath = senderOriginalFilePath;
        this.requesterUsername = requesterUsername;
        this.originalSenderOfFile = originalSenderOfFile;
    }

    public String getFileNameToGet() {
        return fileNameToGet;
    }

    public String getSenderOriginalFilePath() {
        return senderOriginalFilePath;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public String getOriginalSenderOfFile() {
        return originalSenderOfFile;
    }

    @Override
    public String toString() {
        return "FileRequestMessage{" +
               "fileNameToGet='" + fileNameToGet + '\'' +
               ", senderOriginalFilePath='" + senderOriginalFilePath + '\'' +
               ", requesterUsername='" + requesterUsername + '\'' +
               ", originalSenderOfFile='" + originalSenderOfFile + '\'' +
               '}';
    }
}
