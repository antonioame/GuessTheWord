package gruppo05.gtwclient;

import gruppo05.gtwclient.controller.ClientLoginManager;
import gruppo05.gtwclient.controller.ClientSignupManager;
import gruppo05.gtwshared.controller.SceneNavigator;
import gruppo05.gtwclient.networking.ClientConnection;
import gruppo05.gtwclient.networking.ClientConnectionCreator;
import gruppo05.gtwshared.controller.LoginViewController;
import gruppo05.gtwshared.networking.NetworkMessage;
import javafx.application.Application;
import javafx.application.Platform;
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
        SceneNavigator.init(stage);
        
        ClientConnectionCreator creator = new ClientConnectionCreator();
        connection = creator.createConnection();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/gruppo05/gtwshared/controller/LoginView.fxml"));
        
        Parent root = loader.load();
        LoginViewController ctrl = (LoginViewController) loader.getController();
        
        // RIPRISTINATI I COSTRUTTORI ORIGINALI A 1 PARAMETRO
        ClientLoginManager loginMgr  = new ClientLoginManager(connection);
        ClientSignupManager signupMgr = new ClientSignupManager(connection);
        ctrl.setLoginManager(loginMgr);
        ctrl.setSignupManager(signupMgr);
        
        // (Rimosse le chiamate a creator.setLoginViewController)
        
        // Registra il callback di disconnessione improvvisa del server:
        creator.setOnServerDisconnect(() -> {
            try {
                LoginViewController loginCtrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwshared/controller/LoginView.fxml");
                
                // RIPRISTINATI I COSTRUTTORI ORIGINALI A 1 PARAMETRO
                loginCtrl.setLoginManager(new ClientLoginManager(connection));
                loginCtrl.setSignupManager(new ClientSignupManager(connection));
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(event -> {
            try {
                if (connection != null) {
                    connection.send(new NetworkMessage.ClientDisconnect());
                    connection.disconnect();
                }
            } catch (IOException e) {
                System.out.println("Disconnessione client conclusa.");
            } finally {
                Platform.exit();
                System.exit(0);
            }
        });
        stage.show();
    }
    
    public static void main(String[] args) {
        launch();
    }

}