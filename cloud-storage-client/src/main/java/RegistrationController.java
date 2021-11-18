import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
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

    private Socket socket;
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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

        //подключение к серверу
        connect();

    }

    //подключение к серверу
    private void connect() {
        if (socket != null && !socket.isClosed()) {
            return;
        }
        try {
            // устанавливаем соединение с сервером
            socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            //запускаем отдельный демон-поток для общения с сервером
            Thread thread = new Thread(this::readReg);
            thread.setDaemon(true);
            thread.start();

            //отправка на сервер сообщения с данными для регистрации нового пользователя
            sendTextMessageServer("/reg " + regLastNameField.getText() + " " + regNameField.getText() + " " +
                    regEmailField.getText() + " " + regLoginField.getText() + " " + regPasswordField.getText());

        } catch (IOException e) {
            Platform.runLater(() -> {
                new Alert(Alert.AlertType.ERROR, "Невозможно подключиться к серверу", ButtonType.OK).showAndWait();
            });
        }
    }

    //обработка входящих сообщений
    private void readReg() {
        try {
            while (true) {
                Message message = (Message) is.readObject();
                //обработка входящего сообщения в зависимости от его типа
                switch (message.getTypeMessage()) {
                    case TEXT_MESSAGE: //пришло текстовое сообщение
                        TextMessage textMessage = (TextMessage) message;
                        System.out.println("От сервера: " + textMessage.getTextMessage());

                        // если пришло сообщение об успешной регистрации пользователя
                        if (textMessage.getTextMessage().startsWith("/regOK")) {   //    /regOK
                            Platform.runLater(() -> {
                                new Alert(Alert.AlertType.CONFIRMATION, "Регистрация прошла успешно", ButtonType.OK).showAndWait();
                                try {
                                    //закрываем окно регистрации и открываем окно авторизации
                                    openWindow("authorization.fxml", "Облачное хранилище");
                                } catch (IOException e) {
                                    log.error("Ошибка при открытии окна", e);
                                }
                            });
                            break;
                        }

                        // если пришло сообщение о неуспешной регистрации пользователя из-за занятого логина
                        if (textMessage.getTextMessage().startsWith("/regError login")) {  // /regError login
                            Platform.runLater(() -> {
                                new Alert(Alert.AlertType.ERROR, "Указанный логин уже используется", ButtonType.OK).showAndWait();
                            });
                            break;
                        }

                        // если пришло сообщение о неуспешной регистрации пользователя из-за занятого email
                        if (textMessage.getTextMessage().startsWith("/regError email")) { // /regError email
                            Platform.runLater(() -> {
                                new Alert(Alert.AlertType.ERROR, "Указанный email уже используется", ButtonType.OK).showAndWait();
                            });
                            break;
                        }
                        break;
                    case FILE_MESSAGE:
                        break;
                    case LIST_MESSAGE:
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error("Ошибка при отправке сообщений на сервер", e);
        }
    }

    //отправка текстового сообщения на сервер
    private void sendTextMessageServer(String message) {
        try {
            os.writeObject(new TextMessage(message));
            os.flush();
            log.debug("От клиента: " + message);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Ошибка при отправке сообщения на сервер");
        }
    }

    //закрытие текущего окна и открытие нового
    private void openWindow(String fxml, String title) throws IOException {
        //Закрываем текущее окно
        Stage stage = (Stage) regRegistrButton.getScene().getWindow();
        stage.close();
        //открываем другое окно
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
        Parent root1 = (Parent) fxmlLoader.load();
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setScene(new Scene(root1));
        stage.resizableProperty().set(false);
        stage.show();
    }

}
