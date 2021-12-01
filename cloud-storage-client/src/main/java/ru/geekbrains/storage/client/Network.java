package ru.geekbrains.storage.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.geekbrains.storage.common.*;
import ru.geekbrains.storage.util.FilesUtils;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class Network {
    private static Network instance;
    private StageChange stageChange;

    private Network() {
    }

    private CloudController controller;

    public static Network getInstance() {
        if (instance == null) {
            instance = new Network();
        }
        return instance;
    }

    public StageChange getStageChange() {
        return stageChange;
    }

    private Socket socket;
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    private FilesUtils filesUtils;
    private String userName;
    private String login;
    private Thread thread;

    public ObjectEncoderOutputStream getObjectEncoderOutputStream() {
        return os;
    }

    //соединение с сервером
    public boolean connect() {
        if (socket != null && !socket.isClosed()) {
            return true;
        }
        try {
            // устанавливаем соединение с сервером
            socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            filesUtils = new FilesUtils();

            //запускаем отдельный поток для общения с сервером
            thread = new Thread(this::read);
            // thread.setDaemon(true);
            thread.start();

            return true;
        } catch (IOException e) {
            showError("Невозможно подключиться к серверу");
            return false;
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            //вывод сообщения об ошибке
            new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
        });
    }

    public void showCONFIRMATION(String message) {
        Platform.runLater(() -> {
            //вывод информационного сообщения
            new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK).showAndWait();
        });
    }

    //обработка входящих сообщений
    private void read() {
        try {
            while (true) {
                //обработка входящего сообщения в зависимости от его типа
                Message message = (Message) is.readObject();
                switch (message.getTypeMessage()) {
                    //пришло сообщение об успешной авторизации пользователя с указанным логином и паролем
                    case AUTH_OK:
                        AuthOK authOK = (AuthOK) message;
                        log.debug("Сообщение от сервера: успешная авторизация");
                        userName = authOK.getUsername();
                        login = authOK.getLogin();
                        //отправка на сервер запроса списка файлов пользователя
                        sendListMessageToServer(authOK.getLogin());
                        break;

                    // если пришло сообщение с частью файла
                    case FILE_MESSAGE:
                        FileMessage fileMessage = (FileMessage) message;
                        Path dirTmp = Paths.get(Paths.get("").toAbsolutePath().toString(), "cloud-storage-client",
                                "client", "tmp", fileMessage.getLogin());
                        filesUtils.saveFile(dirTmp, Paths.get(fileMessage.getDirDestination()), fileMessage);
                        break;

                    // если пришло сообщение о неуспешной авторизации, выводим предупреждение
                    case AUTH_ERROR:
                        showError("Неверные логин или пароль, либо указанный логин не существует!");
                        break;

                    // если пришло сообщение об успешной регистрации пользователя
                    case REG_OK:
                        openWindow("/authorization.fxml", "Облачное хранилище", "", "", null);
                        showCONFIRMATION("Регистрация прошла успешно");
                        break;

                    // если пришло сообщение о неуспешной регистрации пользователя из-за занятого логина
                    case REG_ERROR:
                        RegError regError = (RegError) message;
                        if (!regError.getLogin().isEmpty()) {
                            showError("Логин " + regError.getLogin() + "  уже используется");
                        } else {
                            showError("Email " + regError.getEmail() + "  уже используется");
                        }
                        break;

                    //пришел список файлов
                    case LIST_MESSAGE:
                        ListMessage listMessage = (ListMessage) message;
                        if (controller == null) {
                            //открываем основное окно облачного хранилища и заполняем имя польз-ля и
                            //список файлов
                            openWindow("/cloud.fxml", "Облачное хранилище", userName, login, listMessage);
                        } else {
                            updateFileList(listMessage.getListFiles(), controller);
                        }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            log.error("Ошибка при отправке сообщений на сервер");
        }
    }

    // закрытие окна авторизации и открытие нового окна (регистрации или облачного хранилища)
    public void openWindow(String fxml, String title, String fio, String login, ListMessage listMessage) throws IOException {
        Platform.runLater(() -> {
            try {
                //получаем ссылку на текущее открытое окно
                stageChange = StageChange.getInstance();
                Stage stage = stageChange.getCurrentStage();
                //Закрываем текущее окно
                stage.close();

                //открываем другое окно
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
                Parent root1 = (Parent) fxmlLoader.load();

                //если открываем главное окно облачного хранилища, то заполняем его данными
                if (fxml.equals("/cloud.fxml")) {
                    controller = fxmlLoader.<CloudController>getController();

                    //записываем имя пользователя в label
                    controller.storUserLabel.setText(fio);
                    controller.storLoginLabel.setText(login);

                    // заполняем список файлов пользователя в listView
                    updateFileList((listMessage.getListFiles()), controller);
                }

                //открываем другое окно
                stage = new Stage();
                //сохраняем ссылку на текущее открытое окно
                stageChange.setCurrentScene(stage);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle(title);
                stage.setScene(new Scene(root1));
                if (fxml.equals("/registration.fxml")) {
                    stage.setOnCloseRequest(event -> {
                        try {
                            openWindow("/authorization.fxml", "Авторизация пользователя", "", "", null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                if (fxml.equals("/cloud.fxml")) {
                    stage.setOnCloseRequest(event -> сloseConnection());
                }
                stage.resizableProperty().set(false);
                stage.show();
            } catch (IOException e) {
                log.error("Ошибка при открытии окна", e);
            }
        });
    }

    // заполнение listView файлами из списка
    public void updateFileList(List<String> listFiles, CloudController controller) {
        Platform.runLater(() -> {
            controller.storServerView.getItems().clear();
            if (listFiles != null) {
                controller.storServerView.getItems().addAll(listFiles);
            }
        });
    }

    //закрытие соединений
    public void сloseConnection() {
        Platform.runLater(() -> {
            log.debug("Закрытие соединений");
            try {
                if (is != null) {
                    if (thread.isAlive()) {
                        thread.stop();
                    }
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Ошибка при закрытии потока");
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Ошибка при закрытии потока");
            }
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Ошибка при закрытии соединения");
            }
            // закрываем приложение
            Platform.exit();
        });
    }

    // отправка на сервер запроса авторизации пользователя
    public void sendAuthMessageToServer(String login, String password) {
        try {
            os.writeObject(new AuthAsk(login, password));
            os.flush();
            log.debug("Сообщение от клиента: запрос авторизации пользователя " + login);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Ошибка при отправке сообщения об авторизации на сервер");
        }
    }

    // отправка на сервер запроса  о выгрузке файла с сервера
    public void sendSaveFileAskToServer(String fileName, String dirDestination, String login) {
        try {
            os.writeObject(new FileAsk(fileName, dirDestination, login));
            os.flush();
            log.debug("Сообщение от клиента: запрос о выгрузке файла " + fileName + " с сервера");
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Ошибка при отправке запроса о выгрузке файла на сервер");
        }
    }

    // отправка на сервер запроса о списке файлов пользователя
    public void sendListMessageToServer(String login) {
        try {
            os.writeObject(new ListAsk(login));
            os.flush();
            log.debug("Сообщение от клиента: запрос списка файлов пользователя " + login);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Ошибка при отправке сообщения о списке файлов на сервер");
        }
    }

    // отправка на сервер запроса о регистрации нового пользователя
    public void sendRegMessageToServer(String lastname, String name, String email, String login, String password) {
        try {
            os.writeObject(new RegAsk(lastname, name, email, login, password));
            os.flush();
            log.debug("Сообщение от клиента: запрос о регистрации нового пользователя " + login);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Ошибка при отправке сообщения о регистрации нового пользователя на сервер");
        }
    }

    // отправка на сервер запроса об удалении файла
    public void sendDeleteFileMessageToServer(String fileName, String login) {
        try {
            os.writeObject(new DeleteAsk(fileName, login));
            os.flush();
            log.debug("Сообщение от клиента: запрос на удаление файла " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Ошибка при отправке запроса на удаление файла на сервер");
        }
    }

    // отправка на сервер запроса о переименовании файла
    public void sendRenameMessageToServer(String oldName, String newName, String login) {
        try {
            os.writeObject(new RenameAsk(oldName, newName, login));
            os.flush();
            log.debug("Сообщение от клиента: запрос на переименование файла " + oldName + " в " + newName);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Ошибка при отправке запроса на переименование файла на сервер");
        }
    }

    // отправка на сервер запроса о share файла
    public void sendShareMessageToServer(String fileName, String login, String email) {
        try {
            os.writeObject(new ShareAsk(fileName, login, email));
            os.flush();
            log.debug("Сообщение от клиента: пользователь хочет поделиться файлом с " + email);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Ошибка при отправке запроса на сервер");
        }
    }


}