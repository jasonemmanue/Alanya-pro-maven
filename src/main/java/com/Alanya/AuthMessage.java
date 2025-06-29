package com.Alanya;

import java.io.Serializable;

public class AuthMessage implements Serializable {
    private static final long serialVersionUID = 100L; // UID pour la sérialisation
    private final String username;

    public AuthMessage(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "AuthMessage{username='" + username + "'}";
    }
}