package gruppo05.gtwclient.controller;

import gruppo05.gtwclient.networking.ClientConnection;
import gruppo05.gtwshared.controller.LoginManager;
import gruppo05.gtwshared.networking.NetworkMessage;
import gruppo05.gtwshared.utility.SecurityUtils;
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
        String hashedPassword = SecurityUtils.hashPassword(password);
        NetworkMessage.LoginRequest lr = new NetworkMessage.LoginRequest(username, hashedPassword);
        conn.setUsername(username);

        try {
            conn.send(lr);
        } catch (IOException ex) {
            // Debug: da cambiare
            ex.printStackTrace();             
        }
    }
    
}
