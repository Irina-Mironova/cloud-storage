import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;


import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
   
    public TextField loginField;
    public PasswordField passwordField;

    public Button registrationButton;
    public Button inputButton;
    public Pane panelLogin;
    public Pane panelFiles;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
       panelLogin.setVisible(false);
       //panelLogin.setVisible(false);

    }
}
