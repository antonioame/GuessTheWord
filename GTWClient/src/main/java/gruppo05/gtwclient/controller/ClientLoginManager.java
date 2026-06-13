package gruppo05.gtwclient.controller;

import gruppo05.gtwclient.networking.ClientConnection;
import gruppo05.gtwshared.controller.LoginManager;
import gruppo05.gtwshared.controller.LoginViewController;
import gruppo05.gtwshared.networking.NetworkMessage;
import gruppo05.gtwshared.utility.SecurityUtils;
import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.control.Alert;

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
            
        } catch (IllegalArgumentException ex) {
            // IL SERVER E' OFFLINE: Nessun canale attivo
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Attendi che la connessione venga stabilita in background prima di effettuare il login.");
                alert.setHeaderText("Server non raggiungibile");
                alert.show(); // Usiamo show() per non bloccare il thread
                
                // Sblocca la UI usando l'istanza statica
                if (LoginViewController.instance != null) {
                    LoginViewController.instance.resetLoginButton();
                }
            });
            
        } catch (IOException ex) {
            // ERRORE DI RETE IMPREVISTO DURANTE L'INVIO
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Impossibile inviare la richiesta di login. Connessione interrotta.");
                alert.setHeaderText("Errore di rete");
                alert.showAndWait();
                
                if (LoginViewController.instance != null) {
                    LoginViewController.instance.resetLoginButton();
                }
            });
        }
    }
}