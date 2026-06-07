package gruppo05.gtwserver.networking;

import gruppo05.gtwshared.networking.NetworkConfiguration;
import gruppo05.gtwshared.networking.NetworkConnectionCreator;

/**
 *
 * @author francesco-vecchione
 */
public class ServerConnectionCreator extends NetworkConnectionCreator {

    @Override
    public ServerConnection createConnection() {
        NetworkConfiguration config = this.readConfiguration("server.properties");
        
        /* DA IMPLEMENTARE
        return new ServerConnection(
                config.getPort(),
                
                );
        */
        
        return null;    // debug
    }
}
