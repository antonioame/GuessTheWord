package gruppo05.gtwclient.networking;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import gruppo05.gtwshared.networking.NetworkConnection;

/**
 * @class ClientConnection
 * @brief Implementazione lato client della connessione di rete.
 * @details Specializza {@link NetworkConnection} fornendo la logica per instaurare 
 * e gestire un singolo canale di comunicazione bidirezionale verso il server di gioco. 
 * Nasconde la complessità della gestione multi-canale del padre, fissando 
 * implicitamente tutte le comunicazioni sull'indice di canale 0.
 * 
 * @author chiara
 * @version 2.0
 */
public class ClientConnection extends NetworkConnection {

    // ATTRIBUTI

    /**
     * @brief Indirizzo IP o hostname del server a cui connettersi.
     * @details Può essere un indirizzo IPv4 locale, remoto o il classico "localhost".
     */
    private final String serverIp;

    /**
     * @brief Porta TCP esposta dal server.
     * @details Deve corrispondere esattamente alla porta su cui l'istanza di ServerConnection è in ascolto.
     */
    private final int serverPort;

    // COSTRUTTORI
    
    /**
     * @brief Costruttore completo per inizializzare il modulo di rete del client.
     * @details Prepara i parametri di connessione e le callback, ma non avvia 
     * immediatamente la comunicazione. Per connettersi fisicamente è necessario 
     * invocare il metodo ereditato {@link #connect()}.
     * @param[in] serverIp     L'indirizzo IP del server.
     * @param[in] serverPort   La porta del server.
     * @param[in] onReceive    Callback invocata dinamicamente ad ogni pacchetto ricevuto.
     * @param[in] onDisconnect Callback di emergenza/pulizia invocata in caso di caduta della connessione.
     */
    public ClientConnection(String serverIp, int serverPort, 
                            BiConsumer<Integer, Serializable> onReceive,
                            Consumer<Integer> onDisconnect) {
        super(onReceive, onDisconnect);
        this.serverIp   = serverIp;
        this.serverPort = serverPort;
    }

    /**
     * @brief Costruttore semplificato sprovvisto di listener per la disconnessione.
     * @param[in] serverIp   L'indirizzo IP del server.
     * @param[in] serverPort La porta del server.
     * @param[in] onReceive  Callback per la gestione dei messaggi in ingresso.
     */
    public ClientConnection(String serverIp, int serverPort, BiConsumer<Integer, Serializable> onReceive) {
        this(serverIp, serverPort, onReceive, null);
    }

    // IMPLEMENTAZIONE METODI

    /**
     * @brief Instanzia e avvia attivamente una connessione TCP verso il server.
     * @details Questo metodo viene invocato automaticamente in modo asincrono dal thread 
     * di setup di {@link NetworkConnection#connect()}. A differenza del server che 
     * attende passivamente, il client lancia un handshake di rete esplicito.
     * @return Una {@link Socket} validata, aperta e collegata al server.
     * @throws IOException Se il server risulta offline, irraggiungibile, o la connessione viene rifiutata.
     */
    @Override
    protected Socket createSocket() throws IOException {
        System.out.println("[ClientConnection] Connessione a " + serverIp + ":" + serverPort);
        return new Socket(serverIp, serverPort);
    }

    /**
     * @brief Indica il numero di canali di rete che questa classe deve inizializzare.
     * @details Poiché un client comunica esclusivamente in un rapporto 1-a-1 con il 
     * server di gioco (e mai P2P con gli altri client), il valore atteso è costantemente 1.
     * @return Il valore intero fisso 1.
     */
    @Override
    protected int expectedChannels() {
        return 1;
    }

    // METODI DI SUPPORTO

    /**
     * @brief Inoltra un oggetto serializzato verso il server.
     * @details Funge da wrapper di comodità sul metodo ereditato 
     * {@link NetworkConnection#sendTo}, eliminando l'obbligo di passare 
     * l'indice di canale (che per il client è sempre e solo 0).
     * @param[in] data L'oggetto di trasporto (di norma una sottoclasse di NetworkMessage).
     * @throws IOException Se lo stream di rete non è pronto, è collassato o la scrittura fallisce.
     */
    public void send(Serializable data) throws IOException {
        sendTo(0, data);
    }

    /**
     * @brief Arresta e distrugge volontariamente il collegamento con il server.
     * @details Chiude in modo pulito il canale associato all'indice 0, causando 
     * l'attivazione a catena della callback di disconnessione su entrambi gli endpoint.
     * @throws IOException Se si manifestano problemi a livello I/O durante la chiusura fisica dei socket.
     */
    public void disconnect() throws IOException {
        disconnectChannel(0);
    }

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}