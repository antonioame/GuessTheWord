package gruppo05.gtwclient.controller;

import gruppo05.gtwclient.networking.ClientConnection;
import gruppo05.gtwshared.controller.SignupManager;
import gruppo05.gtwshared.networking.NetworkMessage;
import java.io.IOException;

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
    public void validateInfo(String username, String password) {
        NetworkMessage.RegisterRequest lr = new NetworkMessage.RegisterRequest(username, password);

        try {
            conn.send(lr);
        } catch (IOException ex) {
            // Debug: da cambiare
            ex.printStackTrace();             
        }
    }
    
}
