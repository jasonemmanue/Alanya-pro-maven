package com.Alanya;


import java.io.Serializable;


 //Énumération des types de commandes pouvant être envoyées au serveur central
 
public enum ServerCommandType implements Serializable {
    AUTHENTICATE,            // Authentification d'un client
    REGISTER,                // Inscription d'un nouveau client
    DISCONNECT,              // Déconnexion d'un client
    CALL_PORT_INFO_FOR_INITIATOR,
    CLIENT_SERVER_STARTED,   // Notification que le client a démarré son serveur
    GET_ONLINE_CLIENTS,       // Demande la liste des clients en ligne
    PROFILE_UPDATE 
    
}
