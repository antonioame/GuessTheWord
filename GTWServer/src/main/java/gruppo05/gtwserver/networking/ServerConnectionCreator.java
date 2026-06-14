package gruppo05.gtwserver.networking;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import gruppo05.gtwshared.networking.NetworkConfiguration;
import gruppo05.gtwshared.networking.NetworkConnectionCreator;
import gruppo05.gtwshared.dto.CallbackDTO;
import gruppo05.gtwshared.networking.NetworkMessage;
import gruppo05.gtwshared.utility.Result;
import gruppo05.gtwshared.utility.Difficulty;
import gruppo05.gtwserver.db.*;
import gruppo05.gtwserver.model.*;
import gruppo05.gtwserver.controller.GameSetupController;
import gruppo05.gtwserver.sourcemanager.api.SourceManager;
import java.util.stream.Collectors;
import javafx.event.EventType;

/**
 * @class ServerConnectionCreator
 * @brief Controller di alto livello per la gestione della logica di rete lato Server.
 * @details Questa classe agisce da "cervello" per il server. Implementa il pattern Factory 
 * per la creazione della connessione TCP e funge da Dispatcher (smistatore) per tutti i 
 * messaggi in arrivo. Gestisce in modo thread-safe l'autenticazione, la coda di matchmaking, 
 * l'avvio delle partite e l'aggiornamento delle statistiche persistenti tramite DAO.
 * 
 */
public class ServerConnectionCreator extends NetworkConnectionCreator {

    /**
     * @brief Riferimento diretto all'infrastruttura di rete.
     * @details Utilizzato per inviare messaggi di risposta (unicast) o notifiche di gioco (broadcast).
     */
    private ServerConnection connection;
    
    /**
     * @brief Registro in memoria degli utenti attualmente autenticati.
     * @details È una ConcurrentHashMap per garantire la thread-safety, dato che più thread di 
     * rete (uno per client) potrebbero accedervi simultaneamente. Associa l'ID del canale (chiave) 
     * allo username (valore).
     */
    private final Map<Integer, String> loggedUsers = new ConcurrentHashMap<>();
    
    /**
     * @brief Mappa che associa l'indice del canale di connessione alla sfida (Challenge) attualmente in corso.
     * @details Utilizza una {@link ConcurrentHashMap} per garantire la thread-safety durante la gestione 
     * concorrente di multiple partite attive.
     */
    private final Map<Integer, Challenge> activeGames = new ConcurrentHashMap<>();

    /**
     * @brief Indice del canale del giocatore in attesa di un avversario.
     * @details Viene impostato a null se non ci sono giocatori in attesa nel sistema.
     */
    private Integer waitingChannel = null;

    /**
     * @brief Livello di difficoltà selezionato dal giocatore in attesa di matchmaking.
     * @details Definisce il vincolo di difficoltà che l'avversario dovrà soddisfare per l'accoppiamento.
     */
    private Difficulty waitingDifficulty = null;
    
    /**
     * @brief Mutex (Lock) utilizzato per sincronizzare la sezione critica del matchmaking.
     * @details Previene la "race condition" in cui due giocatori inviano PLAY_REQUEST nell'esatto 
     * millisecondo, garantendo che la coppia venga formata in modo atomico e sicuro.
     */
    private final Object matchLock = new Object();
    
    /**
     * @brief Callback opzionale notificata dalla UI quando un canale TCP si chiude.
     * @details È opzionale perché il server è in grado di essere attivo e online in background anche
     *          senza che la dashboard UI sia stata avviata o che l'amministratore abbia 
     *          effettuato l'accesso. Viene impostata dalla dashboard dopo il login per
     *          aggiornare il contatore dei client connessi con meccanismo event-driven. TODO
     */
    private Consumer<Integer> uiDisconnectCallback = null;

    /**
     * @brief Callback opzionale notificata dalla UI quando ci sono cambiamenti al database
     *        (ad esempio fine partita o nuovo utente registrato).
     */
    private Runnable uiDatabaseUpdateCallback = null;
    
    /** @brief Riferimento al SourceManager globale iniettato da App.java  */
    private final SourceManager sourceManager;

    /**
     * @brief Costruttore con dipendenza per il SourceManager.
     * @param sourceManager L'istanza globale unica per la generazione di domande.
     */
    public ServerConnectionCreator(SourceManager sourceManager) {
        this.sourceManager = sourceManager;
    }

    /**
     * @brief Costruisce e avvia il server leggendo la configurazione locale.
     * @details Legge il file "server.properties" per ottenere la porta di ascolto, istanzia 
     * il ServerSocket e inietta le lambda expression per intercettare i messaggi e le disconnessioni.
     * @return L'oggetto ServerConnection inizializzato e in ascolto.
     * @throws RuntimeException In caso di porta già in uso o altri errori irreversibili di I/O.
     */
    @Override
    public ServerConnection createConnection() {
        // Legge le impostazioni dal file .properties
        NetworkConfiguration config = this.readConfiguration("server.properties");
        try {
            // Configura il server con i callback per messaggi e gestione eventi
            this.connection = new ServerConnection(
                    config.getPort(),  // porta
                    this::handleMessage,  // onReceive
                    (index) -> System.out.println("Client connesso su canale " + index), // onClientConnected
                    (index) -> handleDisconnect(index));  // onDisconnect
            this.connection.connect();
            return this.connection;
        } catch (IOException e) {
            throw new RuntimeException("Impossibile avviare il server sulla porta configurata", e);
        }
    }

    /**
     * @brief Pulizia dello stato dell'utente e della partita in caso di disconnessione.
     * @details Oltre alle operazioni di pulizia interne, notifica la callback UI
     *          (se registrata) per aggiornare il contatore dei client connessi.
     * @param channelIndex L'indice del canale che si è disconnesso.
     */
    private void handleDisconnect(Integer channelIndex) {
        // Rimuove l'utente dalle mappe di stato
        loggedUsers.remove(channelIndex);
        activeGames.remove(channelIndex);
        
        // Se l'utente era in coda, lo rimuove dalla coda di attesa
        synchronized (matchLock) {
            if (waitingChannel != null && waitingChannel.equals(channelIndex)) {
                waitingChannel = null; 
                waitingDifficulty = null;
            }
        }

        // Notifica la dashboard (se presente) dell'avvenuta disconnessione
        if (uiDisconnectCallback != null) {
            uiDisconnectCallback.accept(channelIndex);
        }
    }

    /**
     * @brief Registra la callback della UI per gli eventi di disconnessione client.
     * @details Viene chiamata dalla dashboard dopo il login per ricevere notifiche
     *          puntuali sugli eventi di disconnessione (meccanismo event-driven).
     * 
     * @param[in] callback La funzione da invocare all'atto di ogni disconnessione.
     */
    public void setUiDisconnectCallback(Consumer<Integer> callback) {
        this.uiDisconnectCallback = callback;
    }

    /**
     * @brief Registra la callback della UI per gli aggiornamenti del database.
     * @param callback Funzione da invocare ad ogni modifica dei dati utente o partite.
     */
    public void setOnDatabaseUpdateCallback(Runnable callback) {
        this.uiDatabaseUpdateCallback = callback;
    }

    /**
     * @brief Logica principale di smistamento (dispatcher) dei messaggi ricevuti.
     * @param channelIndex Canale di provenienza del messaggio.
     * @param msg Oggetto serializzato ricevuto.
     */
    private void handleMessage(Integer channelIndex, Serializable msg) {
        // Verifica che il messaggio sia del tipo corretto
        if (!(msg instanceof NetworkMessage)) return;
        
        // Converte il messaggio in DTO per facilitare l'estrazione dati
        CallbackDTO dto = ((NetworkMessage) msg).toDTO();

        // Recupero l'username della sessione corrente, utile per autorizzare le richieste post-login
        String currentUsername = loggedUsers.get(channelIndex);

        try {
            switch (dto.getEventType()) {
                
                case LOGIN_REQUEST:
                    // 1. Recupero utente dal database tramite ConcretePlayerDAO
                    PlayerDAO playerDao = new ConcretePlayerDAO();
                    Optional<Player> playerOpt = playerDao.selectById(Optional.of(dto.getUsername())); 

                    // CASO A: player presente nel database
                    if (playerOpt.isPresent()) {
                        Player player = playerOpt.get();

                        // CASO B: password è corretta
                        if (player.getPassword().equals(dto.getPassword())) {

                            // Verifica stato dell'utente nei client attivi
                            boolean userAlreadyLogged = loggedUsers.containsValue(dto.getUsername());
                            boolean sameChannel = dto.getUsername().equals(loggedUsers.get(channelIndex));

                            // CASO C: L'utente è già loggato
                            if (userAlreadyLogged) {
                                if (sameChannel) {
                                    // È lo stesso client che ha inviato la richiesta due volte di fila (es. doppio clic).
                                    // Ignoriamo in silenzio per non sporcare la CLI del server.
                                    break; 
                                } else {
                                    // VERO tentativo di accesso simultaneo da un SECONDO client
                                    System.out.println("[Server] Bloccato accesso simultaneo per l'utente: " + dto.getUsername());
                                    connection.sendTo(channelIndex, NetworkMessage.LoginResponse.loginFailed("Account già connesso da un altro dispositivo."));
                                    break; // Interrompe l'esecuzione del case
                                }
                            }

                            // CASO D: Successo. Player presente, password corretta, non loggato altrove
                            System.out.println("[Server] Accesso corretto per l'utente: " + dto.getUsername());

                            // Registra l'utente e notifica il successo
                            loggedUsers.put(channelIndex, dto.getUsername());
                            connection.sendTo(channelIndex, NetworkMessage.LoginResponse.loginSuccess(false));

                        } else {
                            // L'utente esiste, ma la password è SBAGLIATA
                            connection.sendTo(channelIndex, NetworkMessage.LoginResponse.loginFailed("Password errata."));
                        }

                    } else {
                        // L'utente NON ESISTE nel database
                        connection.sendTo(channelIndex, NetworkMessage.LoginResponse.loginFailed("Utente non trovato nel database."));
                    }

                    break;

                case REGISTER_REQUEST:
                    // Creazione nuovo utente se l'username non esiste già nella tabella Player
                    PlayerDAO signupDao = new ConcretePlayerDAO();

                    // Controllo preventivo per evitare violazioni di chiave primaria sul Database
                    if (signupDao.selectById(Optional.of(dto.getUsername())).isPresent()) {

                        // L'utente esiste già
                        connection.sendTo(channelIndex, NetworkMessage.RegisterResponse.registerFailed("Username già in uso"));

                    } else {
                        // L'utente non esiste: possiamo procedere con l'inserimento
                        signupDao.insert(new Player(dto.getUsername(), dto.getPassword(), 0, 0, 0));

                        connection.sendTo(channelIndex, NetworkMessage.RegisterResponse.registerSuccess());
                        if (uiDatabaseUpdateCallback != null) {
                            uiDatabaseUpdateCallback.run();
                        }
                    }
                    break;

                case PLAY_REQUEST:
                    // Gestione matchmaking: se non c'è nessuno in attesa, accoda il giocatore
                    // Estraggo la difficoltà
                    Difficulty requestedDifficulty = dto.getDifficulty() != null ? dto.getDifficulty() : Difficulty.NORMAL;

                    synchronized (matchLock) {
                        if (waitingChannel == null) {
                            // Coda vuota: il client diventa l'host temporaneo in attesa di uno sfidante
                            waitingChannel = channelIndex;
                            waitingDifficulty = requestedDifficulty;
                            connection.sendTo(channelIndex, new NetworkMessage.PlayResponse(CallbackDTO.Status.WAITING));
                            System.out.println("Canale " + channelIndex + " in attesa di un avversario.");

                        } else if (!waitingChannel.equals(channelIndex)) {
                            // Se un altro utente è in coda, avvia la sfida
                            int p1Channel = waitingChannel;
                            int p2Channel = channelIndex;

                            // Recupero gli username effettivi. Se c'è un'anomalia, uso un fallback per il debug
                            String p1User = loggedUsers.getOrDefault(p1Channel, "Player1");
                            String p2User = currentUsername != null ? currentUsername : "Player2";

                            // Notifico a entrambi che la ricerca ha avuto successo (MATCH_FOUND)
                            connection.sendTo(p1Channel, new NetworkMessage.PlayResponse(CallbackDTO.Status.MATCH_FOUND));
                            connection.sendTo(p2Channel, new NetworkMessage.PlayResponse(CallbackDTO.Status.MATCH_FOUND));

                            // Genera i dati della partita tramite controller dedicato
                            // Passaggio del sourceManager globale al GameSetupController
                            GameSetupController setupController = new GameSetupController(this.sourceManager);

                            Difficulty p1Diff = waitingDifficulty;
                            Difficulty p2Diff = requestedDifficulty;

                            // Resetta la coda in anticipo per evitare race conditions, mentre si attende il manager in modo asincrono
                            waitingChannel = null;
                            waitingDifficulty = null;

                            // Passaggio della callback di successo e di errore al GameSetupController
                            setupController.generateMatchData(p1Diff, p2Diff, 
                                (controller) -> {
                                    // Nessun errore: crea la sfida e prosegue
                                    Challenge newChallenge = new Challenge(
                                        new Date(System.currentTimeMillis()), 
                                        controller.getMatchDifficulty(), 
                                        controller.getTargetWord(), 
                                        controller.getSourceId()
                                    );

                                    // Registra la sfida per entrambi i giocatori
                                    activeGames.put(p1Channel, newChallenge);
                                    activeGames.put(p2Channel, newChallenge);

                                    // Invia i dati di avvio partita ai due client
                                    try {
                                        connection.sendTo(p1Channel, new NetworkMessage.GameStart(controller.getCipheredText(), controller.getTimer(), 0, p2User, controller.getMatchDifficulty()));
                                        connection.sendTo(p2Channel, new NetworkMessage.GameStart(controller.getCipheredText(), controller.getTimer(), 1, p1User, controller.getMatchDifficulty()));
                                    } catch (IOException e) {
                                        System.err.println("Errore nell'invio del messaggio di GameStart: " + e.getMessage());
                                    }
                                },
                                (errorMessage) -> {
                                    System.err.println("Generazione partita fallita. Matchmaking annullato.");
                                    try {
                                        String errorPayload = "ERROR_REDIRECT:" + errorMessage;
                                        NetworkMessage errorMsg = new NetworkMessage.TextMessage(errorPayload);

                                        // Invia il messaggio di errore per far tornare i client alla lobby
                                        connection.sendTo(p1Channel, errorMsg);
                                        connection.sendTo(p2Channel, errorMsg);
                                    } catch (IOException e) {
                                        System.err.println("Errore nell'invio del messaggio di fallimento ai client: " + e.getMessage());
                                    }
                                }
                            );
                        }
                    }
                    break;

                case ANSWER_SUBMISSION:
                    // Verifica l'esito della risposta inviata dall'utente
                    if (currentUsername == null) return; 
                    Challenge ch = activeGames.get(channelIndex);
                    
                    if (ch != null) {
                        // TODO la riga sotto funziona correttamente perché sono previsti esattamente 2 giocatori
                        int opponentChannel = (channelIndex == 0) ? 1 : 0;
                        
                        boolean isTimeout = dto.getProposedWord() == null || dto.getProposedWord().trim().isEmpty();
                        boolean isCorrect = ch.getWord().equalsIgnoreCase(dto.getProposedWord());
                        String opponentUsername = loggedUsers.getOrDefault(opponentChannel, "Avversario");

                        if (isCorrect) {
                            activeGames.remove(channelIndex);
                            activeGames.remove(opponentChannel);

                            // Calcola risultati per vincitore e perdente
                            Result resultForSender = Result.WIN;
                            Result resultForOpponent = Result.LOSE;
                            
                            // Notifica esiti ai client
                            connection.sendTo(channelIndex, new NetworkMessage.GameResult(resultForSender, ch.getWord(), currentUsername, dto.getResponseTime()));
                            connection.sendTo(opponentChannel, new NetworkMessage.GameResult(resultForOpponent, ch.getWord(), currentUsername, dto.getResponseTime()));
                            
                            // Persiste i risultati sul DB
                            ChallengeDAO challengeDao = new ConcreteChallengeDAO();
                            challengeDao.insert(ch);
                            
                            GameDAO gameDao = new ConcreteGameDAO();
                            gameDao.insert(new Game(currentUsername, ch.getCode(), resultForSender, dto.getResponseTime()));
                            gameDao.insert(new Game(opponentUsername, ch.getCode(), resultForOpponent, dto.getResponseTime()));
                            
                            if (uiDatabaseUpdateCallback != null) {
                                uiDatabaseUpdateCallback.run();
                            }
                        } else if (isTimeout) {
                            activeGames.remove(channelIndex);
                            activeGames.remove(opponentChannel);

                            Result resultForSender = Result.DRAW;
                            Result resultForOpponent = Result.DRAW;
                            
                            connection.sendTo(channelIndex, new NetworkMessage.GameResult(resultForSender, ch.getWord(), null, dto.getResponseTime()));
                            connection.sendTo(opponentChannel, new NetworkMessage.GameResult(resultForOpponent, ch.getWord(), null, dto.getResponseTime()));
                            
                            ChallengeDAO challengeDao = new ConcreteChallengeDAO();
                            challengeDao.insert(ch);
                            
                            GameDAO gameDao = new ConcreteGameDAO();
                            gameDao.insert(new Game(currentUsername, ch.getCode(), resultForSender, dto.getResponseTime()));
                            gameDao.insert(new Game(opponentUsername, ch.getCode(), resultForOpponent, dto.getResponseTime()));
                            
                            if (uiDatabaseUpdateCallback != null) {
                                uiDatabaseUpdateCallback.run();
                            }
                        } else {
                            // Tentativo errato
                            connection.sendTo(channelIndex, new NetworkMessage.WrongAnswer());
                        }
                    }
                    break;

                case HISTORY_REQUEST:
                    // Recupera e invia lo storico partite dell'utente
                    if (currentUsername == null) return; 
                    
                    PlayerDAO playerD = new ConcretePlayerDAO();
                    Optional<Player> p = playerD.selectById(Optional.of(currentUsername));
                    
                    if (p.isPresent()) {
                        Player player = p.get();
                        GameDAO gameDao = new ConcreteGameDAO();
                        
                        // Carico tutto ed estraggo le partite di questo utente
                        List<Game> allGames = gameDao.selectAll();
                        List<CallbackDTO.MatchRecord> records = new ArrayList<>();
                        
                        // Filtra solo le partite dell'utente corrente
                        for (Game g : allGames) {
                            if (g.getPlayer().equals(currentUsername)) {
                                // Devi recuperare l'username dell'avversario contro cui ha giocato il giocatore
                                List<Game> currentMatch = gameDao.selectAllWhere(Optional.empty(), Optional.of(g.getChallenge()), Optional.empty(), Optional.empty());

                                String opponentUsername = null;
                                for(Game tmp : currentMatch) {
                                    if(!tmp.getPlayer().equals(currentUsername)) opponentUsername = tmp.getPlayer();
                                }
                                // Mappo i dati del database nel Record DTO per alleggerire il transito in rete
                                
                                records.add(new CallbackDTO.MatchRecord(
                                        String.valueOf(opponentUsername), 
                                        g.getResult(),
                                        LocalDateTime.now(), 
                                        g.getResponseTime()
                                ));
                            }
                        }
                        
                        // Calcola statistiche medie e invia risposta
                        double avgTime = (player.getTotalGamesPlayed() > 0) ? (double) player.getTotalPlayedTime() / player.getTotalGamesPlayed() : 0.0;
                        connection.sendTo(channelIndex, new NetworkMessage.HistoryResponse(records, player.getTotalGamesWon(), player.getTotalGamesPlayed(), avgTime, player.getTotalPlayedTime()));
                    }
                    break;

                case CLIENT_DISCONNECT:
                    // Il client ha cliccato "Esci". Invoco la stessa routine usata per le cadute di connessione
                    handleDisconnect(channelIndex);
                    break;

                case TEXT_MESSAGE:
                    // Ricezione di un messaggio generico
                    System.out.println("Messaggio di sistema dal client " + channelIndex + ": " + dto.getMessage());
                    break;

                default:
                    // Ricezione di un messaggio non riconoscito
                    System.out.println("Tipo messaggio non gestito: " + dto.getEventType());
            }
        } catch (IOException e) {
            System.err.println("Errore di invio dati (I/O) verso il client sul canale " + channelIndex);
            e.printStackTrace();
        }
    }
}