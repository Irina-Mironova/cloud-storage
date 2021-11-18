import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent parent = FXMLLoader.load(getClass().getResource("authorization.fxml"));
        primaryStage.setScene(new Scene(parent));
        primaryStage.resizableProperty().set(false);
        primaryStage.setTitle("Облачное хранилище");
        AuthController authController = fxmlLoader.getController();
        primaryStage.setOnCloseRequest(event -> authController.authCloseConnection());
        primaryStage.show();
    }
}
