package gruppo05.gtwclient.controller;

import gruppo05.gtwclient.networking.ClientConnection;
import gruppo05.gtwshared.controller.SignupManager;
import gruppo05.gtwshared.controller.SignupViewController;
import gruppo05.gtwshared.networking.NetworkMessage;
import gruppo05.gtwshared.utility.SecurityUtils;
import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 *
 * @author francesco-vecchione
 */
public class ClientSignupManager implements SignupManager {
    
    private final ClientConnection conn;

    public ClientSignupManager(ClientConnection conn) {
        this.conn = conn;
    }
    
    @Override
    public void registerInfo(String username, String password) {
        String hashedPassword = SecurityUtils.hashPassword(password);
        NetworkMessage.RegisterRequest lr = new NetworkMessage.RegisterRequest(username, hashedPassword);

        try {
            conn.send(lr);
        } catch (IOException ex) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Impossibile contattare il server per la registrazione.");
                alert.setHeaderText("Errore di rete");
                alert.showAndWait();
                
                // Sblocca la UI usando l'istanza statica
                if (SignupViewController.instance != null) {
                    SignupViewController.instance.resetSignupButton();
                }
            });             
        }
    }
}