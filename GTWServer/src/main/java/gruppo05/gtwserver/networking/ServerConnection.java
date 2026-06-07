package gruppo05.gtwserver.networking;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import gruppo05.gtwshared.networking.NetworkConnection;

/**
 * @brief Implementazione lato server della connessione di rete.
 * Estende NetworkConnection specificando che il server accetta
 * esattamente MAX_CLIENTS connessioni in ingresso tramite un
 * ServerSocket condiviso.
 */
public class ServerConnection extends NetworkConnection {

    // COSTANTI

    /**
     * @brief Numero di client attesi prima di avviare la partita.
     */
    public static final int MAX_CLIENTS = 2;

    // ATTRIBUTI

    /**
     * @brief Socket principale del server in ascolto sulla porta configurata.
     */
    private final ServerSocket serverSocket;

    /**
     * @brief Callback invocata nel momento in cui un client si connette.
     */
    private final Consumer<Integer> onClientConnected;

    // COSTRUTTORE

    /**
     * @brief Costruttore del server in ascolto sulla porta specificata.
     * @param[in] port                 Porta di ascolto.
     * @param[in] onReceive            Callback invocata ad ogni messaggio ricevuto.
     * @param[in] onClientConnected    Callback invocata quando un client completa la connessione.
     * @param[in] onClientDisconnected Callback invocata quando un client si disconnette.
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
     * @brief Accetta una connessione in ingresso dal ServerSocket.
     * @return La Socket del client appena connesso.
     */
    @Override
    protected Socket createSocket() throws IOException {
        return serverSocket.accept();
    }

    /**
     * @brief Restituisce il numero di canali attesi dal server.
     * @return Il valore di MAX_CLIENTS (2).
     */
    @Override
    protected int expectedChannels() {
        return MAX_CLIENTS;
    }

    /**
     * @brief Metodo invocato subito dopo che un client si è connesso.
     * @param[in] socket       La socket del client appena connesso.
     * @param[in] channelIndex L'indice del canale (0 o 1).
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
     * @brief Ferma il server chiudendo tutti i canali attivi e il ServerSocket.
     */
    public void stopServer() {
        disconnectAll();
        try {
            serverSocket.close();
            System.out.println("[ServerConnection] Server disconnesso.");
        } catch (IOException ignored) {}
    }

    /**
     * @brief Indica se entrambi i client sono connessi e la partita può iniziare.
     * @return true se i due canali sono attivi, false altrimenti.
     */
    public boolean areBothClientsConnected() {
        return areAllChannelsReady();
    }
}