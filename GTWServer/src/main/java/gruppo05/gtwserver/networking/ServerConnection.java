package gruppo05.gtwserver.networking;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import gruppo05.gtwshared.networking.NetworkConnection;

/**
 * Implementazione lato server della connessione di rete.
 *
 * <p>Estende {@link NetworkConnection} specificando che il server accetta
 * esattamente {@link #MAX_CLIENTS} connessioni in ingresso tramite un
 * {@link ServerSocket} condiviso.</p>
 *
 * <h2>Implementazione dei metodi astratti</h2>
 * <ul>
 *   <li>{@link #createSocket()} — chiama {@code serverSocket.accept()}, bloccando
 *       il thread di setup fino all'arrivo di un client. Poiché {@link #connect()}
 *       lancia i thread di setup in parallelo, le due {@code accept()} avvengono
 *       contemporaneamente e il server è pronto per entrambi i client sin dall'avvio.</li>
 *   <li>{@link #expectedChannels()} — restituisce {@link #MAX_CLIENTS} ({@code 2}).</li>
 *   <li>{@link #onChannelReady(Socket, int)} — sovrascrive il metodo opzionale per
 *       notificare il chiamante nel momento esatto in cui ogni client si connette,
 *       prima che il loop di lettura parta.</li>
 * </ul>
 *
 * <h2>Ciclo di vita</h2>
 * <ol>
 *   <li>Il costruttore apre il {@link ServerSocket} sulla porta configurata.</li>
 *   <li>{@link #connect()} avvia due thread di setup in parallelo, ognuno bloccato
 *       su {@code accept()}.</li>
 *   <li>Alla connessione di ogni client viene invocata {@code onClientConnected}
 *       e avviato il canale di lettura.</li>
 *   <li>Quando entrambi i client sono connessi, {@link #areBothClientsConnected()}
 *       diventa {@code true} e la partita può iniziare.</li>
 * </ol>
 *
 * <h2>Indici dei canali</h2>
 * <p>L'indice di canale (0 o 1) identifica univocamente ogni client in tutti
 * i metodi ereditati ({@link #sendTo}, {@link #broadcast}, {@link #broadcastExcept},
 * {@link #disconnectChannel}) e nelle callback.</p>
 *
 * @author chiara
 * @version 2.0
 * @see NetworkConnection
 * @see ClientConnection
 */
public class ServerConnection extends NetworkConnection {

    // COSTANTI

    /** Numero di client attesi prima di avviare la partita. */
    public static final int MAX_CLIENTS = 2;

    // ATTRIBUTI

    /**
     * Socket principale del server: resta in ascolto sulla porta configurata
     * e genera una {@link Socket} per ogni {@code accept()} andata a buon fine.
     * Viene aperto nel costruttore e chiuso da {@link #stopServer()}.
     */
    private final ServerSocket serverSocket;

    /**
     * Callback invocata nel momento esatto in cui un client si connette
     * (dopo {@code accept()}, prima dell'avvio del loop di lettura).
     * Il parametro è l'indice del canale (0 o 1).
     */
    private final Consumer<Integer> onClientConnected;

    // COSTRUTTORE

    /**
     * Costruttore.
     * Crea un {@code ServerConnection} aprendo il {@link ServerSocket} sulla porta
     * specificata. La connessione effettiva con i client inizia solo con {@link #connect()}.
     *
     * @param port                  Porta di ascolto.
     * @param onReceive             Callback {@code (clientIndex, message)} invocata ad
     *                              ogni messaggio ricevuto da qualsiasi client.
     * @param onClientConnected     Callback {@code (clientIndex)} invocata quando un
     *                              client completa la connessione TCP.
     * @param onClientDisconnected  Callback {@code (clientIndex)} invocata quando un
     *                              client si disconnette.
     * @throws IOException Se la porta è già occupata o il {@link ServerSocket} non
     *                     può essere creato.
     */
    public ServerConnection(int port,
                            BiConsumer<Integer, Serializable> onReceive,
                            Consumer<Integer> onClientConnected,
                            Consumer<Integer> onClientDisconnected) throws IOException {
        super(onReceive, onClientDisconnected);
        this.serverSocket      = new ServerSocket(port);
        this.onClientConnected = onClientConnected;
        System.out.println("[ServerConnection] In ascolto sulla porta " + port);
    }

    // IMPLEMENTAZIONE DEI METODI ASTRATTI DELLA SUPERCLASSE

    /**
     * Accetta una connessione in ingresso dal {@link ServerSocket}.
     *
     * <p>La chiamata è <em>bloccante</em>: il thread di setup chiamante rimane
     * fermo qui finché un client non si connette. Questo è il comportamento
     * desiderato: {@link NetworkConnection#connect()} lancia i thread di setup
     * in parallelo, quindi le due {@code accept()} per i due client avvengono
     * contemporaneamente.</p>
     *
     * @return La {@link Socket} del client appena connesso.
     * @throws IOException Se il {@link ServerSocket} è chiuso o si verifica un
     *                     errore durante l'attesa.
     */
    @Override
    protected Socket createSocket() throws IOException {
        return serverSocket.accept();
    }

    /**
     * Il server attende esattamente {@link #MAX_CLIENTS} client.
     *
     * @return {@code 2}.
     */
    @Override
    protected int expectedChannels() {
        return MAX_CLIENTS;
    }

    /**
     * Metodo invocato dalla classe base subito dopo che un client si è connesso
     * (la socket è pronta) e prima che il loop di lettura parta.
     *
     * <p>Qui viene invocata la callback {@code onClientConnected} in modo che il
     * controller del server possa aggiornare la UI o avviare la partita nel momento
     * preciso della connessione, non al primo messaggio ricevuto.</p>
     *
     * @param socket       La socket del client appena connesso.
     * @param channelIndex L'indice del canale (0 per il primo client, 1 per il secondo).
     */
    @Override
    protected void onChannelReady(Socket socket, int channelIndex) {
        System.out.println("[ServerConnection] Client " + channelIndex
                + " connesso da " + socket.getInetAddress().getHostAddress());
        if (onClientConnected != null) {
            onClientConnected.accept(channelIndex);
        }
    }

    // METODI DI SUPPORTO

    /**
     * Ferma il server chiudendo tutti i canali attivi e il {@link ServerSocket}.
     *
     * <p>Dopo questa chiamata il server non accetta più connessioni e tutti
     * i client vengono disconnessi.</p>
     */
    public void stopServer() {
        disconnectAll();
        try {
            serverSocket.close();
            System.out.println("[ServerConnection] Server disconnesso.");
        } catch (IOException ignored) {}
    }

    /**
     * Indica se entrambi i client sono connessi e la partita può iniziare.
     *
     * <p>Alias leggibile di {@link #areAllChannelsReady()} specifico per
     * il dominio del gioco.</p>
     *
     * @return {@code true} se i due canali sono attivi.
     */
    public boolean areBothClientsConnected() {
        return areAllChannelsReady();
    }
}
