package ru.geekbrains.storage.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class AuthController implements Initializable {

    public PasswordField authPasswordField;
    public TextField authLoginField;
    @FXML
    public Button authEnterButton;
    public Button authRegButton;

    private Network network;
    private StageChange stageChange;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = Network.getInstance();
        Platform.runLater(() -> {
            //получаем ссылку на текущее открытое окно
            stageChange = StageChange.getInstance();
            stageChange.getCurrentStage().setOnCloseRequest(event -> network.сloseConnection());
        });
    }

    //авторизация на сервере (кнопка Войти)
    public void tryToAuth(ActionEvent actionEvent) throws IOException {
        if (authLoginField.getText().isEmpty() || authPasswordField.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Введите логин и пароль", ButtonType.OK).showAndWait();
            return;
        }

        //соединение с сервером
        if (network.connect()) {
            network.sendAuthMessageToServer(authLoginField.getText(), authPasswordField.getText());
        }
        ;
    }


    //кнопка регистрация
    public void registrationUser(ActionEvent actionEvent) throws IOException {
        network.openWindow("/registration.fxml", "Регистрация нового пользователя",
                "", "", null);
    }
}
