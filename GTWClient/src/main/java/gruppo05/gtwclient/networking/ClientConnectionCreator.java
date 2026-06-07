/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwclient.networking;

import gruppo05.gtwshared.networking.NetworkConfiguration;
import gruppo05.gtwshared.networking.NetworkConnectionCreator;
import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 * @author francesco-vecchione
 */
public class ClientConnectionCreator extends NetworkConnectionCreator {

    /* ON RECEIVE e ON DISCONNECT sono da implementare stesso in questa classe
    private final BiConsumer<Integer, Serializable> onReceive;
    private final Consumer<Integer> onDisconnect;

   
    public ClientConnectionCreator(
            BiConsumer<Integer, Serializable> onReceive,
            Consumer<Integer> onDisconnect) {
        this.onReceive = onReceive;
        this.onDisconnect = onDisconnect;
    }
    */

    
    @Override
    public ClientConnection createConnection() {
        // Legge il file client.properties
        NetworkConfiguration config = this.readConfiguration("client.properties");
        
        // Estrae le chiavi server.ip e server.port (es. 127.0.0.1 e 5000)
        /*
        return new ClientConnection(
                config.getIp(),
                config.getPort(),
                onReceive,
                onDisconnect
        );
        */
        return null;        // Debug
    }
}