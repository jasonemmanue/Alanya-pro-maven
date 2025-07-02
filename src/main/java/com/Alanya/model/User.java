package com.Alanya.model;

public class User {
    private long id;
    private String username;
    private boolean isAdmin;

    // Getters
    public long getId() { return id; }
    public String getUsername() { return username; }
    public boolean isAdmin() { return isAdmin; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    /**
     * Surcharge de la méthode toString() pour un affichage correct
     * dans la ListView de l'AdminPanel.
     */
    @Override
    public String toString() {
        return username;
    }
}