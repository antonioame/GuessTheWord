package gruppo05.gtwclient.controller;

import gruppo05.gtwclient.networking.ClientConnection;
import gruppo05.gtwshared.controller.LoginManager;
import gruppo05.gtwshared.controller.LoginViewController;
import gruppo05.gtwshared.networking.NetworkMessage;
import gruppo05.gtwshared.utility.SecurityUtils;
import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 *
 * @author francesco-vecchione
 */
public class ClientLoginManager implements LoginManager {

    private final ClientConnection conn;

    public ClientLoginManager(ClientConnection conn) {
        this.conn = conn;
    }
    
    @Override
    public void validateInfo(String username, String password) {
        String hashedPassword = SecurityUtils.hashPassword(password);
        NetworkMessage.LoginRequest lr = new NetworkMessage.LoginRequest(username, hashedPassword);
        conn.setUsername(username);

        try {
            conn.send(lr);
        } catch (IOException ex) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Impossibile inviare la richiesta di login. Connessione al server non disponibile.");
                alert.setHeaderText("Errore di rete");
                alert.showAndWait();
                
                // Sblocca la UI usando l'istanza statica
                if (LoginViewController.instance != null) {
                    LoginViewController.instance.resetLoginButton();
                }
            });
        }
    }
}