// Fichier : ClientserverAlanya/src/com/Alanya/CentralServerConnection.java
package com.Alanya;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;

public class CentralServerConnection {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Mainfirstclientcontroller controller;
    private volatile boolean connected = false;
    private Thread listenerThread;

    public CentralServerConnection(Mainfirstclientcontroller controller) {
        this.controller = controller;
    }

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            listenerThread = new Thread(this::listenToServer);
            listenerThread.setDaemon(true);
            listenerThread.start();
            return true;
        } catch (IOException e) {
            System.err.println("Impossible de se connecter au serveur central: " + e.getMessage());
            controller.updateStatus("Connexion au serveur central échouée.");
            return false;
        }
    }

    private void listenToServer() {
        try {
            while (connected && !socket.isClosed()) {
                Object responseObject = in.readObject();
                if (responseObject instanceof ServerResponse) {
                    handleServerResponse((ServerResponse) responseObject);
                }
            }
        } catch (EOFException | SocketException e) {
            handleDisconnectCleanup("Déconnecté du serveur central.");
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {
                e.printStackTrace();
                handleDisconnectCleanup("Erreur de communication avec le serveur.");
            }
        }
    }

    private void handleServerResponse(ServerResponse response) {
        ServerResponseType type = response.getType();
        Map<String, String> data = response.getData();

        if (type == ServerResponseType.NEW_INCOMING_CALL) {
            Platform.runLater(() -> controller.handleIncomingCall(data.get("callerUsername"), data.get("callId"), data.get("type")));
            return;
        } 
        
        else if (type == ServerResponseType.CALL_ACCEPTED_BY_PEER) {
            try {
                String callType = data.get("type");
                if ("video".equals(callType)) {
                    int peerAudioPort = Integer.parseInt(data.get("audioPort"));
                    int peerVideoPort = Integer.parseInt(data.get("videoPort"));
                    Platform.runLater(() -> controller.handleCallAcceptedByPeerVideo(data.get("responderUsername"), data.get("callId"), data.get("ip"), peerAudioPort, peerVideoPort));
                } else { // Appel audio
                    int peerP2PPort = Integer.parseInt(data.get("port"));
                    Platform.runLater(() -> controller.handleCallAcceptedByPeer(data.get("responderUsername"), data.get("callId"), data.get("ip"), peerP2PPort, callType));
                }
            } catch (NumberFormatException e) { 
                System.err.println("Port(s) P2P invalide(s) reçu(s) du serveur."); 
            }
            return;
        } 
        else if (type == ServerResponseType.CALL_REJECTED_BY_PEER) {
            Platform.runLater(() -> controller.handleCallRejectedByPeer(data.get("responderUsername"), data.get("callId"), data.get("type")));
            return;
        } 
        else if (type == ServerResponseType.CALL_ENDED_BY_PEER) {
            Platform.runLater(() -> controller.handleCallEndedByPeer(data.get("username"), data.get("callId"), data.get("type")));
            return;
        }
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                // Cas d'une réponse de statut de contact (GET_PEER_INFO)
                if (type == ServerResponseType.P2P_PEER_INFO) {
                    String peerUsername = data.get("username");
                    String host = data.get("host");
                    try {
                        int port = Integer.parseInt(data.get("port"));
                        if (peerUsername != null && port > 0) {
                            // Le peer est en ligne, on met à jour son statut silencieusement
                            controller.updatePeerInfo(peerUsername, host, port, true);
                        }
                    } catch (Exception e) {
                        System.err.println("Réponse P2P_PEER_INFO invalide pour " + peerUsername);
                    }
                } 
                // Cas d'une réponse à l'authentification (on veut afficher ce message)
                else if (type == ServerResponseType.AUTHENTICATION_SUCCESS) {
                    int userId = Integer.parseInt(data.get("id"));
                    controller.handleCentralServerAuthenticated(data.get("username"), userId);
                    controller.updateStatus("Authentification réussie. Bienvenue !");
                } 
                // Autres messages de succès importants
                else if (type == ServerResponseType.P2P_SERVER_REGISTERED) {
                    controller.updateStatus("Connecté et visible par les autres utilisateurs.");
                }

            } else { // Si response.isSuccess() est false
                // Gérer les cas d'échec
                if (type == ServerResponseType.P2P_PEER_INFO && response.getMessage().contains("Peer non joignable")) {
                    // C'est la réponse qui causait le spam. On la traite silencieusement.
                    String peerUsername = data.get("username");
                    if (peerUsername != null) {
                        // On met juste à jour le statut à "hors ligne" dans le modèle de l'UI
                        controller.updatePeerInfo(peerUsername, null, 0, false);
                    }
                    // ON N'AFFICHE RIEN DANS LA BARRE DE STATUT ! C'est la correction clé.
                } else {
                    // Pour toutes les autres erreurs réelles, on affiche une alerte
                    controller.showAlert(AlertType.ERROR, "Erreur Serveur", response.getMessage());
                }
            }
        });
    }

    public boolean sendCommand(ServerCommand command) {
        if (out != null && connected && !socket.isClosed()) {
            try {
                out.writeObject(command);
                out.flush();
                return true;
            } catch (IOException e) {
                handleDisconnectCleanup("Erreur d'envoi de commande.");
                return false;
            }
        }
        return false;
    }

    public void notifyClientServerStarted(String host, int port) {
        Map<String, String> data = Map.of("host", host, "port", String.valueOf(port));
        sendCommand(new ServerCommand(ServerCommand.ServerCommandType.CLIENT_SERVER_STARTED, data));
    }

    public void requestPeerInfo(String targetUsername) {
        Map<String, String> data = Map.of("targetUsername", targetUsername);
        sendCommand(new ServerCommand(ServerCommand.ServerCommandType.GET_PEER_INFO, data));
    }

    public void disconnect() {
        if (connected) {
            sendCommand(new ServerCommand(ServerCommand.ServerCommandType.DISCONNECT));
        }
        handleDisconnectCleanup("Déconnexion manuelle.");
    }

    private void handleDisconnectCleanup(String reason) {
        if (!connected) return;
        connected = false;
        try {
            if (listenerThread != null) listenerThread.interrupt();
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            // Ignorer, on essaie juste de nettoyer
        }
        Platform.runLater(() -> controller.updateStatus(reason));
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}