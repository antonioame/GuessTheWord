package gruppo05.gtwserver.networking;

import gruppo05.gtwshared.networking.NetworkConfiguration;
import gruppo05.gtwshared.networking.NetworkConnectionCreator;
import java.io.IOException;
import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 * @author francesco-vecchione
 */
public class ServerConnectionCreator extends NetworkConnectionCreator {

    private final BiConsumer<Integer, Serializable> onReceive;
    private final Consumer<Integer> onClientConnected;
    private final Consumer<Integer> onClientDisconnected;

    
    public ServerConnectionCreator(
            BiConsumer<Integer, Serializable> onReceive,
            Consumer<Integer> onClientConnected,
            Consumer<Integer> onClientDisconnected) {
        this.onReceive = onReceive;
        this.onClientConnected = onClientConnected;
        this.onClientDisconnected = onClientDisconnected;
    }
    
    @Override
    public ServerConnection createConnection() {
        // Legge il file server.properties
        NetworkConfiguration config = this.readConfiguration("server.properties");
        
        try {
            // Estrae la chiave server.port (es. 5000) e istanzia la connessione
            return new ServerConnection(
                    config.getPort(),
                    onReceive,
                    onClientConnected,
                    onClientDisconnected
            );
        } catch (IOException e) {
            throw new RuntimeException("Errore durante la creazione del ServerSocket sulla porta " + config.getPort(), e);
        }
    }
}