import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
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
public class AuthController implements Initializable {
    public PasswordField authPasswordField;
    public TextField authLoginField;
    public Button authEnterButton;
    public Button authRegButton;

    private Socket socket;
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;


    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }

    public void tryToAuth(ActionEvent actionEvent) throws IOException {
        if (authLoginField.getText().isEmpty() || authPasswordField.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Введите логин и пароль", ButtonType.OK).showAndWait();
            return;
        }

        connect();
        sendTextMessageServer("/auth " + authLoginField.getText() + " " + authPasswordField.getText());


    }

    private void openWindow(String fxml, String title) throws IOException {
        //Закрываем текущее окно
        Stage stage = (Stage) authEnterButton.getScene().getWindow();
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
        } catch (IOException e) {
            ShowError("Невозможно подключиться к серверу");
        }
    }

    private void read() {
        try {
            while (true) {
                Message message = (Message) is.readObject();
                switch (message.getTypeMessage()) {
                    case TEXT_MESSAGE:
                        TextMessage textMessage = (TextMessage) message;
                        System.out.println("От сервера: " + textMessage.getTextMessage());
                        if (textMessage.getTextMessage().startsWith("/authOK")) {
                            openWindow("cloud.fxml", "Облачное хранилище");
                        }
                        break;
                    case FILE_MESSAGE:
                        break;
                    case LIST_MESSAGE:
                        break;
                }
                //authLoginField.setText(message);
              //  System.out.println("От сервера: " + message);
//                    if (message.startsWith("/authOK")) {
//                        openWindow("cloud.fxml", "Облачное хранилище");
//                    }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            log.error("Ошибка при отправке сообщений на сервер");
        }

    }

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
        //вывод сообщения об ошибке
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    public void registrationUser(ActionEvent actionEvent) throws IOException {
        //Закрываем текущее окно
        Stage stage = (Stage) authRegButton.getScene().getWindow();
        stage.close();

        //открываем окно
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("registration.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Регистрация нового пользователя");
        stage.setScene(new Scene(root1));
        stage.resizableProperty().set(false);
        stage.show();
    }
}
