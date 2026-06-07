package gruppo05.gtwclient;

import gruppo05.gtwclient.networking.ClientConnection;
import gruppo05.gtwclient.networking.ClientConnectionCreator;
import gruppo05.gtwshared.controller.LoginViewController;
import gruppo05.gtwshared.networking.NetworkConnection;
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
    
    private ClientConnection connection;
    
    @Override
    public void start(Stage stage) throws IOException {
        
        connection = new ClientConnectionCreator().createConnection();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/gruppo05/gtwshared/controller/LoginView.fxml"));
        
        Parent root = loader.load();
        LoginViewController ctrl = (LoginViewController) loader.getController();
        ctrl.setOnConfirmRoute("route per la main view");
        
        stage.setScene(new Scene(root));
        stage.show();
    }
    
    public static void main(String[] args) {
        launch();
    }

}