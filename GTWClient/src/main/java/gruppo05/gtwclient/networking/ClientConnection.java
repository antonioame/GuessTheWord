package gruppo05.gtwclient.networking;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import gruppo05.gtwshared.networking.NetworkConnection;

/**
 * @brief Implementazione lato client della connessione di rete.
 * Specializza NetworkConnection per un singolo canale verso il server.
 */
public class ClientConnection extends NetworkConnection {

    // ATTRIBUTI

    /**
     * @brief Indirizzo IP del server a cui connettersi.
     */
    private final String serverIp;

    /**
     * @brief Porta del server.
     */
    private final int serverPort;

    // COSTRUTTORI
    
    /**
     * @brief Costruttore completo.
     * @param[in] serverIp     Indirizzo IP del server.
     * @param[in] serverPort   Porta del server.
     * @param[in] onReceive    Callback per ogni messaggio ricevuto.
     * @param[in] onDisconnect Callback alla chiusura della connessione.
     */
    public ClientConnection(String serverIp, int serverPort, 
                            BiConsumer<Integer, Serializable> onReceive,
                            Consumer<Integer> onDisconnect) {
        super(onReceive, onDisconnect);
        this.serverIp   = serverIp;
        this.serverPort = serverPort;
    }

    /**
     * @brief Costruttore senza callback di disconnessione.
     * @param[in] serverIp   Indirizzo IP del server.
     * @param[in] serverPort Porta del server.
     * @param[in] onReceive  Callback per la gestione dei messaggi in ingresso.
     */
    public ClientConnection(String serverIp, int serverPort, BiConsumer<Integer, Serializable> onReceive) {
        this(serverIp, serverPort, onReceive, null);
    }

    // IMPLEMENTAZIONE METODI

    /**
     * @brief Crea una Socket verso il server configurato.
     * @return Una socket connessa al server.
     */
    @Override
    protected Socket createSocket() throws IOException {
        System.out.println("[ClientConnection] Connessione a " + serverIp + ":" + serverPort);
        return new Socket(serverIp, serverPort);
    }

    /**
     * @brief Il client gestisce un solo canale verso il server.
     * @return Il valore fisso 1.
     */
    @Override
    protected int expectedChannels() {
        return 1;
    }

    // METODI DI SUPPORTO

    /**
     * @brief Invia un messaggio al server.
     * @param[in] data Oggetto serializzabile da inviare al server.
     */
    public void send(Serializable data) throws IOException {
        sendTo(0, data);
    }

    /**
     * @brief Chiude la connessione con il server.
     */
    public void disconnect() throws IOException {
        disconnectChannel(0);
    }
}