package gruppo05.gtwclient.networking;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import gruppo05.gtwshared.networking.NetworkConnection;

/**
 * Implementazione lato client della connessione di rete.
 *
 * <brief>
 * <p>Specializza {@link NetworkConnection} per un singolo canale verso il server.
 * Implementa i due metodi astratti:</p>
 * <ul>
 *   <li>{@link #createSocket()} — crea una {@link Socket} verso {@code serverIp:serverPort}.</li>
 *   <li>{@link #expectedChannels()} — restituisce {@code 1} (un solo canale).</li>
 * </ul>
 *
 * <p>La callback {@code onReceive} riceverà sempre {@code channelIndex = 0},
 * che può essere ignorato lato client dato che il canale è uno solo.</p>
 * <\brief>
 *
 * @author chiara
 * @version 2.0
 * @see NetworkConnection
 * @see ServerConnection
 */
public class ClientConnection extends NetworkConnection {

    // ATTRIBUTI

    /** Indirizzo IP del server a cui connettersi. */
    private final String serverIp;

    /** Porta del server. */
    private final int serverPort;

    // COSTRUTTORI
    
    /**
     * Costruttore completo.
     *
     * @param serverIp     Indirizzo IP del server.
     * @param serverPort   Porta del server.
     * @param onReceive    Callback {@code (channelIndex=0, message)} per ogni messaggio.
     * @param onDisconnect Callback {@code (channelIndex=0)} alla chiusura della connessione.
     */
    public ClientConnection(String serverIp, int serverPort, 
                            BiConsumer<Integer, Serializable> onReceive,
                            Consumer<Integer> onDisconnect) {
        super(onReceive, onDisconnect);
        this.serverIp   = serverIp;
        this.serverPort = serverPort;
    }

    /**
     * Costruttore senza callback di disconnessione.
     *
     * @param serverIp   Indirizzo IP del server.
     * @param serverPort Porta del server.
     * @param onReceive  Callback per la gestione dei messaggi in ingresso.
     */
    public ClientConnection(String serverIp, int serverPort, BiConsumer<Integer, Serializable> onReceive) {
        this(serverIp, serverPort, onReceive, null);
    }

    // IMPLEMENTAZIONE METODI

    /**
     * Crea una {@link Socket} verso il server configurato.
     *
     * <p>Implementazione del metodo astratto della superclasse: 
     * il client apre attivamente una connessione
     * TCP verso {@code serverIp:serverPort}.</p>
     *
     * @return Una socket connessa al server.
     * @throws IOException Se la connessione al server fallisce (server assente,
     *                     porta errata, rete non raggiungibile).
     */
    @Override
    protected Socket createSocket() throws IOException {
        System.out.println("[ClientConnection] Connessione a " + serverIp + ":" + serverPort);
        return new Socket(serverIp, serverPort);
    }

    /**
     * Il client gestisce un solo canale verso il server.
     *
     * @return {@code 1} sempre.
     */
    @Override
    protected int expectedChannels() {
        return 1;
    }

    // METODI DI SUPPORTO

    /**
     * Invia un messaggio al server (canale 0).
     *
     * <p>Alias di {@code sendTo(0, data)}: nasconde l'indice di canale (sempre 0)
     * al codice client, rendendo le chiamate più leggibili.</p>
     *
     * @param data         Oggetto serializzabile da inviare al server.
     * @throws IOException Se la scrittura fallisce o la connessione è chiusa.
     */
    public void send(Serializable data) throws IOException {
        sendTo(0, data);
    }

    /**
     * Chiude la connessione con il server.
     *
     * <p>Alias di {@code disconnectChannel(0)}.</p>
     *
     * @throws IOException Se la chiusura fallisce.
     */
    public void disconnect() throws IOException {
        disconnectChannel(0);
    }
}
