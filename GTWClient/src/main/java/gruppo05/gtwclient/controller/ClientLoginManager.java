package gruppo05.gtwclient.controller;

import gruppo05.gtwclient.networking.ClientConnection;
import gruppo05.gtwshared.controller.LoginManager;
import gruppo05.gtwshared.networking.NetworkMessage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        NetworkMessage.LoginRequest lr = new NetworkMessage.LoginRequest(username, password);

        try {
            conn.send(lr);
        } catch (IOException ex) {
            // Debug: da cambiare
            ex.printStackTrace();             
        }
    }
    
}
