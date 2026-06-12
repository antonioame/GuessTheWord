package gruppo05.gtwserver.networking;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import gruppo05.gtwshared.networking.NetworkConnection;

/**
 * @class ServerConnection
 * @brief Implementazione lato server della connessione di rete.
 * @details Estende {@link NetworkConnection} specificando che il server accetta
 * esattamente {@link #MAX_CLIENTS} connessioni in ingresso tramite un
 * {@link ServerSocket} condiviso. Gestisce il ciclo di vita delle connessioni
 * in attesa dei due giocatori necessari per avviare una partita.
 * 
 * @author chiara
 * @version 1.0
 */
public class ServerConnection extends NetworkConnection {

    // COSTANTI

    /**
     * @brief Numero di client attesi prima di poter avviare la partita.
     * @details Il server accetterà ed elaborerà connessioni fino al raggiungimento di questo tetto.
     */
    public static final int MAX_CLIENTS = 2;

    // ATTRIBUTI

    /**
     * @brief Socket principale del server, in ascolto sulla porta configurata.
     * @details Ha il compito di accettare le richieste TCP in ingresso e generare i socket individuali.
     */
    private final ServerSocket serverSocket;

    /**
     * @brief Callback invocata nel momento esatto in cui un client stabilisce la connessione.
     * @details Utile per notificare la UI o i controller di gioco.
     * Non è final per consentire la registrazione tardiva da parte della dashboard (che parte dopo il login).
     */
    private Consumer<Integer> onClientConnected;

    // COSTRUTTORE

    /**
     * @brief Costruttore del server: apre la porta e prepara le funzioni di callback.
     * @details Inizializza il {@link ServerSocket} mettendolo in ascolto, ma non avvia 
     * immediatamente i thread di accettazione (questo avverrà chiamando il metodo connect() ereditato).
     * @param[in] port                 La porta TCP di ascolto.
     * @param[in] onReceive            Callback invocata ad ogni messaggio ricevuto (inoltrata al padre).
     * @param[in] onClientConnected    Callback invocata non appena un client completa l'handshake di connessione.
     * @param[in] onClientDisconnected Callback invocata quando un client si disconnette o cade (inoltrata al padre).
     * @throws IOException Se la porta specificata è già occupata o si verifica un errore di binding.
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
     * @brief Accetta fisicamente una connessione in ingresso dal ServerSocket.
     * @details Operazione bloccante: il thread di background resterà in attesa su questo 
     * metodo finché un nuovo client non cercherà di connettersi.
     * @return La {@link Socket} dedicata per comunicare con il client appena connesso.
     * @throws IOException Se avviene un problema durante l'operazione di accept() (es. socket chiuso).
     */
    @Override
    protected Socket createSocket() throws IOException {
        return serverSocket.accept();
    }

    /**
     * @brief Restituisce il numero di canali di comunicazione attesi dal server.
     * @return Il valore definito nella costante {@link #MAX_CLIENTS} (2).
     */
    @Override
    protected int expectedChannels() {
        return MAX_CLIENTS;
    }

    /**
     * @brief Metodo eseguito subito dopo l'instaurazione fisica della connessione con un client.
     * @details Registra a terminale l'IP del nuovo client connesso ed esegue la callback specifica.
     * @param[in] socket       La socket del client appena connesso.
     * @param[in] channelIndex L'indice identificativo univoco assegnato al canale (0 o 1).
     */
    @Override
    protected void onChannelReady(Socket socket, int channelIndex) {
        System.out.println("[ServerConnection] Client " + channelIndex
                + " connesso da " + socket.getInetAddress().getHostAddress());
        if (onClientConnected != null) {
            onClientConnected.accept(channelIndex);
        }
    }

    /**
     * @brief Registra (o sostituisce) la callback per gli eventi di nuova connessione client.
     * @details Consente alla dashboard di registrarsi come osservatore in un secondo momento,
     *          anche dopo che il server è già in ascolto e che i thread di accettazione sono partiti.
     * @param[in] onClientConnected Nuova callback da associare all'evento di connessione.
     */
    public void setOnClientConnected(Consumer<Integer> onClientConnected) {
        this.onClientConnected = onClientConnected;
    }

    // METODI DI SUPPORTO

    /**
     * @brief Ferma brutalmente e in modo sicuro l'intero server.
     * @details Sconnette preventivamente tutti i client tramite la chiusura forzata dei 
     * loro canali e successivamente abbatte il {@link ServerSocket} smettendo di accettare 
     * nuove richieste. Le eventuali eccezioni I/O di chiusura vengono ignorate silenziosamente.
     */
    public void stopServer() {
        disconnectAll();
        try {
            serverSocket.close();
            System.out.println("[ServerConnection] Server disconnesso.");
        } catch (IOException ignored) {}
    }

    /**
     * @brief Verifica rapida e semanticamente chiara dello stato della lobby.
     * @details Funge da wrapper di dominio per il metodo astratto {@code areAllChannelsReady()} ereditato,
     * rendendo il codice più leggibile.
     * @return true se esattamente i due canali previsti sono attivi, false altrimenti.
     */
    public boolean areBothClientsConnected() {
        return areAllChannelsReady();
    }
}