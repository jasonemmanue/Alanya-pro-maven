package com.Alanya;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.Alanya.DAO.UserDAO; // Import ajouté
import com.Alanya.ServerCommand.ServerCommandType; // Import ajouté

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AlanyaCentralServer extends Application {

    private static final int SERVER_PORT = 9000;
    private static final Map<String, ClientInfo> connectedClientsP2PInfo = new ConcurrentHashMap<>();
    
    private static final Map<String, ClientHandler> activeClientHandlers = new ConcurrentHashMap<>();


    private ServerController controller;
    private ServerThread serverThread;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/Alanya/ServerView.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        controller.setServer(this);

        primaryStage.setTitle("Alanya Central Server");
        try {
            URL iconUrl = getClass().getResource("/com/Alanya/imgA.jpg");
            if (iconUrl != null) {
                Image icon = new Image(iconUrl.toExternalForm());
                primaryStage.getIcons().add(icon);
            } else {
                System.err.println("Image d'icône non trouvée: imgA.jpg");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image d'icône: " + e.getMessage());
        }

        Scene scene = new Scene(root, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);

        primaryStage.setOnCloseRequest(event -> {
            stopServer();
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    public void startServer() {
        if (serverThread != null && serverThread.isAlive()) {
            controller.logMessage("Le serveur est déjà en cours d'exécution");
            return;
        }

        serverThread = new ServerThread(SERVER_PORT, controller, this);
        serverThread.setDaemon(true); 
        serverThread.start();
        controller.setServerStatus(true);
    }

    public void stopServer() {
        controller.logMessage("Arrêt du serveur demandé...");
        if (serverThread != null) {
            serverThread.stopRunning(); 
        }
    }


    public List<String> getConnectedClientsList() {
        List<String> clientDetails = new ArrayList<>();
        for(ClientInfo ci : connectedClientsP2PInfo.values()){
            clientDetails.add(ci.toString());
        }
        return clientDetails;
    }

    public static void addP2PClient(String username, ClientInfo clientInfo) {
        connectedClientsP2PInfo.put(username, clientInfo);
        System.out.println("Client P2P ajouté/mis à jour: " + username + " -> " + clientInfo.getHost() + ":" + clientInfo.getPort());
    }

    public static void removeP2PClient(String username) {
        if (connectedClientsP2PInfo.remove(username) != null) {
            System.out.println("Client P2P retiré de connectedClientsP2PInfo: " + username);
        }
    }
    
    public static void addActiveHandler(String username, ClientHandler handler) {
        activeClientHandlers.put(username, handler);
        System.out.println("Handler actif ajouté pour: " + username);
    }

    public static void removeActiveHandler(String username) {
        if (activeClientHandlers.remove(username) != null) {
            System.out.println("Handler actif retiré pour: " + username);
        }
    }

    public static ClientHandler getClientHandler(String username) {
        return activeClientHandlers.get(username);
    }


    public static ClientInfo getP2PClientInfo(String username) {
        return connectedClientsP2PInfo.get(username);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class ServerThread extends Thread {
        private final int port;
        private final ServerController controller;
        private volatile boolean running = true;
        private ServerSocket serverSocket;
        private final AlanyaCentralServer alanyaServerInstance;

        public ServerThread(int port, ServerController controller, AlanyaCentralServer alanyaServerInstance) {
            this.port = port;
            this.controller = controller;
            this.alanyaServerInstance = alanyaServerInstance;
        }

        public void stopRunning() {
            running = false;
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                controller.logMessage("Erreur lors de la tentative de fermeture du ServerSocket: " + e.getMessage());
            }
            List<String> usernames = new ArrayList<>(activeClientHandlers.keySet());
            for (String username : usernames) {
                ClientHandler handler = activeClientHandlers.get(username);
                if (handler != null) {
                    handler.sendServerShutdownNotification("Le serveur central est en train de s'arrêter.");
                    handler.stopHandlerRunning();
                    handler.closeClientConnection();
                }
            }
            activeClientHandlers.clear();
            connectedClientsP2PInfo.clear();
            
            Platform.runLater(() -> {
                controller.setServerStatus(false);
                controller.logMessage("Serveur arrêté et toutes les connexions client fermées.");
                controller.updateClientList();
            });
        }


        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                controller.logMessage("Serveur démarré. En attente de connexions sur le port " + port + "...");
                
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        controller.logMessage("Nouvelle connexion acceptée de: " + clientSocket.getInetAddress().getHostAddress());
                        ClientHandler handler = new ClientHandler(clientSocket, controller, alanyaServerInstance);
                        new Thread(handler).start();
                    } catch (SocketException e) {
                        if (!running) {
                            controller.logMessage("ServerSocket fermé. Arrêt du thread serveur.");
                        } else {
                            controller.logMessage("Erreur de Socket lors de l'attente de connexion: " + e.getMessage());
                        }
                    } catch (IOException e) {
                        if (running) {
                            controller.logMessage("Erreur d'E/S lors de l'acceptation de la connexion: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                if (running) {
                    controller.logMessage("Impossible de démarrer le serveur sur le port " + port + ": " + e.getMessage());
                    Platform.runLater(() -> controller.setServerStatus(false));
                }
            } finally {
                try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    controller.logMessage("Erreur lors de la fermeture finale du ServerSocket: " + e.getMessage());
                }
                controller.logMessage("Thread serveur terminé.");
            }
        }
    }

    public static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final ServerController controller;
        private String authenticatedUsername;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private boolean isAuthenticated = false;
        private volatile boolean running = true;
        private final AlanyaCentralServer alanyaServerInstance;

        public ClientHandler(Socket socket, ServerController controller, AlanyaCentralServer alanyaServerInstance) {
            this.clientSocket = socket;
            this.controller = controller;
            this.alanyaServerInstance = alanyaServerInstance;
        }
        
        public void stopHandlerRunning() {
            this.running = false;
        }

        public void closeClientConnection() {
            running = false;
            try { if (out != null) out.close(); } catch (IOException e) { }
            try { if (in != null) in.close(); } catch (IOException e) { }
            try { if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close(); } catch (IOException e) { }
            System.out.println("Connexion fermée pour " + (authenticatedUsername != null ? authenticatedUsername : "client non authentifié"));
        }
        
        public void sendServerShutdownNotification(String message) {
            if (out != null && !clientSocket.isClosed()) {
                try {
                    Map<String, String> data = new HashMap<>();
                    data.put("reason", message);
                    ServerResponse response = new ServerResponse(false, message, data, ServerResponseType.SERVER_SHUTDOWN);
                    out.writeObject(response);
                    out.flush();
                } catch (IOException e) {
                    System.err.println("Erreur envoi notification d'arrêt serveur à " + authenticatedUsername + ": " + e.getMessage());
                }
            }
        }


        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush(); 
                in = new ObjectInputStream(clientSocket.getInputStream());
                controller.logMessage("Flux établis pour le client: " + clientSocket.getInetAddress().getHostAddress());

                while (running && !clientSocket.isClosed()) {
                    Object receivedObject = in.readObject();

                    if (receivedObject instanceof ServerCommand) {
                        ServerCommand command = (ServerCommand) receivedObject;
                        processCommand(command);
                    } else {
                        sendResponse(false, "Type de commande non reconnu.", null, ServerResponseType.GENERIC_ERROR);
                    }
                }
            } catch (EOFException | SocketException e) {
                // Déconnexion normale
            } catch (Exception e) {
                if (running) {
                    e.printStackTrace();
                }
            } finally {
                handleDisconnect();
                closeClientConnection();
            }
        }
        
        private void handleDisconnect() {
            if (isAuthenticated && authenticatedUsername != null) {
                updateClientStatusInDatabase(authenticatedUsername, "hors-ligne");
                
                AlanyaCentralServer.removeP2PClient(authenticatedUsername);
                AlanyaCentralServer.removeActiveHandler(authenticatedUsername);

                isAuthenticated = false;
                controller.logMessage("Utilisateur " + authenticatedUsername + " déconnecté et statut mis à jour.");
                Platform.runLater(() -> controller.updateClientList());
            }
        }
        
        private void processCommand(ServerCommand command) throws IOException, SQLException {
            if (!isAuthenticated && command.getType() != ServerCommandType.AUTHENTICATE && command.getType() != ServerCommandType.REGISTER) {
                sendResponse(false, "Vous devez d'abord vous authentifier.", null, ServerResponseType.AUTHENTICATION_FAILED);
                return;
            }
            controller.logMessage("Commande reçue de " + (authenticatedUsername != null ? authenticatedUsername : clientSocket.getInetAddress().getHostAddress()) + ": " + command.getType());

            switch (command.getType()) {
                case AUTHENTICATE:
                    handleAuthentication(command);
                    break;
                case REGISTER: // CAS AJOUTÉ
                    handleRegistration(command);
                    break;
                case DISCONNECT:
                    running = false;
                    break;
                case CLIENT_SERVER_STARTED:
                    handleClientServerStarted(command);
                    break;
                case GET_PEER_INFO:
                    handleGetPeerInfo(command);
                    break;
                case INITIATE_AUDIO_CALL:
                case INITIATE_VIDEO_CALL:
                    handleInitiateCall(command);
                    break;
                case CALL_RESPONSE:
                    handleCallResponse(command);
                    break;
                case END_CALL:
                    handleEndCall(command);
                    break;
                default:
                    sendResponse(false, "Commande non prise en charge.", null, ServerResponseType.GENERIC_ERROR);
            }
        }

        /**
         * **NOUVELLE MÉTHODE** - Gère l'inscription d'un nouvel utilisateur.
         */
        private void handleRegistration(ServerCommand command) throws IOException, SQLException {
            Map<String, String> data = command.getData();
            String username = data.get("username");
            String password = data.get("password");
            String email = data.get("email");
            String phone = data.get("phone");

            if (username == null || password == null || (email == null && phone == null)) {
                sendResponse(false, "Données d'inscription incomplètes.", null, ServerResponseType.GENERIC_ERROR);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                UserDAO userDAO = new UserDAO();

                if (userDAO.findUserByUsername(username) != null) {
                    sendResponse(false, "Ce nom d'utilisateur est déjà pris.", null, ServerResponseType.GENERIC_ERROR);
                    return;
                }
                if (email != null && !email.isEmpty() && userDAO.findUserByEmail(email) != null) {
                    sendResponse(false, "Cet email est déjà utilisé.", null, ServerResponseType.GENERIC_ERROR);
                    return;
                }

                int newUserId;
                boolean idExists;
                java.util.Random rand = new java.util.Random();
                
                do {
                    newUserId = 100000000 + rand.nextInt(900000000);
                    idExists = userDAO.userIdExists(newUserId);
                } while (idExists);

                String hashedPassword = Client.hashMotDePasse(password);
                Client registeredClient = userDAO.registerNewUserWithId(newUserId, username, hashedPassword, email, phone);

                if (registeredClient != null) {
                    controller.logMessage("Nouvel utilisateur enregistré: " + username + " avec UIA: " + newUserId);
                    sendResponse(true, "Inscription réussie !", null, ServerResponseType.GENERIC_SUCCESS);
                } else {
                    sendResponse(false, "L'inscription a échoué côté serveur.", null, ServerResponseType.GENERIC_ERROR);
                }
            }
        }

        private void handleAuthentication(ServerCommand command) throws IOException, SQLException {
            Map<String, String> data = command.getData();
            String identifier = data.get("identifier");
            String password = data.get("password");

            if (identifier == null || password == null) {
                sendResponse(false, "Identifiant ou mot de passe manquant.", null, ServerResponseType.AUTHENTICATION_FAILED);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                ResultatOperation result = Client.connexion(conn, identifier, password); 

                if (result.estSucces()) {
                    Client client = (Client) result.getDonnees();
                    
                    if (AlanyaCentralServer.getClientHandler(client.getNomUtilisateur()) != null) {
                        sendResponse(false, "Ce compte est déjà connecté.", null, ServerResponseType.USER_ALREADY_CONNECTED);
                        running = false;
                        return; 
                    }
                    
                    this.authenticatedUsername = client.getNomUtilisateur();
                    this.isAuthenticated = true;
                    AlanyaCentralServer.addActiveHandler(this.authenticatedUsername, this);
                    updateClientStatusInDatabase(this.authenticatedUsername, "actif");

                    Map<String, String> responseData = new HashMap<>();
                    responseData.put("username", authenticatedUsername);
                    responseData.put("id", String.valueOf(client.getId())); 
                    sendResponse(true, "Authentification réussie.", responseData, ServerResponseType.AUTHENTICATION_SUCCESS);
                    Platform.runLater(() -> controller.updateClientList());
                } else {
                    sendResponse(false, result.getMessage(), null, ServerResponseType.AUTHENTICATION_FAILED);
                }
            }
        }
        
        private void handleClientServerStarted(ServerCommand command) throws IOException, SQLException {
            if (!isAuthenticated) return;
            Map<String, String> data = command.getData();
            String host = data.get("host");
            int port = Integer.parseInt(data.get("port"));

            ClientInfo clientInfo = new ClientInfo(authenticatedUsername, host, port);
            AlanyaCentralServer.addP2PClient(authenticatedUsername, clientInfo); 
            updateClientP2PServerInfoInDB(authenticatedUsername, host, port); 

            sendResponse(true, "Serveur P2P enregistré.", null, ServerResponseType.P2P_SERVER_REGISTERED);
            Platform.runLater(() -> controller.updateClientList());
        }

        private void handleGetPeerInfo(ServerCommand command) throws IOException, SQLException {
             if (!isAuthenticated) return;
            String targetUsername = command.getData().get("targetUsername");
            ClientInfo peerInfo = AlanyaCentralServer.getP2PClientInfo(targetUsername); 
            Map<String, String> responseData = new HashMap<>();
            
            if (peerInfo != null && peerInfo.getPort() > 0) {
                responseData.put("username", peerInfo.getUsername());
                responseData.put("host", peerInfo.getHost());
                responseData.put("port", String.valueOf(peerInfo.getPort()));
                sendResponse(true, "Informations peer trouvées.", responseData, ServerResponseType.P2P_PEER_INFO);
            } else {
                try(Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement("SELECT statut FROM Utilisateurs WHERE nom_utilisateur = ?")) {
                    pstmt.setString(1, targetUsername);
                    ResultSet rs = pstmt.executeQuery();
                    String status = rs.next() ? rs.getString("statut") : "inconnu";
                    sendResponse(false, "Peer non joignable (statut: "+status+").", responseData, ServerResponseType.P2P_PEER_INFO);
                }
            }
        }
        
        private void handleInitiateCall(ServerCommand command) throws IOException {
             if (!isAuthenticated) return;
            String targetUsername = command.getData().get("targetUsername");
            String callId = command.getData().get("callId");
            String callType = command.getType() == ServerCommandType.INITIATE_AUDIO_CALL ? "audio" : "video";

            ClientHandler targetHandler = AlanyaCentralServer.getClientHandler(targetUsername);
            if (targetHandler != null && targetHandler.isAuthenticated) {
                Map<String, String> forwardData = Map.of("callerUsername", authenticatedUsername, "callId", callId, "type", callType);
                targetHandler.sendResponse(true, "Nouvel appel entrant", forwardData, ServerResponseType.NEW_INCOMING_CALL);
            } else {
                sendResponse(false, "L'utilisateur " + targetUsername + " n'est pas joignable.", null, ServerResponseType.CALL_REJECTED_BY_PEER);
            }
        }

        private void handleCallResponse(ServerCommand command) throws IOException {
             if (!isAuthenticated) return;
            Map<String, String> data = command.getData();
            String callId = data.get("callId");
            boolean accepted = Boolean.parseBoolean(data.get("accepted"));
            String initiatorUsername = callId.split("_")[0];

            ClientHandler initiatorHandler = AlanyaCentralServer.getClientHandler(initiatorUsername);
            if (initiatorHandler != null && initiatorHandler.isAuthenticated) {
                Map<String, String> forwardData = new HashMap<>(data);
                forwardData.put("responderUsername", authenticatedUsername);
                if(accepted) {
                    ClientInfo responderP2PInfo = AlanyaCentralServer.getP2PClientInfo(authenticatedUsername);
                    if (responderP2PInfo != null) {
                         forwardData.put("ip", responderP2PInfo.getHost());
                    } else {
                         forwardData.put("ip", clientSocket.getInetAddress().getHostAddress());
                    }
                    initiatorHandler.sendResponse(true, "Appel accepté.", forwardData, ServerResponseType.CALL_ACCEPTED_BY_PEER);
                } else {
                    initiatorHandler.sendResponse(false, "Appel rejeté.", forwardData, ServerResponseType.CALL_REJECTED_BY_PEER);
                }
            }
        }

        private void handleEndCall(ServerCommand command) throws IOException {
             if (!isAuthenticated) return;
            String targetUsername = command.getData().get("targetUsername");
            ClientHandler targetHandler = AlanyaCentralServer.getClientHandler(targetUsername);
            if (targetHandler != null && targetHandler.isAuthenticated) {
                Map<String, String> forwardData = Map.of(
                    "username", authenticatedUsername, 
                    "callId", command.getData().get("callId"), 
                    "type", command.getData().get("type")
                );
                targetHandler.sendResponse(true, "Appel terminé par le pair.", forwardData, ServerResponseType.CALL_ENDED_BY_PEER);
            }
        }
        
        private void sendResponse(boolean success, String message, Map<String, String> data, ServerResponseType type) throws IOException {
            if (out != null && !clientSocket.isClosed()) {
                out.writeObject(new ServerResponse(success, message, data, type));
                out.flush();
            }
        }

        public static void updateClientStatusInDatabase(String username, String status) {
            String sql = "UPDATE Utilisateurs SET statut = ?, derniere_deconnexion_timestamp = CURRENT_TIMESTAMP WHERE nom_utilisateur = ?";
            if (status.equals("actif")) {
                 sql = "UPDATE Utilisateurs SET statut = ?, derniere_connexion_timestamp = CURRENT_TIMESTAMP WHERE nom_utilisateur = ?";
            }
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setString(2, username);
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Erreur SQL MAJ statut pour " + username + ": " + e.getMessage());
            }
        }

        public static void updateClientP2PServerInfoInDB(String username, String host, int port) {
            String sql = "UPDATE Utilisateurs SET last_known_ip = ?, last_known_port = ? WHERE nom_utilisateur = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, host);
                stmt.setInt(2, port);
                stmt.setString(3, username);
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Erreur SQL MAJ P2P info pour " + username + ": " + e.getMessage());
            }
        }
    }
    
    public static class ClientInfo implements Serializable {
        private static final long serialVersionUID = 1L; 
        private final String username;
        private final String host;
        private final int port;
        public ClientInfo(String username, String host, int port) {
            this.username = username;
            this.host = host;
            this.port = port;
        }
        public String getUsername() { return username; }
        public String getHost() { return host; }
        public int getPort() { return port; }
        @Override
        public String toString() {
            return username + " @ " + (host != null ? host : "N/A") + ":" + (port > 0 ? port : "N/A");
        }
    }
}