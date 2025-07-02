package com.Alanya;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/Alanya/AdminLogin.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Alanya - Connexion Administrateur");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    //USER:admin PASSWORD:password123
    public static void main(String[] args) {
        launch(args);
    }
}