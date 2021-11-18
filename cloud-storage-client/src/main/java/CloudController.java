import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.List;
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

    private Socket socket;
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    public String login;

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //подключение к серверу
        connect();
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
            Thread thread = new Thread(this::readCloud);
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Невозможно подключиться к серверу", ButtonType.OK).showAndWait();
        }
    }

    //обработка входящих сообщений
    private void readCloud() {
        try {
            while (true) {
                Message message = (Message) is.readObject();
                //обработка входящего сообщения в зависимости от его типа
                switch (message.getTypeMessage()) {
                    case TEXT_MESSAGE: //пришло текстовое сообщение
                        break;
                    case FILE_MESSAGE:
                        break;
                    case LIST_MESSAGE: //пришел список файлов
                        ListMessage listMessage = (ListMessage) message;
                        updateFileList(listMessage.getListFiles());
                        break;
                }

            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            log.error("Ошибка при отправке сообщений на сервер");
        }
    }

    // заполнение listView файлами из списка
    public void updateFileList(List<String> listFiles) {
        storServerView.getItems().clear();
        if (listFiles != null) {
            storServerView.getItems().addAll(listFiles);
        }
    }
}
