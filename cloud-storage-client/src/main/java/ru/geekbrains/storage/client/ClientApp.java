package ru.geekbrains.storage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    private Network network;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent parent = FXMLLoader.load(getClass().getResource("/authorization.fxml"));
        StageChange stageChange = StageChange.getInstance();
        primaryStage.setScene(new Scene(parent));
        stageChange.setCurrentScene(primaryStage);
        primaryStage.resizableProperty().set(false);
        primaryStage.setTitle("Облачное хранилище");
//        Network.getInstance();
//        primaryStage.setOnCloseRequest(event -> network.сloseConnection());
        primaryStage.show();
    }
}
