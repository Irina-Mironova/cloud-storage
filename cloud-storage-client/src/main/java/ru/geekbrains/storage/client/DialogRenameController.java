package ru.geekbrains.storage.client;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DialogRenameController implements Initializable {
    public Button dialRenRenameButton;
    public Button dialRenCancelButton;
    public TextField dialRenNewNameField;

    private Network network;
    private String login;
    private String fileName;
    private List<String> listFiles;


    public void setLogin(String login) {
        this.login = login;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setListFiles(List<String> listFiles) {
        this.listFiles = listFiles;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = Network.getInstance();
    }

    // кнопка Переименовать
    public void renameFile(ActionEvent actionEvent) {
        if (!dialRenNewNameField.getText().isEmpty()) {
            if (listFiles.indexOf(dialRenNewNameField.getText()) == -1) {
                //отправка запроса на переименование файла на сервер
                network.sendRenameMessageToServer(fileName, dialRenNewNameField.getText(), login);
                closeWindow();
            } else network.showCONFIRMATION("Файл с таким именем уже существует. Выберите другое имя файла");
        } else {
            network.showCONFIRMATION("Введите новое имя файла " + fileName);
        }
    }

    // кнопка Отмена
    public void closeWindow() {
        Stage stage = (Stage) dialRenNewNameField.getScene().getWindow();
        stage.close();
    }


}
