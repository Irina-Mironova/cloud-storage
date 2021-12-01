package ru.geekbrains.storage.client;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DialogDeleteController implements Initializable {
    public Button dialDelYesButton;
    public Button dialDelNoButton;

    private Network network;
    private String login;
    private String fileName;

    public void setLogin(String login) {
        this.login = login;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = Network.getInstance();
    }

    //кнопка Yes
    public void deleteFile(ActionEvent actionEvent) {
        //отправка запроса на удаление файла на сервер
        network.sendDeleteFileMessageToServer(fileName, login);
        closeWindow();
    }

    //Кнопка Отмена
    public void closeWindow() {
        Stage stage = (Stage) dialDelYesButton.getScene().getWindow();
        stage.close();
    }
}
