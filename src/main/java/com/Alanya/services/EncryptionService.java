package com.Alanya.services;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.prefs.Preferences;

public class EncryptionService {

    private static final String ALGORITHM = "AES";
    // La clé DOIT faire 16, 24 ou 32 bytes. Nous utiliserons 16 bytes (128 bits).
    private static final String PREF_KEY_NAME = "alanyaAppSecretKey";
    private static Key secretKey;

    // Bloc statique pour charger ou générer la clé au démarrage
    static {
        try {
            // Utilise les Préférences Java pour stocker la clé de manière un peu plus sécurisée qu'en dur
            Preferences prefs = Preferences.userNodeForPackage(EncryptionService.class);
            String encodedKey = prefs.get(PREF_KEY_NAME, null);
            
            if (encodedKey == null) {
                // Générer une clé si elle n'existe pas. NE JAMAIS CHANGER CETTE CHAÎNE une fois en production !
                // Remplacez cette chaîne par votre propre phrase secrète de 16 caractères.
                String keyString = "MySuperSecretKey"; // DOIT FAIRE 16 CARACTÈRES
                if (keyString.length() != 16) {
                    throw new IllegalArgumentException("La clé secrète doit faire exactement 16 caractères.");
                }
                secretKey = new SecretKeySpec(keyString.getBytes(StandardCharsets.UTF_8), ALGORITHM);
                // Sauvegarder la clé encodée pour les futurs lancements
                prefs.put(PREF_KEY_NAME, Base64.getEncoder().encodeToString(secretKey.getEncoded()));
                prefs.flush();
            } else {
                // Charger la clé existante
                byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
                secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'initialisation du service de chiffrement.", e);
        }
    }

    public static String encrypt(String data) {
        if (data == null) return null;
        try {
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encVal = c.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // On encode en Base64 pour stocker le résultat binaire dans un format texte (pour la BDD)
            return Base64.getEncoder().encodeToString(encVal);
        } catch (Exception e) {
            System.err.println("Erreur de chiffrement: " + e.getMessage());
            return data; // En cas d'erreur, on retourne la donnée en clair pour ne pas bloquer
        }
    }

    public static String decrypt(String encryptedData) {
        if (encryptedData == null) return null;
        try {
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, secretKey);
            // On décode d'abord le Base64 avant de déchiffrer
            byte[] decodedValue = Base64.getDecoder().decode(encryptedData);
            byte[] decValue = c.doFinal(decodedValue);
            return new String(decValue, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Erreur de déchiffrement: " + e.getMessage() + ". La donnée est peut-être en clair.");
            return encryptedData; // Retourne la donnée telle quelle si le déchiffrement échoue
        }
    }
}