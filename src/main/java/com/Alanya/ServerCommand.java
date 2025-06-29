package com.Alanya;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ServerCommand implements Serializable {
    private static final long serialVersionUID = 2L; 

    private final ServerCommandType type;
    private final Map<String, String> data;

    public ServerCommand(ServerCommandType type) {
        this(type, new HashMap<>());
    }

    public ServerCommand(ServerCommandType type, Map<String, String> data) {
        this.type = type;
        this.data = data != null ? data : new HashMap<>(); // Ensure data is never null
    }

    public ServerCommandType getType() {
        return type;
    }

    public Map<String, String> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ServerCommand{type=" + type + ", data=" + data + "}";
    }

    public enum ServerCommandType {
        AUTHENTICATE,      
        REGISTER,         
        DISCONNECT,       
        CLIENT_SERVER_STARTED,
        GET_PEER_INFO,
        
        // Commandes pour les appels
        INITIATE_AUDIO_CALL, // data: {"targetUsername": "userB"}
        INITIATE_VIDEO_CALL, // data: {"targetUsername": "userB"}
        CALL_RESPONSE,       // data: {"callId": "id_unique", "responderUsername": "userB", "accepted": "true/false", "type": "audio/video", "port": "port_du_repondeur_pour_p2p"}
        END_CALL,            // data: {"callId": "id_unique", "targetUsername": "userB"}
        CALL_PORT_INFO       // data: {"callId": "id_unique", "targetUsername": "userB", "port": "12345", "type": "audio/video"} -> envoyé par le serveur au demandeur après que le destinataire ait accepté et ouvert son port
, PROFILE_UPDATE

    }
}
