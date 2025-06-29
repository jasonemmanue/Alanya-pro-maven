package com.Alanya;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe qui représente une commande envoyée au serveur
 */
public class Server_Command implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final CommandType type;
    private final Map<String, String> data;
    
    public Server_Command(CommandType type) {
        this(type, new HashMap<>());
    }
    
    public Server_Command(CommandType type, Map<String, String> data) {
        this.type = type;
        this.data = data != null ? data : new HashMap<>();
    }
    
    public CommandType getType() {
        return type;
    }
    
    public Map<String, String> getData() {
        return data;
    }
    
    @Override
    public String toString() {
        return "ServerCommand{type=" + type + ", data=" + data + "}";
    }
    
    /**
     * Types de commandes possibles
     */
    public enum CommandType {
        AUTHENTICATE,        
        REGISTER,           
        DISCONNECT,         
        CLIENT_SERVER_STARTED,
        GET_ONLINE_CLIENTS  
    }
}