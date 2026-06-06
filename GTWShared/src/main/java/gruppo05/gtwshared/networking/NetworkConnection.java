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
 * Classe astratta che modella una connessione di rete generica.
 *
 * <brief>
 * <p>Gestisce un insieme di canali di comunicazione ({@link ConnectionThread}),
 * ognuno associato a una {@link Socket} distinta e a un indice numerico progressivo.
 * Il numero di canali e la modalità di ottenimento delle socket sono delegati
 * alle sottoclassi tramite i due metodi astratti {@link #createSocket()} e
 * {@link #expectedChannels()}.</p>
 * <\brief>
 * 
 * <h2>Pattern di progettazione</h2>
 * <ul>
 *   <li><b>Template Method</b> – {@link #connect()} definisce lo scheletro
 *       dell'algoritmo di avvio (thread di setup → createSocket → ConnectionThread).
 *       Le sottoclassi possono sovrascrivere {@code connect()} per intercalare
 *       logica propria (es. notifica di connessione in {@link ServerConnection}).</li>
 *   <li><b>Strategy</b> – il comportamento alla ricezione di un messaggio è
 *       iniettato come {@code BiConsumer<Integer, Serializable>}: il primo
 *       parametro è l'indice del canale, il secondo è il messaggio ricevuto.</li>
 * </ul>
 *
 * <h2>Ordine critico degli stream</h2>
 * <p>{@link ObjectOutputStream} va sempre aperto <em>prima</em> di
 * {@link ObjectInputStream}: il costruttore di OIS è bloccante e attende
 * l'header scritto da OOS. Invertire l'ordine su entrambi i capi causa
 * un deadlock.</p>
 *
 * <h2>Thread safety</h2>
 * <p>La lista {@code threads} è avvolta in {@link Collections#synchronizedList}.
 * Tutti gli accessi iterativi vengono sincronizzati esplicitamente sul monitor
 * della lista.</p>
 *
 * @author chiara
 * @version 2.0
 */
public abstract class NetworkConnection {
    // ATTRIBUTI
    
    /**
     * Lista thread-safe dei canali di comunicazione attivi.
     * L'indice di posizione nella lista non coincide necessariamente con
     * {@link ConnectionThread#channelIndex}; bisogna usare sempre {@code channelIndex}
     * per identificare un canale specifico.
     */
    private final List<ConnectionThread> threads = Collections.synchronizedList(new ArrayList<>());

    /**
     * Callback invocata ad ogni messaggio ricevuto su qualsiasi canale.
     * <ul>
     *   <li>Primo parametro: indice del canale mittente (0-based).</li>
     *   <li>Secondo parametro: messaggio deserializzato.</li>
     * </ul>
     */
    private final BiConsumer<Integer, Serializable> onReceive;

    /**
     * Callback invocata quando un canale si chiude (normalmente o per errore).
     * Il parametro è l'indice del canale chiuso. Può essere {@code null}.
     */
    private final Consumer<Integer> onDisconnect;

    // COSTRUTTORI
    
    /**
     * Costruttore completo.
     *
     * @param onReceive    Callback {@code (channelIndex, message)} invocata ad ogni messaggio.
     * @param onDisconnect Callback {@code (channelIndex)} invocata alla chiusura di un canale
     *                     (può essere {@code null}).
     */
    protected NetworkConnection(BiConsumer<Integer, Serializable> onReceive, Consumer<Integer> onDisconnect) {
        this.onReceive    = onReceive;
        this.onDisconnect = onDisconnect;
    }

    /**
     * Costruttore senza callback di disconnessione.
     *
     * @param onReceive Callback {@code (channelIndex, message)} invocata ad ogni messaggio.
     */
    protected NetworkConnection(BiConsumer<Integer, Serializable> onReceive) {
        this(onReceive, null);
    }

    // METODI ASTRATTI (devono essere implementati nelle sottoclassi)
    /**
     * Metodo che crea e restituisce una socket pronta per la comunicazione.
     *
     * La classe base non conosce il modo in cui la connessione viene stabilita:
     * ogni sottoclasse fornisce la propria implementazione.
     *
     * Ad esempio:
     * - ClientConnection crea una nuova Socket verso il server.
     * - ServerConnection accetta una connessione tramite accept().
     *
     * @return una socket connessa e pronta all'uso.
     * @throws IOException se si verifica un errore durante la connessione.
     */
    protected abstract Socket createSocket() throws IOException;

    /**
     * Restituisce il numero di canali di comunicazione da aprire.
     *
     * Questo valore viene utilizzato dal metodo connect() per stabilire
     * quante connessioni devono essere create.
     *
     * @return numero di canali da aprire.
     */
    protected abstract int expectedChannels();

    // GESTIONE DELLA CONNESSIONE
    
    /**
     * Avvia l'apertura di tutti i canali di comunicazione necessari.
     *
     * Per ogni canale viene creato un thread dedicato che:
     * 1. Ottiene una socket tramite createSocket().
     * 2. Esegue eventuali operazioni aggiuntive tramite onChannelReady().
     * 3. Avvia il relativo ConnectionThread.
     *
     * L'utilizzo di thread separati evita che eventuali operazioni bloccanti,
     * come accept(), impediscano il corretto funzionamento dell'applicazione.
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
     * Metodo eseguito subito dopo l'apertura di una socket e prima
     * dell'avvio del relativo ConnectionThread.
     *
     * L'implementazione predefinita non esegue alcuna operazione.
     * Le sottoclassi possono ridefinire questo metodo per eseguire
     * attività specifiche al momento della connessione.
     *
     * @param socket socket appena aperta.
     * @param channelIndex indice del canale associato alla socket.
     */
    protected void onChannelReady(Socket socket, int channelIndex) {
        // Nessuna operazione di default
    }
    
    // GESTIONE INTERNA DEI CANALI

    /**
     * Crea, registra nella lista e avvia un {@link ConnectionThread} per la socket
     * e l'indice di canale specificati.
     * Avvia un nuovo canale di comunicazione indipendente per un client connesso.
     *
     * <p>Metodo {@code protected} per consentire alle sottoclassi di avviare canali
     * aggiuntivi se necessario, senza esporre la lista {@code threads} direttamente.</p>
     *
     * @param socket       Socket connessa su cui leggere/scrivere.
     * @param channelIndex Indice del canale.
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
     * Invia un oggetto serializzabile sul canale identificato da {@code channelIndex}.
     *
     * @param channelIndex              Indice del canale destinatario (0-based).
     * @param data                      Oggetto da inviare; deve implementare {@link Serializable}.
     * @throws IOException              Se la scrittura fallisce o il canale è chiuso.
     * @throws IllegalArgumentException Se non esiste un canale con quell'indice.
     */
    public void sendTo(int channelIndex, Serializable data) throws IOException {
        getThread(channelIndex).send(data);
    }

    /**
     * Invia un messaggio su tutti i canali attivi (broadcast).
     *
     * <p>Gli errori su singoli canali vengono loggati ma non propagati,
     * così gli altri canali ricevono comunque il messaggio.</p>
     *
     * @param data Oggetto da trasmettere a tutti i canali.
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
     * Invia un messaggio a tutti i canali tranne quello specificato.
     *
     * @param excludedIndex Indice del canale da escludere.
     * @param data          Oggetto da inviare.
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
     * Chiude il canale identificato da {@code channelIndex}.
     *
     * @param channelIndex   Indice del canale da chiudere.
     * @throws IOException   Se la chiusura della socket fallisce.
     */
    public void disconnectChannel(int channelIndex) throws IOException {
        getThread(channelIndex).closeSocket();
    }

    /**
     * Chiude tutti i canali attivi.
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
     * Restituisce il numero di canali attualmente attivi.
     *
     * @return Numero di {@link ConnectionThread} in esecuzione.
     */
    public int getActiveChannelCount() {
        return threads.size();
    }

    /**
     * Verifica se tutti i canali attesi sono attivi.
     *
     * @return {@code true} se i canali attivi sono esattamente {@link #expectedChannels()}.
     */
    public boolean areAllChannelsReady() {
        return threads.size() == expectedChannels();
    }

    // METODO HELPER

    /**
     * Recupera il {@link ConnectionThread} per indice di canale.
     *
     * @param channelIndex Indice del canale.
     * @return Il {@link ConnectionThread} corrispondente.
     * @throws IllegalArgumentException Se non esiste un canale con quell'indice.
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
     * Thread dedicato alla comunicazione su un singolo canale (socket).
     *
     * <p>Apre OOS e OIS (in quest'ordine, obbligatorio), poi rimane in loop
     * bloccante su {@code readObject()} fino alla chiusura del canale.
     * Ogni messaggio ricevuto viene passato alla callback {@code onReceive}
     * della classe esterna.</p>
     *
     * <p>Alla terminazione (normale o per eccezione) si rimuove dalla lista
     * {@code threads} e invoca {@code onDisconnect}.</p>
     *
     * <p>La visibilità è package-private per consentire a {@link ServerConnection}
     * (stesso package) di accedere a {@link #channelIndex} e ai metodi
     * {@link #send} e {@link #closeSocket}.</p>
     */
    class ConnectionThread extends Thread {

        // ATTRIBUTI 
        
        /** Indice del canale gestito da questo thread (immutabile). */
        final int channelIndex;

        /** Socket del canale. */
        private final Socket socket;

        /**
         * Stream di output verso il canale remoto.
         * Inizializzato all'avvio di {@link #run()}; {@code null} prima di allora.
         */
        private ObjectOutputStream oos;

        // COSTRUTTORE
        
        /**
         * Costruttore.
         * @param socket       Socket già connessa.
         * @param channelIndex Indice del canale (passato a onReceive e onDisconnect).
         */
        ConnectionThread(Socket socket, int channelIndex) {
            super("ConnectionChannel-" + channelIndex); // Serve per dare nomi identificativi diversi.
            this.socket       = socket;
            this.channelIndex = channelIndex;
        }

        /**
         * Invia un oggetto serializzabile su questo canale.
         *
         * @param data         Oggetto da inviare.
         * @throws IOException Se lo stream non è inizializzato o la scrittura fallisce.
         */
        void send(Serializable data) throws IOException {
            if (oos == null) throw new IOException("Stream non ancora inizializzato.");
            oos.writeObject(data);
            oos.flush();  // flush esplicito: evita che messaggi brevi restino nel buffer
        }

        /**
         * Chiude la socket di questo canale, interrompendo il loop di lettura.
         *
         * @throws IOException Se la chiusura fallisce.
         */
        void closeSocket() throws IOException {
            socket.close();
        }

        /**
         * Corpo del thread di lettura.
         *
         * <p><b>Ordine critico degli stream:</b> OOS prima di OIS (vedi Javadoc
         * della classe padre {@link NetworkConnection}).</p>
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
