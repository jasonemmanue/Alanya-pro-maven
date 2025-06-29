package com.Alanya; // Assurez-vous que le package est correct

import java.io.Serializable;

/**
 * Message envoyé par le possesseur du fichier au demandeur, contenant un morceau (chunk) du fichier.
 */
public class FileChunkMessage implements Serializable {
    private static final long serialVersionUID = 502L; // Gardez une UID cohérente

    private final String fileName;      // Nom du fichier auquel ce morceau appartient
    private final byte[] chunkData;     // Les données du morceau de fichier
    private final int chunkIndex;       // L'index de ce morceau (pour l'ordonnancement, optionnel si l'ordre est garanti par le flux)
    private final boolean isLastChunk;  // True si c'est le dernier morceau du fichier

    public FileChunkMessage(String fileName, byte[] chunkData, int chunkIndex, boolean isLastChunk) {
       this.fileName = fileName;
       this.chunkData = chunkData; // Le Mainfirstclientcontroller s'assure de copier le buffer
       this.chunkIndex = chunkIndex;
       this.isLastChunk = isLastChunk;
    }

    public String getFileName() { return fileName; }
    public byte[] getChunkData() { return chunkData; }
    public int getChunkIndex() { return chunkIndex; } // Peut ne pas être utilisé si l'ordre TCP est suffisant
    public boolean isLastChunk() { return isLastChunk; }

    @Override
    public String toString() {
        return "FileChunkMessage{" +
               "fileName='" + fileName + '\'' +
               ", chunkSize=" + (chunkData != null ? chunkData.length : 0) +
               ", index=" + chunkIndex +
               ", last=" + isLastChunk +
               '}';
    }
}
