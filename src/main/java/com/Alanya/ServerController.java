package com.Alanya;


import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ServerController implements Initializable {

    @FXML
    private Button startServerButton;

    @FXML
    private Button stopServerButton;

    @FXML
    private Button showClientsButton;

    @FXML
    private Circle statusIndicator;

    @FXML
    private TextArea logTextArea;

    @FXML
    private ListView<String> clientsListView;

    private AlanyaCentralServer server;
    private final ObservableList<String> clientsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientsListView.setItems(clientsList);
        setServerStatus(false);
        stopServerButton.setDisable(true);
    }

    public void setServer(AlanyaCentralServer server) {
        this.server = server;
    }

    @FXML
    void handleShowClients(ActionEvent event) {
        updateClientList();
    }

    @FXML
    void handleStartServer(ActionEvent event) {
        if (server != null) {
            server.startServer();
            startServerButton.setDisable(true);
            stopServerButton.setDisable(false);
        } else {
            logMessage("Erreur: référence au serveur non initialisée");
        }
    }

    @FXML
    void handleStopServer(ActionEvent event) {
        if (server != null) {
            server.stopServer();
            startServerButton.setDisable(false);
            stopServerButton.setDisable(true);
        } else {
            logMessage("Erreur: référence au serveur non initialisée");
        }
    }

    public void updateClientList() {
        if (server != null) {
            List<String> connectedClients = server.getConnectedClientsList();
            Platform.runLater(() -> {
                clientsList.clear();
                clientsList.addAll(connectedClients);
                logMessage("Liste des clients mise à jour: " + connectedClients.size() + " client(s) connecté(s)");
            });
        }
    }

    public void setServerStatus(boolean running) {
        Platform.runLater(() -> {
            statusIndicator.setFill(running ? Color.GREEN : Color.RED);
        });
    }

    public void logMessage(String message) {
        Platform.runLater(() -> {
            logTextArea.appendText(message + "\n");
            // Auto-scroll to bottom
            logTextArea.setScrollTop(Double.MAX_VALUE);
        });
    }
}
