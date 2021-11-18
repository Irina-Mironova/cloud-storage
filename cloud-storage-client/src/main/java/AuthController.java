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
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class AuthController implements Initializable {

    public PasswordField authPasswordField;
    public TextField authLoginField;
    public Button authEnterButton;
    public Button authRegButton;

    private Socket socket;
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    private List<String> listFiles;
    private String userName;

    public void authCloseConnection() {
        System.out.println("Закрытие соединений");
        try {
            if (is != null) {
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

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    //авторизация на сервере (кнопка Войти)
    public void tryToAuth(ActionEvent actionEvent) throws IOException {
        if (authLoginField.getText().isEmpty() || authPasswordField.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Введите логин и пароль", ButtonType.OK).showAndWait();
            return;
        }

        //соединение с сервером
        connect();
    }

    // закрытие окна авторизации и открытие нового окна (регистрации или облачного хранилища)
    private void openWindow(String fxml, String title, String fio, String login, ListMessage listMessage) throws IOException {
        //Закрываем текущее окно
        Stage stage = (Stage) authEnterButton.getScene().getWindow();
        stage.close();

        //открываем другое окно
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
        Parent root1 = (Parent) fxmlLoader.load();


        //если открываем главное окно облачного хранилища, то заполняем его данными
        if (!fio.isEmpty()) {
            CloudController controller = fxmlLoader.<CloudController>getController();
            //записываем имя пользователя в label
            controller.storUserLabel.setText(fio);
            controller.setLogin(login);
            // заполняем список файлов пользователя в listView
            controller.updateFileList((listMessage.getListFiles()));
        }

        //открываем другое окно
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setScene(new Scene(root1));
        stage.resizableProperty().set(false);
        stage.show();
    }

    //соединение с сервером
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
            Thread thread = new Thread(this::read);
            thread.setDaemon(true);
            thread.start();

            // отправка на сервер сообщения об авторизации
            sendTextMessageServer("/auth " + authLoginField.getText() + " " + authPasswordField.getText());
        } catch (IOException e) {
            ShowError("Невозможно подключиться к серверу");
        }
    }

    //обработка входящих сообщений
    private void read() {
        try {
            while (true) {
                //обработка входящего сообщения в зависимости от его типа
                Message message = (Message) is.readObject();
                switch (message.getTypeMessage()) {
                    case TEXT_MESSAGE: //пришло текстовое сообщение
                        TextMessage textMessage = (TextMessage) message;

                        //разбиваем текстовое сообщение на отдельные слова по пробелам и сохраняем в массив
                        String[] tokens = textMessage.getTextMessage().split("\\s");
                        System.out.println("От сервера: " + textMessage.getTextMessage());

                        // если пришло сообщение об успешной авторизации пользователя с указанным логином и паролем
                        if (textMessage.getTextMessage().startsWith("/authOK")) { //    /authOK lastname name login
                            userName = tokens[1] + " " + tokens[2];
                            //отправка на сервер запроса списка файлов пользователя
                            sendTextMessageServer("/list " + tokens[3]);  //    /list login
                            break;
                        }

                        // если пришло сообщение о неуспешной авторизации, выводим предупреждение
                        if (textMessage.getTextMessage().startsWith("/authError")) {
                            Platform.runLater(() -> {
                                new Alert(Alert.AlertType.ERROR, "Неверные логин или пароль, либо указанный логин не существует!", ButtonType.OK).showAndWait();
                            });
                        }
                        break;
                    case FILE_MESSAGE:
                        break;
                    case LIST_MESSAGE: //пришел список файлов
                        ListMessage listMessage = (ListMessage) message;
                        Platform.runLater(() -> {
                            try {
                                //открываем основное окно облачного хранилища и заполняем имя польз-ля и
                                //список файлов
                                openWindow("cloud.fxml", "Облачное хранилище", userName, "", listMessage);
                            } catch (IOException e) {
                                log.error("Ошибка при открытии окна", e);
                            }
                        });
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            log.error("Ошибка при отправке сообщений на сервер");
        }

    }

    // отправка на сервер текстового сообщения
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

    private void ShowError(String message) {
        Platform.runLater(() -> {
            //вывод сообщения об ошибке
            new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
        });
    }


    //кнопка регистрация
    public void registrationUser(ActionEvent actionEvent) throws IOException {
        Platform.runLater(() -> {
            try {
                openWindow("registration.fxml", "Регистрация нового пользователя", "", "", null);
            } catch (IOException e) {
                log.error("Ошибка при открытии окна", e);
            }
        });
    }
}
