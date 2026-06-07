/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwclient.networking;

import gruppo05.gtwshared.networking.NetworkConfiguration;
import gruppo05.gtwshared.networking.NetworkConnectionCreator;

/**
 *
 * @author francesco-vecchione
 */
public class ClientConnectionCreator extends NetworkConnectionCreator {

    @Override
    public ClientConnection createConnection() {
        
        NetworkConfiguration config = this.readConfiguration("client.properties");
        
        /* DA IMPLEMENTARE
        return new ClientConnection(
                config.getIp(),
                config.getPort(),
                
                );
        */
        
        return null;    // debug
    }
    
}
