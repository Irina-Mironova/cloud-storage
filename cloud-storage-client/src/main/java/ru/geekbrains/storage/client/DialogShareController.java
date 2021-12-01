package ru.geekbrains.storage.client;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DialogShareController implements Initializable {
    public Button dialShareShareButton;
    public Button dialShareCancelButton;
    public TextField dialShareEmailField;

    private Network network;
    private String login;
    private String fileName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = Network.getInstance();
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    // кнопка Поделиться файлом
    public void shareFile(ActionEvent actionEvent) {
        if (!dialShareEmailField.getText().isEmpty()) {
            //отправка запроса о желании пользователя поделиться файлом
            network.sendShareMessageToServer(fileName, login, dialShareEmailField.getText());
            closeWindow();
        } else {
            network.showCONFIRMATION("Введите email пользователя, с которым хотите поделиться файлом " + fileName);
        }
    }

    // кнопка Отмена
    public void closeWindow() {
        Stage stage = (Stage) dialShareCancelButton.getScene().getWindow();
        stage.close();
    }


}
