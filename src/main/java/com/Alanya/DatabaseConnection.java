package com.Alanya;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String HOST = "163.123.183.89"; 

    private static final String PORT = "17705"; 

    private static final String DB_NAME = "alaniaOther"; 

    private static final String USER = "people"; 

    private static final String PASSWORD = "people2030";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME + "?useSSL=true&serverTimezone=UTC";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            System.out.println("Tentative de connexion à : " + URL);
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion à la base de données en ligne réussie !");

        } catch (ClassNotFoundException e) {
            System.err.println("Erreur : Driver JDBC MySQL non trouvé. Assurez-vous que le connecteur est dans votre projet.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur : Échec de la connexion à la base de données en ligne.");
            System.err.println("Vérifiez les informations (Hôte, Port, User, Mot de passe) et les paramètres SSL.");
            System.err.println("Assurez-vous que l'IP de votre machine est autorisée par le pare-feu de l'hébergeur.");
            e.printStackTrace();
        }
        return conn;
    }
}












/*package com.Alanya;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Informations de connexion pour la base de données Railway
    private static final String HOST = "hopper.proxy.rlwy.net";
    private static final String PORT = "31345";
    private static final String DATABASE = "railway";
    private static final String USER = "root";
    private static final String PASSWORD = "JlrSGxeqZKsgZBpkbbFNJYaKlsudxiHe";

    // Construction de l'URL de connexion JDBC
    // On ajoute ?allowPublicKeyRetrieval=true pour résoudre le problème de connexion
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?allowPublicKeyRetrieval=true&useSSL=false";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Chargement du driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Établissement de la connexion
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur : Driver JDBC non trouvé.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur : Échec de la connexion à la base de données en ligne.");
            e.printStackTrace();
        }
        return conn;
    }
}
*/
/*
package com.Alanya;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/alanyadb?useSSL=false";
    private static final String USER = "useralanya";
    private static final String PASSWORD = "123456";

    public static Connection getConnection() {
        Connection conn = null;
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur : Driver JDBC non trouvé.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur : Échec de la connexion à la base de données.");
            e.printStackTrace();
        }
        return conn;
    }
}
*/


