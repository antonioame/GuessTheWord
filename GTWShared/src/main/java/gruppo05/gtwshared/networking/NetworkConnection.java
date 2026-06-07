package gruppo05.gtwshared.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @brief Classe astratta che modella una connessione di rete generica.
 */
public abstract class NetworkConnection {
    // ATTRIBUTI
    
    /**
     * @brief Lista thread-safe dei canali di comunicazione attivi.
     */
    private final List<ConnectionThread> threads = Collections.synchronizedList(new ArrayList<>());

    /**
     * @brief Callback invocata ad ogni messaggio ricevuto su qualsiasi canale.
     */
    private final BiConsumer<Integer, Serializable> onReceive;

    /**
     * @brief Callback invocata quando un canale si chiude.
     */
    private final Consumer<Integer> onDisconnect;

    // COSTRUTTORI
    
    /**
     * @brief Costruttore completo.
     * @param[in] onReceive    Callback invocata ad ogni messaggio.
     * @param[in] onDisconnect Callback invocata alla chiusura di un canale.
     */
    protected NetworkConnection(BiConsumer<Integer, Serializable> onReceive, Consumer<Integer> onDisconnect) {
        this.onReceive    = onReceive;
        this.onDisconnect = onDisconnect;
    }

    /**
     * @brief Costruttore senza callback di disconnessione.
     * @param[in] onReceive Callback invocata ad ogni messaggio.
     */
    protected NetworkConnection(BiConsumer<Integer, Serializable> onReceive) {
        this(onReceive, null);
    }

    // METODI ASTRATTI (devono essere implementati nelle sottoclassi)
    /**
     * @brief Metodo che crea e restituisce una socket pronta per la comunicazione.
     * @return Una socket connessa e pronta all'uso.
     */
    protected abstract Socket createSocket() throws IOException;

    /**
     * @brief Restituisce il numero di canali di comunicazione da aprire.
     * @return Numero di canali da aprire.
     */
    protected abstract int expectedChannels();

    // GESTIONE DELLA CONNESSIONE
    
    /**
     * @brief Avvia l'apertura di tutti i canali di comunicazione necessari.
     */
    public void connect() {

        // Numero di canali che devono essere aperti (definito dalle sottoclassi)
        int n = expectedChannels();

        // Ciclo per creare tutti i canali richiesti
        for (int i = 0; i < n; i++) {
            // Serve per usare il valore di i dentro il thread in modo sicuro
            final int channelIndex = i;

            // Creazione di un thread separato per gestire l'apertura del canale
            Thread setupThread = new Thread(() -> {
                try {
                    // Creazione della socket (logica definita dalla sottoclasse)
                    Socket socket = createSocket();

                    // Eventuale operazione aggiuntiva subito dopo la connessione
                    onChannelReady(socket, channelIndex);

                    // Avvio del canale di comunicazione vero e proprio
                    startChannel(socket, channelIndex);

                } catch (IOException ex) {
                    // Messaggio di errore in caso di problemi durante la connessione
                    System.err.println("[NetworkConnection] Errore apertura canale "
                            + channelIndex + ": " + ex.getMessage());

                    // Se è definito un gestore di disconnessione, viene notificato
                    if (onDisconnect != null)
                        onDisconnect.accept(channelIndex);
                }
            }, "SetupChannel-" + channelIndex); // nome del thread per debugging

            // Il thread viene impostato come daemon:
            // termina automaticamente quando termina l'applicazione
            setupThread.setDaemon(true);

            // Avvio del thread di setup
            setupThread.start();
        }
    }


    // METODO DI UTILITA'

    /**
     * @brief Metodo eseguito subito dopo l'apertura di una socket.
     * @param[in] socket       Socket appena aperta.
     * @param[in] channelIndex Indice del canale associato alla socket.
     */
    protected void onChannelReady(Socket socket, int channelIndex) {
        // Nessuna operazione di default
    }
    
    // GESTIONE INTERNA DEI CANALI

    /**
     * @brief Crea, registra nella lista e avvia un ConnectionThread.
     * @param[in] socket       Socket connessa su cui leggere/scrivere.
     * @param[in] channelIndex Indice del canale.
     */
    protected final void startChannel(Socket socket, int channelIndex) {
        // 1. CREAZIONE DEL THREAD
        // Istanzazione di un nuovo Thread dedicato esclusivamente a questo client.
        ConnectionThread ct = new ConnectionThread(socket, channelIndex);
        
        // 2. REGISTRAZIONE DEL THREAD
        // Salvo il riferimento di questo thread in una lista gestita dal server.
        // Serve al server per mantenere il controllo della comunicazione.
        threads.add(ct);
        
        // 3. IMPOSTAZIONE DEMONE (BACKGROUND)
        // Segnala alla JVM che questo è un thread di "servizio".
        // Se il programma server principale (main) viene terminato o si chiude, 
        // questo thread verrà spento istantaneamente in automatico, evitando che 
        // il processo rimanga bloccato (appeso) in memoria.
        ct.setDaemon(true);

        // 4. ESECUZIONE IN PARALLELO
        // Il metodo .start() accende fisicamente il thread. Da questo preciso istante, 
        // il codice contenuto nel metodo run() di ConnectionThread girerà in parallelo, 
        // liberando il server principale che potrà subito fare altre operazioni.
        ct.start();
    }

    // API PUBBLICA

    /**
     * @brief Invia un oggetto serializzabile sul canale identificato.
     * @param[in] channelIndex Indice del canale destinatario.
     * @param[in] data         Oggetto da inviare.
     */
    public void sendTo(int channelIndex, Serializable data) throws IOException {
        getThread(channelIndex).send(data);
    }

    /**
     * @brief Invia un messaggio su tutti i canali attivi (broadcast).
     * @param[in] data Oggetto da trasmettere a tutti i canali.
     */
    public void broadcast(Serializable data) {
        synchronized (threads) {
            for (ConnectionThread ct : threads) {
                try {
                    ct.send(data);
                } catch (IOException ex) {
                    System.err.println("[NetworkConnection] Broadcast fallito su canale "
                            + ct.channelIndex + ": " + ex.getMessage());
                }
            }
        }
    }

    /**
     * @brief Invia un messaggio a tutti i canali tranne quello specificato.
     * @param[in] excludedIndex Indice del canale da escludere.
     * @param[in] data          Oggetto da inviare.
     */
    public void broadcastExcept(int excludedIndex, Serializable data) {
        synchronized (threads) {
            for (ConnectionThread ct : threads) {
                if (ct.channelIndex != excludedIndex) {
                    try {
                        ct.send(data);
                    } catch (IOException ex) {
                        System.err.println("[NetworkConnection] Invio fallito su canale "
                                + ct.channelIndex + ": " + ex.getMessage());
                    }
                }
            }
        }
    }

    /**
     * @brief Chiude il canale identificato da channelIndex.
     * @param[in] channelIndex Indice del canale da chiudere.
     */
    public void disconnectChannel(int channelIndex) throws IOException {
        getThread(channelIndex).closeSocket();
    }

    /**
     * @brief Chiude tutti i canali attivi.
     */
    public void disconnectAll() {
        synchronized (threads) {
            for (ConnectionThread ct : new ArrayList<>(threads)) {
                try { 
                    ct.closeSocket(); 
                } catch (IOException ignored) {}
            }
        }
    }

    /**
     * @brief Restituisce il numero di canali attualmente attivi.
     * @return Numero di canali attivi.
     */
    public int getActiveChannelCount() {
        return threads.size();
    }

    /**
     * @brief Verifica se tutti i canali attesi sono attivi.
     * @return true se i canali attivi sono pari al numero atteso.
     */
    public boolean areAllChannelsReady() {
        return threads.size() == expectedChannels();
    }

    // METODO HELPER

    /**
     * @brief Recupera il ConnectionThread tramite indice di canale.
     * @param[in] channelIndex Indice del canale.
     * @return Il thread di connessione corrispondente.
     */
    private ConnectionThread getThread(int channelIndex) {
        synchronized (threads) {
            return threads.stream()
                    .filter(ct -> ct.channelIndex == channelIndex)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Nessun canale attivo con indice " + channelIndex));
        }
    }

    // CLASSE INNESTATA (INTERNA)

    /**
     * @brief Thread dedicato alla comunicazione su un singolo canale (socket).
     */
    class ConnectionThread extends Thread {

        // ATTRIBUTI 
        
        /**
         * @brief Indice del canale gestito da questo thread.
         */
        final int channelIndex;

        /**
         * @brief Socket del canale.
         */
        private final Socket socket;

        /**
         * @brief Stream di output verso il canale remoto.
         */
        private ObjectOutputStream oos;

        // COSTRUTTORE
        
        /**
         * @brief Costruttore.
         * @param[in] socket       Socket già connessa.
         * @param[in] channelIndex Indice del canale.
         */
        ConnectionThread(Socket socket, int channelIndex) {
            super("ConnectionChannel-" + channelIndex); // Serve per dare nomi identificativi diversi.
            this.socket       = socket;
            this.channelIndex = channelIndex;
        }

        /**
         * @brief Invia un oggetto serializzabile su questo canale.
         * @param[in] data Oggetto da inviare.
         * @pre
         * Lo stream è stato inizializzato
         */
        void send(Serializable data) throws IOException {
            if (oos == null) throw new IOException("Stream non ancora inizializzato.");
            oos.writeObject(data);
            oos.flush();  // flush esplicito: evita che messaggi brevi restino nel buffer
        }

        /**
         * @brief Chiude la socket di questo canale, interrompendo il loop di lettura.
         */
        void closeSocket() throws IOException {
            socket.close();
        }

        /**
         * @brief Corpo del thread di lettura.
         */
        @Override
        public void run() {
            try (
                // ORDINE OBBLIGATORIO: OOS prima di OIS
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream  ois = new ObjectInputStream(socket.getInputStream())
            ) {
                this.oos = oos;

                System.out.println("[NetworkConnection] Canale " + channelIndex
                        + " attivo -> " + socket.getInetAddress().getHostAddress()
                        + ":" + socket.getPort());

                // Loop bloccante: readObject() attende il prossimo messaggio
                while (!socket.isClosed()) {
                    Serializable msg = (Serializable) ois.readObject();
                    onReceive.accept(channelIndex, msg);
                }

            } catch (IOException ex) {
                // Chiusura normale (disconnectChannel) o perdita di connessione
                System.out.println("[NetworkConnection] Canale " + channelIndex
                        + " chiuso: " + ex.getMessage());
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } finally {
                threads.remove(this);
                if (onDisconnect != null) onDisconnect.accept(channelIndex);
            }
        }
    }
}