package gruppo05.gtwserver.networking;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
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
import gruppo05.gtwserver.db.*;
import gruppo05.gtwserver.model.*;
import javafx.scene.control.Alert;

/**
 * @class ServerConnectionCreator
 * @brief Controller di alto livello per la gestione della logica di rete lato Server.
 * @details Questa classe agisce da "cervello" per il server. Implementa il pattern Factory 
 * per la creazione della connessione TCP e funge da Dispatcher (smistatore) per tutti i 
 * messaggi in arrivo. Gestisce in modo thread-safe l'autenticazione, la coda di matchmaking, 
 * l'avvio delle partite e l'aggiornamento delle statistiche persistenti tramite DAO.
 * 
 * @author chiara
 * @version 2.0
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
     * @brief Puntatore al canale dell'utente attualmente in attesa di un avversario.
     * @details Vale null se la coda è vuota. Viene popolato dal primo giocatore che invia una 
     * PLAY_REQUEST e viene resettato non appena la partita si avvia.
     */
    private Integer waitingChannel = null;
    
    /**
     * @brief Mutex (Lock) utilizzato per sincronizzare la sezione critica del matchmaking.
     * @details Previene la "race condition" in cui due giocatori inviano PLAY_REQUEST nell'esatto 
     * millisecondo, garantendo che la coppia venga formata in modo atomico e sicuro.
     */
    private final Object matchLock = new Object();

    /**
     * @brief Costruisce e avvia il server leggendo la configurazione locale.
     * @details Legge il file "server.properties" per ottenere la porta di ascolto, istanzia 
     * il ServerSocket e inietta le lambda expression per intercettare i messaggi e le disconnessioni.
     * @return L'oggetto ServerConnection inizializzato e in ascolto.
     * @throws RuntimeException In caso di porta già in uso o altri errori irreversibili di I/O.
     */
    @Override
    public ServerConnection createConnection() {
        NetworkConfiguration config = this.readConfiguration("server.properties");
        try {
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
     * @brief Routine di pulizia eseguita quando un client interrompe la comunicazione.
     * @details Rimuove l'utente dai registri di sessione e, se era in attesa di una partita, 
     * libera la coda di matchmaking per non bloccare i futuri giocatori.
     * @param[in] channelIndex L'identificativo numerico del socket disconnesso.
     */
    private void handleDisconnect(Integer channelIndex) {
        System.out.println("Client " + channelIndex + " disconnesso o caduto.");
        
        // Rimuove l'utente dalla mappa delle sessioni attive
        loggedUsers.remove(channelIndex);
        
        // Accesso sincronizzato per evitare conflitti con il thread di un nuovo giocatore in ingresso
        synchronized (matchLock) {
            if (waitingChannel != null && waitingChannel.equals(channelIndex)) {
                waitingChannel = null; 
                System.out.println("La coda di matchmaking è stata pulita a causa della disconnessione.");
            }
        }
    }

    /**
     * @brief Cuore pulsante della logica server: smista ed elabora i messaggi ricevuti.
     * @details Analizza l'evento trasportato dal NetworkMessage, interroga/aggiorna il database 
     * tramite i vari DAO e formula la risposta di protocollo appropriata da rispedire al mittente.
     * @param[in] channelIndex Il canale univoco da cui proviene il messaggio.
     * @param[in] msg L'oggetto dati deserializzato, atteso di tipo NetworkMessage.
     */
    private void handleMessage(Integer channelIndex, Serializable msg) {
        // Filtro di sicurezza: scarto qualsiasi pacchetto non conforme al protocollo
        if (!(msg instanceof NetworkMessage)) return;
        
        // Conversione in Data Transfer Object per estrarre facilmente i valori scambiati
        CallbackDTO dto = ((NetworkMessage) msg).toDTO();

        // Recupero l'username della sessione corrente, utile per autorizzare le richieste post-login
        String currentUsername = loggedUsers.get(channelIndex);

        try {
            switch (dto.getEventType()) {
                case LOGIN_REQUEST:
                    AdminDAO adminDao = new AdminDAO();
                    Optional<Admin> admin = adminDao.selectById(new AdminId(dto.getUsername()));
                    
                    // Verifica dell'esistenza dell'utente e match esatto della password
                    if (admin.isPresent() && admin.get().getPassword().equals(dto.getPassword())) {
                        // Salvo in memoria lo stato "loggato" legando l'username al canale TCP
                        loggedUsers.put(channelIndex, dto.getUsername());
                        connection.sendTo(channelIndex, NetworkMessage.LoginResponse.loginSuccess(false));
                    } else {
                        connection.sendTo(channelIndex, NetworkMessage.LoginResponse.loginFailed("Credenziali errate"));
                    }
                    break;

                case REGISTER_REQUEST:
                    AdminDAO signupDao = new AdminDAO();
                    
                    // Controllo preventivo per evitare violazioni di chiave primaria sul Database
                    if (signupDao.selectById(new AdminId(dto.getUsername())).isPresent()) {
                        connection.sendTo(channelIndex, NetworkMessage.RegisterResponse.registerFailed("Username già in uso"));
                    } else {
                        signupDao.insert(new Admin(dto.getUsername(), dto.getPassword()));
                        connection.sendTo(channelIndex, NetworkMessage.RegisterResponse.registerSuccess());
                    }
                    break;

                case PLAY_REQUEST:
                    // Sezione Critica: L'accesso alla coda (waitingChannel) deve essere thread-safe
                    synchronized (matchLock) {
                        if (waitingChannel == null) {
                            // Coda vuota: il client diventa l'host temporaneo in attesa di uno sfidante
                            waitingChannel = channelIndex;
                            connection.sendTo(channelIndex, new NetworkMessage.PlayResponse(CallbackDTO.Status.WAITING));
                            System.out.println("Canale " + channelIndex + " in attesa di un avversario.");
                            
                        } else if (!waitingChannel.equals(channelIndex)) {
                            // Coda piena: ci sono due giocatori distinti, la partita può iniziare
                            int p1Channel = waitingChannel;
                            int p2Channel = channelIndex;

                            // Recupero gli username effettivi. Se c'è un'anomalia, uso un fallback per il debug
                            String p1User = loggedUsers.getOrDefault(p1Channel, "Player1");
                            String p2User = currentUsername != null ? currentUsername : "Player2";

                            // Fase 1: Notifico a entrambi che la ricerca ha avuto successo (MATCH_FOUND)
                            connection.sendTo(p1Channel, new NetworkMessage.PlayResponse(CallbackDTO.Status.MATCH_FOUND));
                            connection.sendTo(p2Channel, new NetworkMessage.PlayResponse(CallbackDTO.Status.MATCH_FOUND));
                            
                            // Fase 2: Prelevo i dati della sfida dal database
                            ChallengeDAO chDao = new ChallengeDAO();
                            Optional<Challenge> optCh = chDao.selectById(new ChallengeId(1)); /* vedi */
                            
                            // Valori di default in caso di assenza della sfida nel DB
                            String cipheredText = ""; 
                            int challengeCode = 1;
                            int timer = 60; 
                            
                            if (optCh.isPresent()) {
                                Challenge ch = optCh.get();
                                challengeCode = ch.getId().getCode();
                                // Meccanismo di cifratura 
                                cipheredText = ch.getWord(); /* vedi */
                            }

                            // Fase 3: Avvio effettivo. 
                            // INCROCIO DEI NOMI: p1 deve vedere il nome di p2 come avversario, e viceversa.
                            connection.sendTo(p1Channel, new NetworkMessage.GameStart(cipheredText, timer, 0, p2User, challengeCode));
                            connection.sendTo(p2Channel, new NetworkMessage.GameStart(cipheredText, timer, 1, p1User, challengeCode));

                            // Fase 4: Svuoto la coda per consentire a due nuovi giocatori di sfidarsi
                            waitingChannel = null;
                            System.out.println("Match avviato con successo: " + p1User + " VS " + p2User);
                        }
                    }
                    break;

                case ANSWER_SUBMISSION:
                    // Drop del pacchetto se arriva da un client non autenticato
                    if (currentUsername == null) return; 
                    
                    ChallengeDAO challengeDao = new ChallengeDAO();
                    Optional<Challenge> challenge = challengeDao.selectById(new ChallengeId(dto.getChallengeCode()));
                    
                    if (challenge.isPresent()) {
                        Challenge ch = challenge.get();
                        
                        // Controllo validità: ignora maiuscole/minuscole nella risposta dell'utente
                        // getWord() -> parola corretta
                        boolean isCorrect = ch.getWord().equalsIgnoreCase(dto.getProposedWord());
                        Result result = isCorrect ? Result.WIN : Result.LOSE;
                        
                        // BROADCAST: Comunico a tutti e due i client nella stanza il risultato finale della partita
                        connection.broadcast(new NetworkMessage.GameResult(
                                result, 
                                ch.getWord(), 
                                currentUsername, // Chi ha generato l'esito
                                dto.getResponseTime()
                        ));
                        
                        // Persistenza: Registro la giocata per le statistiche globali (usate da HISTORY_REQUEST)
                        GameDAO gameDao = new GameDAO();
                        gameDao.insert(new Game(currentUsername, dto.getChallengeCode(), result, dto.getResponseTime()));
                    }
                    break;

                case HISTORY_REQUEST:
                    if (currentUsername == null) return; 
                    
                    PlayerDAO playerDao = new PlayerDAO();
                    Optional<Player> p = playerDao.selectById(new PlayerId(currentUsername));
                    
                    if (p.isPresent()) {
                        Player player = p.get();
                        GameDAO gameDao = new GameDAO();
                        
                        // Carico tutto ed estraggo le partite di questo utente
                        List<Game> allGames = gameDao.selectAll();
                        List<CallbackDTO.MatchRecord> records = new ArrayList<>();
                        
                        for (Game g : allGames) {
                            if (g.getId().getPlayer().equals(currentUsername)) {
                                // Mappo i dati del database nel Record DTO per alleggerire il transito in rete
                                records.add(new CallbackDTO.MatchRecord(
                                        String.valueOf(g.getId().getChallenge()), 
                                        g.getResult(),
                                        LocalDateTime.now(), // Il timestamp locale al momento della richiesta
                                        g.getResponseTime()
                                ));
                            }
                        }
                        
                        // Prevenzione errore matematico: Divisione per zero se l'utente è nuovo e non ha mai giocato
                        double avgTime = (player.getTotalGamesPlayed() > 0) 
                                ? (double) player.getTotalPlayedTime() / player.getTotalGamesPlayed() 
                                : 0.0;
                                
                        // Impacchetto i dati aggregati del Player e la lista dei singoli Match, e li invio
                        connection.sendTo(channelIndex, new NetworkMessage.HistoryResponse(
                                records, 
                                player.getTotalGamesWon(), 
                                player.getTotalGamesPlayed(), 
                                avgTime, 
                                player.getTotalPlayedTime()
                        ));
                    }
                    break;

                case CLIENT_DISCONNECT:
                    // Il client ha cliccato "Esci". Invoco la stessa routine usata per le cadute di connessione
                    handleDisconnect(channelIndex);
                    break;

                case TEXT_MESSAGE:
                    Alert infoAlert = new Alert(Alert.AlertType.INFORMATION, "Messaggio dal Client: " + dto.getMessage());
                    infoAlert.setHeaderText("Notifica");
                    infoAlert.showAndWait(); 
                    System.out.println("Messaggio di sistema dal client " + channelIndex + ": " + dto.getMessage());
                    break;

                default:
                    // Catch-all per eventuali nuovi tipi di messaggio futuri non ancora implementati
                    System.out.println("Attenzione: Tipo di messaggio non gestito - " + dto.getEventType());
            }
            
        } catch (IOException e) {
            System.err.println("Errore di invio dati (I/O) verso il client sul canale " + channelIndex);
            e.printStackTrace();
        }
    }
}