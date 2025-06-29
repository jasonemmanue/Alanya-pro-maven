package com.Alanya;

import com.Alanya.DAO.MessageDAO;
import com.Alanya.DAO.UserDAO;
import com.Alanya.model.AttachmentInfo;
import com.Alanya.services.ContactService;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientServer implements Runnable {
    private final String ownerUsername;
    private final int ownerUserId;
    private ServerSocket serverSocket;
    private int port;
    private volatile boolean running = false;
    private final Map<String, ClientHandler> connectedPeersToThisServer = new ConcurrentHashMap<>();
    private final Mainfirstclientcontroller uiController;
    private final ContactService contactService;
    private final UserDAO userDAO;

    public static final String SAVE_DIRECTORY = System.getProperty("user.home") + File.separator + "AlanyaChatFiles";

    public ClientServer(String ownerUsername, int ownerUserId, Mainfirstclientcontroller controller, ContactService contactService, UserDAO userDAO) {
        this.ownerUsername = ownerUsername;
        this.ownerUserId = ownerUserId;
        this.uiController = controller;
        this.contactService = contactService;
        this.userDAO = userDAO;
        this.port = findAvailablePort(10000, 20000);
        if (this.port == -1) {
            uiController.showAlert(AlertType.ERROR, "Erreur P2P", "Aucun port P2P disponible.");
            throw new RuntimeException("Aucun port P2P disponible.");
        }
        new File(SAVE_DIRECTORY).mkdirs();
    }
    
    @Override
    public void run() {
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Serveur P2P de " + ownerUsername + " démarré sur " + getHost() + ":" + port);
            while (running) {
                try {
                    Socket peerSocket = serverSocket.accept();
                    new Thread(new ClientHandler(peerSocket, this)).start();
                } catch (SocketException e) { if (!running) break; }
            }
        } catch (IOException e) { if(running) e.printStackTrace(); } finally { stop(); }
    }

    public class ClientHandler implements Runnable {
        private final Socket peerSocket;
        private final ClientServer parentP2PServer;
        private ObjectInputStream peerIn;
        private ObjectOutputStream peerOut;
        private String connectedPeerUsername;
        private volatile boolean peerAuthenticated = false;

        public ClientHandler(Socket socket, ClientServer server) {
            this.peerSocket = socket;
            this.parentP2PServer = server;
        }
        
        public void sendP2PObject(Serializable object) {
            if (peerAuthenticated && peerOut != null && !peerSocket.isClosed()) {
                try {
                    synchronized (peerOut) {
                        peerOut.writeObject(object);
                        peerOut.flush();
                        peerOut.reset();
                    }
                } catch (IOException e) {
                    System.err.println("ClientHandler: Erreur d'envoi P2P à " + connectedPeerUsername + ": " + e.getMessage());
                    closeConnection();
                }
            }
        }

        @Override
        public void run() {
            try {
                peerOut = new ObjectOutputStream(peerSocket.getOutputStream());
                peerOut.flush();
                peerIn = new ObjectInputStream(peerSocket.getInputStream());

                Object authObj = peerIn.readObject();
                if (authObj instanceof AuthMessage) {
                    this.connectedPeerUsername = ((AuthMessage) authObj).getUsername();
                    this.peerAuthenticated = true;
                    parentP2PServer.registerPeerToThisServer(this.connectedPeerUsername, this);
                    if (parentP2PServer.contactService != null) {
                        parentP2PServer.contactService.ensureContactExistsAndDisplay(parentP2PServer.ownerUserId, this.connectedPeerUsername);
                    }
                } else {
                    closeConnection();
                    return;
                }

                while (peerAuthenticated && !peerSocket.isClosed()) {
                    Object receivedObj = peerIn.readObject();
                    if (receivedObj instanceof Message) {
                        Message msg = (Message) receivedObj;
                        long messageDbId = msg.getDatabaseId();
                        if (messageDbId <= 0) continue;

                        // Sauvegarde de pièce jointe
                        if (msg.getAttachmentInfo() != null && msg.getAttachmentInfo().getFileData() != null) {
                           try {
                                AttachmentInfo attachment = msg.getAttachmentInfo();
                                String newFileName = messageDbId + "-" + attachment.getFileName();
                                Path savePath = Paths.get(SAVE_DIRECTORY, newFileName);
                                Files.write(savePath, attachment.getFileData());
                                attachment.setLocalPath(savePath.toAbsolutePath().toString());
                                attachment.setFileData(null);
                            } catch (IOException e) {
                                msg.setAttachmentInfo(null);
                            }
                        }

                        // === NOUVELLE LOGIQUE DE STATUT ===
                        // Par défaut, le nouveau statut est "Reçu"
                        int newStatus = 1; 
                        
                        // Vérifier si le chat avec l'expéditeur est déjà ouvert dans l'UI
                        if (uiController.isChattingWith(connectedPeerUsername)) {
                            newStatus = 2; // Si oui, marquer directement comme "Lu"
                        }

                        // Mettre à jour le statut dans la BDD
                        try {
                            new MessageDAO().updateMessageReadStatus(messageDbId, newStatus);
                            msg.setReadStatus(newStatus);
                        } catch (SQLException e) {
                             System.err.println("Erreur MAJ statut lecture: " + e.getMessage());
                        }

                        // Afficher le message dans l'interface
                        Platform.runLater(() -> uiController.displayMessage(msg, false));

                        // Envoyer l'accusé de réception (Reçu ou Lu)
                        MessageStatusUpdateMessage statusUpdate = new MessageStatusUpdateMessage(msg.getDatabaseId(), newStatus);
                        sendP2PObject(statusUpdate);
                        System.out.println("Accusé (statut " + newStatus + ") envoyé pour msg ID: " + msg.getDatabaseId());

                    } else if (receivedObj instanceof MessageStatusUpdateMessage) {
                        uiController.handleMessageStatusUpdate((MessageStatusUpdateMessage) receivedObj, connectedPeerUsername);
                    }
                }
            } catch (Exception e) {
                // Gérer déconnexion
            } finally {
                closeConnection();
                parentP2PServer.unregisterPeerFromThisServer(this.connectedPeerUsername);
            }
        }
        
        public void closeConnection() {
            peerAuthenticated = false;
            try { if (peerIn != null) peerIn.close(); } catch (IOException e) {}
            try { if (peerOut != null) peerOut.close(); } catch (IOException e) {}
            try { if (peerSocket != null && !peerSocket.isClosed()) peerSocket.close(); } catch (IOException e) {}
        }
    }

    public String getHost() { try { return InetAddress.getLocalHost().getHostAddress(); } catch (UnknownHostException e) { return "127.0.0.1"; } }
    public int getPort() { return port; }
    private int findAvailablePort(int minPort, int maxPort) { for (int p = minPort; p <= maxPort; p++) { try (ServerSocket ss = new ServerSocket(p)) { return p; } catch (IOException e) {} } return -1; }
    public void stop() { running = false; try { if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close(); } catch (IOException e) { } connectedPeersToThisServer.values().forEach(ClientHandler::closeConnection); connectedPeersToThisServer.clear(); }
    public void registerPeerToThisServer(String u, ClientHandler h) { connectedPeersToThisServer.put(u, h); uiController.peerConnected(u); }
    public void unregisterPeerFromThisServer(String u) { if (u != null) { connectedPeersToThisServer.remove(u); uiController.peerDisconnected(u); } }
}