package com.Alanya;

import java.util.List;

public class ResultatOperation {
    private boolean succes;
    private String message;
    private Object donnees;

    
    public ResultatOperation(boolean succes, String message) {
        this.succes = succes;
        this.message = message;
        this.donnees = null;
    }

    
    public ResultatOperation(boolean succes, String message, Object donnees) {
        this.succes = succes;
        this.message = message;
        this.donnees = donnees;
    }

    
    public boolean estSucces() {
        return succes;
    }

    
    public String getMessage() {
        return message;
    }

    
    public Object getDonnees() {
        return donnees;
    }
    
    
    public Client getClient() {
        if (donnees instanceof Client) {
            return (Client) donnees;
        }
        return null;
    }
    
    
    @SuppressWarnings("unchecked")
    public List<Client> getListeClients() {
        if (donnees instanceof List<?>) {
            try {
                return (List<Client>) donnees;
            } catch (ClassCastException e) {
                return null;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "ResultatOperation{" +
                "succes=" + succes +
                ", message='" + message + '\'' +
                ", donnees=" + (donnees != null ? "présentes" : "absentes") +
                '}';
    }
}