package gruppo05.gtwserver;

import gruppo05.gtwserver.controller.ServerLoginManager;
import gruppo05.gtwserver.controller.ServerSignupManager;
import gruppo05.gtwserver.networking.ServerConnection;
import gruppo05.gtwserver.networking.ServerConnectionCreator;
import gruppo05.gtwshared.controller.LoginViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private ServerConnection connection;
    
    @Override
    public void start(Stage stage) throws IOException {     
        
        connection = new ServerConnectionCreator().createConnection();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/gruppo05/gtwshared/controller/LoginView.fxml"));
        
        Parent root = loader.load();
        LoginViewController ctrl = (LoginViewController) loader.getController();
        ctrl.setLoginManager(new ServerLoginManager());
        ctrl.setSignupManager(new ServerSignupManager());
        
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}