// Fichier : ClientserverAlanya/src/com/Alanya/model/AttachmentInfo.java
package com.Alanya.model;

import java.io.Serializable;

public class AttachmentInfo implements Serializable {
    private static final long serialVersionUID = 3L; // Version modifiée pour la nouvelle logique

    private String fileName;
    private String fileType;
    private long fileSize;
    
    // CORRECTION MAJEURE : Ce champ contiendra les données du fichier pendant le transfert
    private byte[] fileData; 
    
    // Ce champ contiendra le chemin une fois le fichier sauvegardé chez le destinataire
    private String localPath; 

    public AttachmentInfo(String fileName, String fileType, long fileSize, String senderLocalPath) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.localPath = senderLocalPath; // Utilisé par l'expéditeur
        this.fileData = null;
    }
    
    // Getters et Setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }

    @Override
    public String toString() {
        return "AttachmentInfo{" +
               "fileName='" + fileName + '\'' +
               ", fileSize=" + fileSize +
               (localPath != null ? ", localPath='" + localPath + '\'' : "") +
               '}';
    }
}