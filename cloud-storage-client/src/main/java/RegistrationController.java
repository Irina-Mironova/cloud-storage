import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegistrationController implements Initializable {
    public TextField regLastNameField;
    public TextField regNameField;
    public TextField regLoginField;
    public Button regRegistrButton;
    public Button regCloseButton;
    public TextField regEmailField;
    public PasswordField regPasswordField;
    public PasswordField regConfirmPasswordField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }

    public void registration(ActionEvent actionEvent) throws IOException {
        //Закрываем текущее окно
        Stage stage = (Stage) regRegistrButton.getScene().getWindow();
        stage.close();

        //открываем окно
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("authorization.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root1));
        stage.show();
    }
}
