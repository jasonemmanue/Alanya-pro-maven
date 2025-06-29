package com.Alanya;

public enum ServerResponseType {
    // Réponses générales
    GENERIC_SUCCESS,
    GENERIC_ERROR,
    
    // Réponses d'authentification
    AUTHENTICATION_SUCCESS,
    AUTHENTICATION_FAILED,
    
    // Réponses liées aux serveurs P2P
    P2P_SERVER_REGISTERED,
    P2P_PEER_INFO,
    
    // Réponses liées aux appels
    NEW_INCOMING_CALL,
    CALL_ACCEPTED_BY_PEER,
    CALL_REJECTED_BY_PEER,
    CALL_ENDED_BY_PEER,
    CALL_PORT_INFO_FOR_INITIATOR,
    
    // Autres réponses
    USER_ALREADY_CONNECTED,
    SERVER_SHUTDOWN,
    PEER_PROFILE_UPDATED 
}