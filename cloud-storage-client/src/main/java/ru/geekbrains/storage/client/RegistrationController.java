package ru.geekbrains.storage.client;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class RegistrationController implements Initializable {
    public TextField regLastNameField;
    public TextField regNameField;
    public TextField regLoginField;
    public Button regRegistrButton;
    public Button regCloseButton;
    public TextField regEmailField;
    public PasswordField regPasswordField;
    public PasswordField regConfirmPasswordField;

    private Network network;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = Network.getInstance();
    }

    //кнопка Зарегистрироваться
    public void registration(ActionEvent actionEvent) throws IOException {
        //проверка корректности введенных пользователем данных
        if (regLastNameField.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Укажите фамилию", ButtonType.OK).showAndWait();
            return;
        }
        if (regNameField.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Укажите имя", ButtonType.OK).showAndWait();
            return;
        }
        if (regLoginField.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Укажите логин", ButtonType.OK).showAndWait();
            return;
        }
        if (regEmailField.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Укажите email", ButtonType.OK).showAndWait();
            return;
        }
        if (regPasswordField.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Укажите пароль", ButtonType.OK).showAndWait();
            return;
        }
        if (regConfirmPasswordField.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Укажите пароль для подтверждения", ButtonType.OK).showAndWait();
            return;
        }
        if (!regPasswordField.getText().equals(regConfirmPasswordField.getText())) {
            new Alert(Alert.AlertType.WARNING, "Пароли не совпадают", ButtonType.OK).showAndWait();
            return;
        }

        //соединение с сервером
        if (network.connect()) {
            //хешируем пароль
            String hashed = BCrypt.hashpw(regPasswordField.getText(), BCrypt.gensalt());
            // отправка на сервер сообщения с данными для регистрации нового пользователя
            network.sendRegMessageToServer(regLastNameField.getText(), regNameField.getText(),
                    regEmailField.getText(), regLoginField.getText(), hashed);
        }
    }

    //кнопка Отмена
    public void cancel(ActionEvent actionEvent) throws IOException {
        network.openWindow("/authorization.fxml", "Авторизация пользователя", "", "", null);
    }
}