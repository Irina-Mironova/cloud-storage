package ru.geekbrains.storage.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.geekbrains.storage.util.FilesUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class CloudController implements Initializable {
    public ListView storServerView;
    public Button storAddButton;
    public Button storDelButton;
    public Button storRenameButton;
    public Button storSharButton;
    public Button storCloseButton;
    @FXML
    public Label storUserLabel;
    public Label storLoginLabel;
    public Button storSaveButton;


    public Network network;
    FilesUtils filesUtils;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = Network.getInstance();

        //подключение к серверу
        network.connect();
        filesUtils = new FilesUtils();
    }

    //кнопка Добавить файл
    public void addFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser(); //Класс работы с диалогом выборки и сохранения
        fileChooser.setTitle("Add document to cloud"); //Заголовок диалога
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("All files ", "*.*");
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showOpenDialog(network.getStageChange().getCurrentStage());
        if (file != null) {
            filesUtils.sendFile(file.toString(), storLoginLabel.getText(), "", network.getObjectEncoderOutputStream(), null);
        }
    }

    //кнопка Удалить файл
    public void deleteFile(ActionEvent actionEvent) throws IOException {
        if (!storServerView.getSelectionModel().isEmpty()) {
            String fileName = storServerView.getSelectionModel().getSelectedItem().toString();
            //открытие диалогового окна для подтверждения удаления файла
            openDialogWindow("/dialogDelete.fxml", fileName, storLoginLabel.getText());
        } else {
            network.showCONFIRMATION("Выберите из списка файл для удаления");
        }
    }

    // Кнопка Переименовать файл
    public void renameFile(ActionEvent actionEvent) {
        if (!storServerView.getSelectionModel().isEmpty()) {
            String fileName = storServerView.getSelectionModel().getSelectedItem().toString();
            openDialogWindow("/dialogRename.fxml", fileName, storLoginLabel.getText());
        } else {
            network.showCONFIRMATION("Выберите из списка файл для переименования");
        }
    }

    //кнопка Поделиться файлом
    public void shareFile(ActionEvent actionEvent) {
        if (!storServerView.getSelectionModel().isEmpty()) {
            String fileName = storServerView.getSelectionModel().getSelectedItem().toString();
            openDialogWindow("/dialogShare.fxml", fileName, storLoginLabel.getText());
        } else {
            network.showCONFIRMATION("Выберите из списка файл, которым хотите поделиться с другим пользователем");
        }

    }

    //Кнопка Выход
    public void closeWindow(ActionEvent actionEvent) {
        network.сloseConnection();
    }

    //кнопка Выгрузить файл
    public void saveFile(ActionEvent actionEvent) {
        if (!storServerView.getSelectionModel().isEmpty()) {
            String fileName = storServerView.getSelectionModel().getSelectedItem().toString();
            FileChooser fileChooser = new FileChooser(); //Класс работы с диалогом выборки и сохранения
            fileChooser.setTitle("Save file from cloud"); //Заголовок диалога
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("All files ", "*.*");
            fileChooser.getExtensionFilters().add(filter);
            fileChooser.setInitialFileName(fileName);
            File file = fileChooser.showSaveDialog((network.getStageChange().getCurrentStage()));
            if (file != null) {
                network.sendSaveFileAskToServer(fileName, file.getParent(), storLoginLabel.getText());
            }
        } else {
            network.showCONFIRMATION("Выберите из списка файл для выгрузки с сервера");
        }
    }

    // открытие диалогового окна
    public void openDialogWindow(String fxml, String fileName, String login) {
        Platform.runLater(() -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
                Parent root1 = (Parent) fxmlLoader.load();

                if (fxml.equals("/dialogDelete.fxml")) {
                    DialogDeleteController controller = fxmlLoader.<DialogDeleteController>getController();
                    controller.setFileName(fileName);
                    controller.setLogin(login);
                }

                if (fxml.equals("/dialogRename.fxml")) {
                    DialogRenameController controller = fxmlLoader.<DialogRenameController>getController();
                    controller.setFileName(fileName);
                    controller.setLogin(login);
                    controller.setListFiles(storServerView.getItems());
                }

                if (fxml.equals("/dialogShare.fxml")) {
                    DialogShareController controller = fxmlLoader.<DialogShareController>getController();
                    controller.setFileName(fileName);
                    controller.setLogin(login);
                }

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Сonfirmation");
                stage.setScene(new Scene(root1));
                stage.resizableProperty().set(false);
                stage.show();
            } catch (IOException e) {
                log.error("Ошибка при открытии окна", e);
            }
        });
    }

}
