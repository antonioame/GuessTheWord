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
 * @class NetworkConnection
 * @brief Classe astratta che modella una connessione di rete generica (Client o Server).
 * * @details Gestisce un insieme di canali di comunicazione indipendenti tramite la classe 
 * interna {@link ConnectionThread}. Fornisce un'architettura thread-safe per inviare 
 * e ricevere oggetti serializzabili attraverso socket TCP. La logica di creazione delle 
 * socket e la determinazione del numero di canali è delegata alle classi figlie tramite 
 * i metodi astratti {@link #createSocket()} e {@link #expectedChannels()}.
 * 
 * @version 1.0
 */
public abstract class NetworkConnection {
    
    // ATTRIBUTI
    
    /**
     * @brief Lista thread-safe dei canali di comunicazione attivi.
     * @details Essendo utilizzata da thread paralleli, la lista è wrappata in 
     * Collections.synchronizedList. Gli accessi in iterazione devono essere sincronizzati esplicitamente.
     */
    private final List<ConnectionThread> threads = Collections.synchronizedList(new ArrayList<>());

    /**
     * @brief Callback invocata automaticamente alla ricezione di un messaggio su qualsiasi canale.
     * @details Riceve in input due parametri: l'indice del canale (Integer) da cui proviene il 
     * messaggio e il payload deserializzato (Serializable).
     */
    private final BiConsumer<Integer, Serializable> onReceive;

    /**
     * @brief Callback opzionale invocata quando un canale di rete viene chiuso.
     * @details Viene chiamata passando l'indice del canale disconnesso, sia in caso di 
     * chiusura volontaria che di caduta della connessione.
     */
    private final Consumer<Integer> onDisconnect;

    // COSTRUTTORI
    
    /**
     * @brief Costruttore completo per istanziare una connessione di rete.
     * @param[in] onReceive    La callback invocata ad ogni messaggio in ingresso.
     * @param[in] onDisconnect La callback invocata alla chiusura di un canale.
     */
    protected NetworkConnection(BiConsumer<Integer, Serializable> onReceive, Consumer<Integer> onDisconnect) {
        this.onReceive    = onReceive;
        this.onDisconnect = onDisconnect;
    }

    /**
     * @brief Costruttore semplificato senza la gestione degli eventi di disconnessione.
     * @param[in] onReceive La callback invocata ad ogni messaggio in ingresso.
     */
    protected NetworkConnection(BiConsumer<Integer, Serializable> onReceive) {
        this(onReceive, null);
    }

    // METODI ASTRATTI (devono essere implementati nelle sottoclassi)
    
    /**
     * @brief Metodo astratto che delega alla sottoclasse la fornitura di una socket pronta.
     * @details Nel caso di un Server, utilizzerà serverSocket.accept(), mentre nel caso 
     * di un Client creerà una new Socket(ip, porta).
     * @return Una Socket connessa e pronta per l'apertura degli stream.
     * @throws IOException Se si verifica un errore di rete durante la creazione o l'accettazione.
     */
    protected abstract Socket createSocket() throws IOException;

    /**
     * @brief Metodo astratto che definisce quanti canali la connessione deve gestire.
     * @return Il numero atteso di canali.
     */
    protected abstract int expectedChannels();

    // GESTIONE DELLA CONNESSIONE
    
    /**
     * @brief Avvia asincronamente la fase di setup di tutti i canali richiesti.
     * @details Crea un numero di thread di setup pari a {@link #expectedChannels()}. 
     * Ogni thread tenterà di ottenere una socket in loop finché non ci riesce.
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
                boolean connected = false;
                boolean hasFailed = false; // Flag per tracciare se c'è stata una disconnessione
                
                while (!connected) {
                    try {
                    // Creazione della socket delegata alla sottoclasse
                        Socket socket = createSocket();
                        connected = true; 
                        
                        // Se aveva fallito in precedenza, significa che ora si è RICONNESSO
                        if (hasFailed) {
                            onReconnected(channelIndex); 
                        }

                        onChannelReady(socket, channelIndex);
                        startChannel(socket, channelIndex);

                    } catch (IOException ex) {
                        hasFailed = true; // Imposta il flag a true al primo fallimento
                        System.err.println("[NetworkConnection] Canale offline. Ritento in 5s...");

                        try {
                            Thread.sleep(5000); 
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }, "SetupChannel-" + channelIndex);

            // Thread demone: non impedisce la chiusura della JVM
            setupThread.setDaemon(true);

            // Avvio del thread di setup
            setupThread.start();
        }
    }
    
    /**
     * @brief Metodo invocato SOLO se la connessione viene ristabilita dopo almeno un tentativo fallito.
     * @param[in] channelIndex L'identificativo numerico del canale.
     */
    protected void onReconnected(int channelIndex) {
        // Implementazione di default vuota
    }

    // METODI DI UTILITA' E GESTIONE INTERNA

    /**
     * @brief Metodo eseguito subito dopo la creazione fisica della socket.
     * @details Le sottoclassi possono sovrascriverlo per eseguire logiche custom (es. log di rete, 
     * avvisi UI) appena la connessione TCP viene stabilita ma prima che inizi l'ascolto.
     * @param[in] socket       La socket appena instaurata.
     * @param[in] channelIndex L'identificativo numerico del canale.
     */
    protected void onChannelReady(Socket socket, int channelIndex) {
        // Implementazione di default vuota
    }
    
    // GESTIONE INTERNA DEI CANALI

    /**
     * @brief Istituisce, registra in memoria e avvia il thread di ascolto persistente per un canale.
     * @param[in] socket       La socket connessa.
     * @param[in] channelIndex L'identificativo numerico assegnato al canale.
     */
    protected final void startChannel(Socket socket, int channelIndex) {
        // 1. CREAZIONE DEL THREAD
        // Istanzazione di un nuovo Thread dedicato esclusivamente a questo client.
        ConnectionThread ct = new ConnectionThread(socket, channelIndex);
        
        // 2. REGISTRAZIONE DEL THREAD
        // Salvo il riferimento di questo thread in una lista gestita dal server.
        // Serve al server per mantenere il controllo della comunicazione.
        threads.add(ct);
        
        // Imposta il thread come demone per permetterne la chiusura in background al termine del main
        ct.setDaemon(true);

        // 4. ESECUZIONE IN PARALLELO
        // Il metodo .start() accende fisicamente il thread. Da questo preciso istante, 
        // il codice contenuto nel metodo run() di ConnectionThread girerà in parallelo, 
        // liberando il server principale che potrà subito fare altre operazioni.
        ct.start();
    }

    // API PUBBLICA

    /**
     * @brief Invia un oggetto serializzato verso uno specifico canale.
     * @param[in] channelIndex L'indice del canale destinatario.
     * @param[in] data         L'oggetto (che implementa Serializable) da trasmettere.
     * @throws IOException Se lo stream è chiuso, non inizializzato, o cade la connessione.
     * @throws IllegalArgumentException Se l'indice specificato non corrisponde a nessun canale attivo.
     */
    public void sendTo(int channelIndex, Serializable data) throws IOException {
        getThread(channelIndex).send(data);
    }

    /**
     * @brief Invia lo stesso oggetto a tutti i canali correntemente attivi (Broadcast).
     * @details È un'operazione thread-safe. Se l'invio fallisce su un canale, viene loggato
     * l'errore ma il ciclo continua per tentare di recapitare il pacchetto agli altri.
     * @param[in] data Il payload da inviare a tutti.
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
     * @brief Invia un messaggio a tutti i canali attivi ad eccezione di uno specifico (Multicast filtrato).
     * @param[in] excludedIndex L'indice del canale a cui NON inviare il pacchetto.
     * @param[in] data          Il payload da inviare.
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
     * @brief Chiude forzatamente il socket e il flusso di rete associati a un canale.
     * @param[in] channelIndex L'indice del canale da troncare.
     * @throws IOException Se si verifica un errore durante la chiusura fisica del socket.
     */
    public void disconnectChannel(int channelIndex) throws IOException {
        getThread(channelIndex).closeSocket();
    }

    /**
     * @brief Itera su tutti i canali attivi chiudendone i rispettivi socket.
     * @details Crea una copia della lista per evitare eccezioni di concorrenza 
     * durante la modifica strutturale operata dai thread morenti.
     */
    public void disconnectAll() {
        synchronized (threads) {
            for (ConnectionThread ct : new ArrayList<>(threads)) {
                try { 
                    ct.closeSocket(); 
                } catch (IOException ignored) {
                    // Eccezione soppressa intenzionalmente in fase di chiusura massiva
                }
            }
        }
    }

    /**
     * @brief Restituisce il conteggio in tempo reale dei thread di rete attivi e validi.
     * @return Il numero intero di canali correntemente aperti.
     */
    public int getActiveChannelCount() {
        return threads.size();
    }

    /**
     * @brief Controlla se la rete è pronta all'uso secondo i requisiti della sottoclasse.
     * @return true se il numero di canali attivi coincide con i canali attesi, false altrimenti.
     */
    public boolean areAllChannelsReady() {
        return threads.size() == expectedChannels();
    }

    /**
     * @brief Verifica se un determinato indice di canale è attualmente occupato da un thread attivo.
     * @details Utile per le sottoclassi che implementano logica di assegnazione dinamica degli indici
     *          (es. per riutilizzare uno slot liberato da una disconnessione precedente).
     * 
     * @param[in] channelIndex Indice da controllare.
     * @return true Se esiste un {@link ConnectionThread} attivo con quell'indice, false altrimenti.
     */
    protected boolean isChannelActive(int channelIndex) {
        synchronized (threads) {
            return threads.stream().anyMatch(ct -> ct.channelIndex == channelIndex);
        }
    }

    // METODO HELPER

    /**
     * @brief Ricerca in modo sincronizzato un ConnectionThread specifico tramite il suo indice.
     * @param[in] channelIndex L'indice da cercare.
     * @return L'istanza del thread che gestisce quel canale.
     * @throws IllegalArgumentException Se nessun thread nella lista ha l'indice richiesto.
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
     * @class ConnectionThread
     * @brief Lavoratore in background dedicato alla ricezione persistente su una singola socket.
     * @details Il thread apre gli stream (con l'ordine rigoroso OOS -> OIS per evitare stalli), 
     * e rimane in un ciclo bloccante in ascolto. Alla ricezione, gira il pacchetto alla callback.
     */
    class ConnectionThread extends Thread {

        // ATTRIBUTI 
        
        /**
         * @brief Indice immutabile assegnato a questo canale.
         */
        final int channelIndex;

        /**
         * @brief La connessione TCP sottostante.
         */
        private final Socket socket;

        /**
         * @brief Lo stream adibito all'invio dei dati in formato oggetto.
         */
        private ObjectOutputStream oos;

        // COSTRUTTORE
        
        /**
         * @brief Istanzia il gestore di canale.
         * @param[in] socket       La socket connessa su cui operare.
         * @param[in] channelIndex L'identificativo che il server usa per distinguere i client.
         */
        ConnectionThread(Socket socket, int channelIndex) {
            super("ConnectionChannel-" + channelIndex);
            this.socket       = socket;
            this.channelIndex = channelIndex;
        }

        /**
         * @brief Serializza un oggetto e lo inoltra verso il partner remoto.
         * @details Utilizza oos.flush() per assicurare che non ci siano blocchi nei buffer TCP.
         * @param[in] data L'oggetto Serializable.
         * @throws IOException Se lo stream non è pronto o in caso di fallimento di rete.
         */
        void send(Serializable data) throws IOException {
            if (oos == null) throw new IOException("Stream di output non ancora inizializzato.");
            
            try {
                oos.writeObject(data);
                oos.flush();  // flush esplicito: evita che messaggi brevi restino nel buffer
            } catch (IOException e) {
                System.err.println("[NetworkConnection] Errore I/O sul canale " + channelIndex 
                        + ". Impossibile inviare dati, chiusura forzata del socket in corso.");
                
                // Forza la chiusura del socket per innescare la pulizia di sistema (onDisconnect)
                try {
                    closeSocket();
                } catch (IOException ignored) {
                    // Ignora le eccezioni durante la chiusura
                }
                // Lancia un'eccezione 
                throw new IOException("Socket interrotto.", e);
            }
        }

        /**
         * @brief Interrompe volontariamente le comunicazioni chiudendo il tubo di rete.
         * @throws IOException Se ci sono problemi hardware/software in chiusura.
         */
        void closeSocket() throws IOException {
            socket.close();
        }

        /**
         * @brief Ciclo vitale del thread: apertura stream e loop di ascolto.
         * @details L'ObjectOutputStream (OOS) DEVE essere aperto prima dell'ObjectInputStream (OIS).
         * La creazione dell'OIS è un'operazione bloccante in attesa dell'header del partner; se entrambi 
         * i nodi aprissero prima l'OIS si otterrebbe un deadlock infinito.
         */
        @Override
        public void run() {
            try (
                // Ordine garantito grazie al blocco try-with-resources
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream  ois = new ObjectInputStream(socket.getInputStream())
            ) {
                this.oos = oos;

                System.out.println("[NetworkConnection] Canale " + channelIndex
                        + " attivo -> " + socket.getInetAddress().getHostAddress()
                        + ":" + socket.getPort());

                // Loop bloccante di lettura
                while (!socket.isClosed()) {
                    Serializable msg = (Serializable) ois.readObject();
                    onReceive.accept(channelIndex, msg); // Dispatch alla logica applicativa
                }

            } catch (IOException ex) {
                // Chiusura normale (disconnectChannel) o perdita di connessione
                System.out.println("[NetworkConnection] Canale " + channelIndex
                        + " chiuso: " + ex.getMessage());
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } finally {
                // Operazioni di pulizia inevitabili all'uscita dal thread
                threads.remove(this);
                if (onDisconnect != null) {
                    onDisconnect.accept(channelIndex);
                }
            }
        }
    }
}