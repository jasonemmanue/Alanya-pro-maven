package com.Alanya;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

public class Client {
    private int id;
    private String nomUtilisateur;
    private String motDePasseHash;
    private String email;
    private String telephone;
    private String statut;
    private boolean estAdmin;
    private String lastKnownIp;
    private int lastKnownPort;
    private byte[] profilePicture; // CHAMP AJOUTÉ

    private static final Pattern PHONE_PATTERN_CM = Pattern.compile("^\\+237[6-9]\\d{8}$");
    private static final Pattern PHONE_PATTERN_INTERNATIONAL_GENERIC = Pattern.compile("^\\+\\d{10,14}$");

    public Client() {
        this.statut = "actif";
        this.estAdmin = false;
    }

    public Client(int id, String nomUtilisateur, String email,
                  String telephone, String statut, boolean estAdmin,
                  String lastKnownIp, int lastKnownPort) {
        this.id = id;
        this.nomUtilisateur = nomUtilisateur;
        this.email = email;
        this.telephone = telephone;
        this.statut = statut;
        this.estAdmin = estAdmin;
        this.lastKnownIp = lastKnownIp;
        this.lastKnownPort = lastKnownPort;
        this.profilePicture = null; // Initialisé à null par défaut
    }

    public Client(int id, String nomUtilisateur, String email,
                   String telephone, String statut, boolean estAdmin) {
        this(id, nomUtilisateur, email, telephone, statut, estAdmin, null, 0);
    }

    // Getters
    public int getId() { return id; }
    public String getNomUtilisateur() { return nomUtilisateur; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getStatut() { return statut; }
    public boolean isEstAdmin() { return estAdmin; }
    public String getLastKnownIp() { return lastKnownIp; }
    public int getLastKnownPort() { return lastKnownPort; }
    public byte[] getProfilePicture() { return profilePicture; } // GETTER AJOUTÉ

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }
    public void setEmail(String email) { this.email = email; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setEstAdmin(boolean estAdmin) { this.estAdmin = estAdmin; }
    public void setLastKnownIp(String lastKnownIp) { this.lastKnownIp = lastKnownIp; }
    public void setLastKnownPort(int lastKnownPort) { this.lastKnownPort = lastKnownPort; }
    public void setProfilePicture(byte[] profilePicture) { this.profilePicture = profilePicture; } // SETTER AJOUTÉ

    public void setMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide.");
        }
        this.motDePasseHash = hashMotDePasse(motDePasse);
    }

    static String hashMotDePasse(String motDePasse) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(motDePasse.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Erreur de hachage du mot de passe", e);
        }
    }
    
    public static boolean validerEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(regex).matcher(email).matches();
    }

    public static boolean validerTelephoneFormatInscriptionCM(String telephone) {
        if (telephone == null || telephone.trim().isEmpty()) return false;
        return PHONE_PATTERN_CM.matcher(telephone).matches();
    }

    public static boolean validerTelephoneFormatGeneric(String telephone) {
        if (telephone == null || telephone.trim().isEmpty()) return false;
        return PHONE_PATTERN_INTERNATIONAL_GENERIC.matcher(telephone).matches() || PHONE_PATTERN_CM.matcher(telephone).matches();
    }

    public ResultatOperation inscrire(Connection connexion) throws SQLException {
        if (connexion == null) {
            return new ResultatOperation(false, "Erreur BDD: Connexion nulle.");
        }
        if (this.nomUtilisateur == null || this.nomUtilisateur.trim().isEmpty()) {
            return new ResultatOperation(false, "Le nom d'utilisateur est obligatoire.");
        }
        if (this.motDePasseHash == null) {
             return new ResultatOperation(false, "Mot de passe non défini pour l'inscription.");
        }

        boolean emailProvided = this.email != null && !this.email.trim().isEmpty();
        boolean phoneProvided = this.telephone != null && !this.telephone.trim().isEmpty();

        if (!emailProvided && !phoneProvided) {
            return new ResultatOperation(false, "Un email OU un numéro de téléphone est requis pour l'inscription.");
        }

        if (emailProvided && !validerEmailFormat(this.email)) {
            return new ResultatOperation(false, "Format d'email invalide.");
        }

        if (phoneProvided && !validerTelephoneFormatInscriptionCM(this.telephone)) {
            return new ResultatOperation(false, "Format du numéro de téléphone invalide pour l'inscription. Utilisez +237xxxxxxxxx.");
        }

        String sql = "INSERT INTO Utilisateurs (nom_utilisateur, mot_de_passe, email, telephone, statut, est_admin) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (checkIfExists(connexion, "nom_utilisateur", this.nomUtilisateur)) {
                return new ResultatOperation(false, "Ce nom d'utilisateur existe déjà.");
            }
            if (emailProvided && checkIfExists(connexion, "email", this.email)) {
                return new ResultatOperation(false, "Cet email est déjà utilisé.");
            }
            if (phoneProvided && checkIfExists(connexion, "telephone", this.telephone)) {
                return new ResultatOperation(false, "Ce numéro de téléphone est déjà utilisé.");
            }

            pstmt.setString(1, this.nomUtilisateur);
            pstmt.setString(2, this.motDePasseHash);
            pstmt.setString(3, emailProvided ? this.email : null);
            pstmt.setString(4, phoneProvided ? this.telephone : null);
            pstmt.setString(5, this.statut != null ? this.statut : "actif");
            pstmt.setBoolean(6, this.estAdmin);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return new ResultatOperation(false, "Échec de l'inscription, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    this.id = generatedKeys.getInt(1);
                    return new ResultatOperation(true, "Inscription réussie !", this);
                } else {
                    return new ResultatOperation(false, "Échec de l'inscription, impossible de récupérer l'ID.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().toLowerCase().contains("unique constraint") || e.getMessage().toLowerCase().contains("duplicate entry")) {
                 if (e.getMessage().contains("nom_utilisateur")) return new ResultatOperation(false, "Ce nom d'utilisateur existe déjà (erreur BD).");
                 if (emailProvided && e.getMessage().contains("email")) return new ResultatOperation(false, "Cet email est déjà utilisé (erreur BD).");
                 if (phoneProvided && e.getMessage().contains("telephone")) return new ResultatOperation(false, "Ce numéro de téléphone est déjà utilisé (erreur BD).");
            }
            return new ResultatOperation(false, "Erreur SQL lors de l'inscription: " + e.getMessage());
        }
    }

    private boolean checkIfExists(Connection conn, String fieldName, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            return false; 
        }
        String sql = "SELECT id FROM Utilisateurs WHERE " + fieldName + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }


    public static ResultatOperation connexion(Connection connexion, String identifiant, String motDePasse) {
        if (connexion == null) {
            return new ResultatOperation(false, "Erreur BDD: Connexion nulle.");
        }
        String hashedPassword = hashMotDePasse(motDePasse);
        
        String sql = "SELECT id, nom_utilisateur, email, telephone, statut, est_admin, mot_de_passe, last_known_ip, last_known_port, profile_picture " +
                     "FROM Utilisateurs WHERE (nom_utilisateur = ? OR email = ? OR telephone = ?)";

        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, identifiant);
            pstmt.setString(2, identifiant); 
            pstmt.setString(3, identifiant); 

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dbPasswordHash = rs.getString("mot_de_passe");
                    if (dbPasswordHash.equals(hashedPassword)) {
                        String statut = rs.getString("statut");
                        if (!"actif".equalsIgnoreCase(statut) && 
                            !"contact".equalsIgnoreCase(statut) &&
                            !"hors-ligne".equalsIgnoreCase(statut) ) {
                            return new ResultatOperation(false, "Connexion non autorisée pour le statut: " + statut);
                        }

                        Client client = new Client(
                                rs.getInt("id"),
                                rs.getString("nom_utilisateur"),
                                rs.getString("email"),
                                rs.getString("telephone"),
                                statut, 
                                rs.getBoolean("est_admin"),
                                rs.getString("last_known_ip"),
                                rs.getInt("last_known_port")
                        );
                        // Récupérer la photo de profil
                        client.setProfilePicture(rs.getBytes("profile_picture"));

                        return new ResultatOperation(true, "Connexion réussie !", client);
                    } else {
                        return new ResultatOperation(false, "Identifiant ou mot de passe incorrect.");
                    }
                } else {
                    return new ResultatOperation(false, "Identifiant ou mot de passe incorrect.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ResultatOperation(false, "Erreur SQL lors de la connexion: " + e.getMessage());
        }
    }
    
    public static String getUsernameById(Connection conn, int userId) throws SQLException {
        String sql = "SELECT nom_utilisateur FROM Utilisateurs WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nom_utilisateur");
                } else {
                     System.err.println("Utilisateur non trouvé par ID: " + userId);
                    return null; 
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return nomUtilisateur + (estAdmin ? " (Admin)" : "") + (statut != null ? " - " + statut : "");
    }
}