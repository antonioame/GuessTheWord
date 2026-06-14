package gruppo05.gtwshared.networking;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import gruppo05.gtwshared.dto.CallbackDTO;
import gruppo05.gtwshared.utility.Result;
import gruppo05.gtwshared.utility.Difficulty;

/**
 * @class NetworkMessage
 * @brief Classe base astratta per tutti i payload scambiati in rete tra server e client.
 * * @details Stabilisce il contratto fondamentale del protocollo di rete del progetto.
 * Ogni pacchetto serializzabile viaggiante sui socket deve estendere questa classe. 
 * Assicura l'autoconsapevolezza temporale (timestamp) e l'interoperabilità verso la GUI 
 * tramite la conversione in {@link CallbackDTO}.
 * 
 * * <p><b>FLUSSO DI COMUNICAZIONE CLIENT-SERVER:</b></p>
 * <ol>
 * <li><b>Autenticazione:</b> Il client invia una {@link LoginRequest}. Il server controlla 
 * i dati nel DB e risponde con {@link LoginResponse}, specificando anche se l'utente 
 * ha privilegi di amministratore.</li>
 * <li><b>Registrazione:</b> Se l'utente non ha un account, invia una {@link RegisterRequest}. 
 * Il server inserisce i dati e risponde con {@link RegisterResponse}.</li>
 * 
 * <li><b>Richiesta Partita:</b> L'utente loggato invia una {@link PlayRequest} per cercare 
 * un avversario. Il server risponde con {@link PlayResponse}, indicando se la partita 
 * inizia subito (MATCH_FOUND) o se deve aspettare (WAITING).</li>
 * <li><b>Inizio Partita:</b> Appena ci sono due giocatori, il server invia a entrambi 
 * un messaggio {@link GameStart} contenente la sfida cifrata, il timer e i dati dell'avversario.</li>
 * <li><b>Fase di Gioco:</b> 
 * <ul>
 * <li>Il giocatore elabora la risposta e la invia con {@link AnswerSubmission}.</li>
 * <li>Se un giocatore risponde, il server avvisa l'altro in tempo reale tramite {@link OpponentAnswered}.</li>
 * </ul>
 * </li>
 * <li><b>Fine Partita:</b> Il server elabora i tempi e decreta un vincitore, comunicandolo 
 * ai due client tramite il messaggio {@link GameResult}.</li>
 * 
 * <li><b>Storico:</b> Dalla lobby, il client può richiedere lo storico delle proprie partite 
 * con {@link HistoryRequest}. Il server interroga il DB e risponde con {@link HistoryResponse}.</li>
 * 
 * <li><b>Disconnessione:</b> 
 * <ul>
 * <li>Se un client esce dal gioco regolarmente, invia {@link ClientDisconnect}.</li>
 * <li>Se un client cade o si disconnette durante un'attesa o una partita, il server avvisa 
 * l'avversario superstite inviando {@link OpponentDisconnected}.</li>
 * </ul>
 * </li>
 * </ol>
 * 
 * @version 1.0
 */
public abstract class NetworkMessage implements Serializable {

    /** @brief Versione di serializzazione di base dell'architettura. */
    private static final long serialVersionUID = 1L;
    
    /** @brief Valore enumerato identificativo usato in ricezione per smistare rapidamente la logica. */
    private final MessageType type;
    
    /** @brief L'istante esatto di emissione fisica o logica del messaggio. */
    private final LocalDateTime timestamp;

    /**
     * @brief Costruttore interno per la base del messaggio.
     * @param type Il tipo di messaggio ereditato.
     */
    protected NetworkMessage(MessageType type) {
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * @brief Restituisce il tipo discriminante del messaggio.
     * @return Enum MessageType associato.
     */
    public MessageType getType() { 
        return type; 
    }

    /**
     * @brief Restituisce il momento di generazione.
     * @return LocalDateTime nativo.
     */
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }

    /**
     * @brief Metodo polimorfico che impone alle sottoclassi di auto-tradursi in DTO.
     * @return Il CallbackDTO compilato a dovere con i dati della sottoclasse invocatrice.
     */
    public abstract CallbackDTO toDTO();

    /**
     * @brief Ritorna una traccia descrittiva per debug e logging di rete.
     * @return Formato stringa ClassName[TYPE @ Time].
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + type + " @ " + timestamp + "]";
    }

    // 1. AUTENTICAZIONE E REGISTRAZIONE

    /**
     * @class LoginRequest
     * @brief Comando (Client -> Server): Chiede l'autorizzazione di accesso al sistema.
     */
    public static class LoginRequest extends NetworkMessage {
        
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 2L;
        
        /**
         * @brief Username.
         */
        private final String username;
        
        /**
         * @brief Password in chiaro.
         */
        private final String password;

        /**
         * @brief Inizializza il pacchetto per l'autenticazione.
         * @param username Lo username utente.
         * @param password La password immessa.
         */
        public LoginRequest(String username, String password) {
            super(MessageType.LOGIN_REQUEST);
            this.username = username;
            this.password = password;
        }

        /**
         * @brief Ritorna lo username contenuto.
         * @return Stringa username.
         */
        public String getUsername() { 
            return username; 
        }

        /**
         * @brief Ritorna la password contenuta.
         * @return Stringa password.
         */
        public String getPassword() { 
            return password; 
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType())
                    .credentials(username, password)
                    .build();
        }
    }

    /**
     * @class LoginResponse
     * @brief Notifica (Server -> Client): Riposta con esito della validazione credenziali.
     */
    public static class LoginResponse extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 3L;
        /**
         * @brief Esito dell'autenticazione.
         */
        private final boolean success;

        /**
         * @brief Messaggio di errore mostrato all'utente.
         */
        private final String errorMessage;

        /**
         * @brief Indica se l'utente autenticato ha il ruolo di amministratore.
         */
        private final boolean isAdmin;

        /**
         * @brief Costruisce il responso completo del server.
         * @param success      Esito (true=permesso accordato, false=rifiuto).
         * @param errorMessage Testo di fallimento da inoltrare (null se successo).
         * @param isAdmin      Flag di determinazione privilegi elevati sul db.
         */
        public LoginResponse(boolean success, String errorMessage, boolean isAdmin) {
            super(MessageType.LOGIN_RESPONSE);
            this.success = success;
            this.errorMessage = errorMessage;
            this.isAdmin = isAdmin;
        }

        /**
         * @brief Generatore factory di supporto per un login andato a buon fine.
         * @param isAdmin Specifica i privilegi attribuiti in sessione.
         * @return Pacchetto pre-configurato.
         */
        public static LoginResponse loginSuccess(boolean isAdmin) {
            return new LoginResponse(true, null, isAdmin);
        }

        /**
         * @brief Generatore factory di supporto per un login bloccato o fallito.
         * @param reason La motivazione da rendere all'utente.
         * @return Pacchetto pre-configurato per il fallimento.
         */
        public static LoginResponse loginFailed(String reason) {
            return new LoginResponse(false, reason, false);
        }

        /**
         * @brief Indica il raggiungimento del target funzionale.
         * @return True se login valido.
         */
        public boolean isSuccess() { 
            return success; 
        }

        /**
         * @brief Fornisce i dettagli del mancato accesso.
         * @return Testo descrittivo del problema.
         */
        public String getErrorMessage() { 
            return errorMessage; 
        }

        /**
         * @brief Riporta lo stato dei privilegi speciali.
         * @return True se l'account appartiene a un amministratore.
         */
        public boolean isAdmin() { 
            return isAdmin; 
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType())
                    .success(success)
                    .message(errorMessage)
                    .isAdmin(isAdmin)
                    .build();
        }
    }

    /**
     * @class RegisterRequest
     * @brief Comando (Client -> Server): Inviazione dei dati per creare un nuovo profilo.
     */
    public static class RegisterRequest extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 4L;
        
        /**
         * @brief Username per la registrazione.
         */
        private final String username;
        
        /**
         * @brief Password per la registrazione.
         */
        private final String password;
        
        /**
         * @brief Struttura la richiesta di inserimento.
         * @param username Il nome desiderato (possibile chiave primaria db).
         * @param password Il segreto da conservare.
         */
        public RegisterRequest(String username, String password) {
            super(MessageType.REGISTER_REQUEST);
            this.username = username;
            this.password = password;
        }

        /**
         * @brief Restituisce il nuovo username scelto.
         * @return Stringa nome.
         */
        public String getUsername() { 
            return username; 
        }

        /**
         * @brief Restituisce la nuova password associata.
         * @return Stringa password.
         */
        public String getPassword() { 
            return password; 
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType())
                    .credentials(username, password)
                    .build();
        }
    }

    /**
     * @class RegisterResponse
     * @brief Notifica (Server -> Client): Feedback sull'operazione di iscrizione SQL.
     */
    public static class RegisterResponse extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 5L;
        
        /**
         * @brief Esito della registrazione.
         */
        private final boolean success;
        
        /**
         * @brief Messaggio d'errore eventuale.
         */
        private final String  errorMessage;

        /**
         * @brief Inizializza un messaggio grezzo del risultato di insert.
         * @param success      Esito inserimento record.
         * @param errorMessage Possibile avviso (es. PK_Violation "Nome già in uso").
         */
        public RegisterResponse(boolean success, String errorMessage) {
            super(MessageType.REGISTER_RESPONSE);
            this.success = success;
            this.errorMessage = errorMessage;
        }

        /**
         * @brief Fornisce un blocco base a fronte di una registrazione pulita.
         * @return RegisterResponse con Success = true.
         */
        public static RegisterResponse registerSuccess() {
            return new RegisterResponse(true, null);
        }

        /**
         * @brief Fornisce un blocco base a fronte di una registrazione fallimentare.
         * @param reason La violazione incontrata (es. Username duplicato).
         * @return RegisterResponse con Success = false.
         */
        public static RegisterResponse registerFailed(String reason) {
            return new RegisterResponse(false, reason);
        }

        /**
         * @brief Informa della buona o mala riuscita.
         * @return True se inserimento avvenuto con successo.
         */
        public boolean isSuccess() { 
            return success; 
        }

        /**
         * @brief Messaggio visualizzabile del fallimento per la UI.
         * @return Stringa di avviso.
         */
        public String getErrorMessage() { 
            return errorMessage; 
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType())
                    .success(success)
                    .message(errorMessage)
                    .build();
        }
    }

    // 2. RICERCA PARTITA E ATTESA

    /**
     * @class PlayRequest
     * @brief Comando (Client -> Server): L'utente si mette in coda definendo una difficoltà.
     */
    public static class PlayRequest extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 6L;
        
        /** @brief Il livello di osticità selezionato nei menu dell'interfaccia. */
        private final Difficulty difficulty;

        /**
         * @brief Istanzia una richiesta esplicita di avvio ricerca.
         * @param difficulty L'impostazione di difficoltà che guida la logica lato server.
         */
        public PlayRequest(Difficulty difficulty) {
            super(MessageType.PLAY_REQUEST);
            this.difficulty = difficulty;
        }
        
        /**
         * @brief Restituisce il valore di difficoltà contenuto.
         * @return Valore Enum della difficoltà.
         */
        public Difficulty getDifficulty() { 
            return difficulty; 
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType())
                    .difficulty(difficulty)
                    .build();
        }
    }

    /**
     * @class PlayResponse
     * @brief Notifica (Server -> Client): Aggiornamento real-time dello stato in lobby.
     */
    public static class PlayResponse extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 7L;
        
        /**
         * @brief Stati possibili della risposta.
         */
        private final CallbackDTO.Status status;

        /**
         * @brief Impacchetta lo status fornito dalla procedura di matchmaking in thread.
         * @param status Enum per l'attesa (WAITING) o il reperimento dell'avversario (MATCH_FOUND).
         */
        public PlayResponse(CallbackDTO.Status status) {
            super(MessageType.PLAY_RESPONSE);
            this.status = status;
        }

        /**
         * @brief Mette a disposizione lo status logico di rete.
         * @return L'enumeratore Status.
         */
        public CallbackDTO.Status getStatus() { 
            return status; 
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType())
                    .playStatus(status)
                    .build();
        }
    }

    // 3. FASE DI PARTITA

    /**
     * @class GameStart
     * @brief Comando (Server -> Client): Trasferimento del payload generato alla grafica (Scambio Scene).
     */
    public static class GameStart extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 8L;

        /**
         * @brief Testo estratto dal documento.
         */
        private final String cipheredText;

        /**
         * @brief Durata del timer in secondi.
         */
        private final int timer;

        /**
         * @brief Indice del client ricevente (0 o 1).
         */
        private final int playerIndex;

        /**
         * @brief Username dell'avversario.
         */
        private final String opponentUsername;

        /**
         * @brief Difficoltà della partita.
         */
        private final Difficulty difficulty;

        /**
         * @brief Costruisce il pacchetto di lancio contenente l'arena di sfida generata al volo.
         * @param cipheredText     Lo stralcio testuale criptato prodotto dal QuestionGenerator.
         * @param timer            Tempo totale per la risoluzione assegnato in base alla difficoltà.
         * @param playerIndex      Indice di posizione del client in locale (0 per sx, 1 per dx nel layout).
         * @param opponentUsername Identità dell'avversario frontale.
         * @param difficulty       Difficoltà effettiva della partita.
         */
        public GameStart(String cipheredText, int timer, int playerIndex, String opponentUsername, Difficulty difficulty) {
            super(MessageType.GAME_START);
            this.cipheredText = cipheredText;
            this.timer = timer;
            this.playerIndex = playerIndex;
            this.opponentUsername = opponentUsername;
            this.difficulty = difficulty;
        }

        /**
         * @brief Fornisce il testo mascherato/cifrato.
         * @return La stringa completa dell'indovinello.
         */
        public String getCipheredText() { 
            return cipheredText; 
        }

        /**
         * @brief Fornisce il valore originario di partenza del Timer.
         * @return Valore intero (secondi).
         */
        public int getTimer() { 
            return timer; 
        }

        /**
         * @brief Indica l'assegnazione dello slot visivo.
         * @return Un intero, utile al frontend.
         */
        public int getPlayerIndex() { 
            return playerIndex; 
        }

        /**
         * @brief Indica chi c'è dall'altra parte della sessione virtuale.
         * @return L'username avversario.
         */
        public String getOpponentUsername() { 
            return opponentUsername; 
        }

        /**
         * @brief Indica la difficoltà della partita.
         * @return Livello di difficoltà.
         */
        public Difficulty getDifficulty() {
            return difficulty;
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType())
                    .gameStartData(cipheredText, timer, playerIndex, opponentUsername)
                    .difficulty(difficulty)
                    .build();
        }
    }

    /**
     * @class AnswerSubmission
     * @brief Comando (Client -> Server): L'utente fa un tentativo o invia lo scadere del timeout.
     */
    public static class AnswerSubmission extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 9L;

        /**
         * @brief Parola proposta dall'utente come soluzione.
         */
        private final String proposedWord;
        
        /**
         * @brief Il tempo di risposta in ms.
         */
        private final int responseTime;

        /**
         * @brief Struttura la sottomissione dei calcoli in locale da testare sul back-end.
         * @param proposedWord  Ciò che l'utente ha scritto nel text-field.
         * @param responseTime  Timer rilevato dal frontend (suscettibile a ritardi di rete e calcolato in ms).
         */
        public AnswerSubmission(String proposedWord, int responseTime) {
            super(MessageType.ANSWER_SUBMISSION);
            this.proposedWord = proposedWord;
            this.responseTime = responseTime;
        }

        /**
         * @brief Restituisce il contenuto testato.
         * @return Il testo ipotizzato dal client.
         */
        public String getProposedWord() { 
            return proposedWord; 
        }

        /**
         * @brief Fornisce la misurazione cronometrica.
         * @return Valore in millisecondi.
         */
        public int getResponseTime() { 
            return responseTime; 
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType())
                    .answerData(proposedWord, responseTime)
                    .build();
        }
    }

    /**
     * @class GameResult
     * @brief Notifica (Server -> Client): Fine ufficiale dei giochi, decreto di vittoria/sconfitta.
     */
    public static class GameResult extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 10L;

        /**
         * @brief Risultato del gioco.
         */
        private final Result result;

        /**
         * @brief La parola originale.
         */
        private final String correctWord;

        /**
         * @brief Username del vincitore.
         */
        private final String winnerUsername;
        
        /**
         * @brief Tempo di risposta del vincitore in ms.
         */
        private final int winnerResponseTime;

        /**
         * @brief Composizione del tabellone finale da proiettare post-partita.
         * @param result             Verdetto strettamente relativo per il fruitore specifico (Io = Vinto, Tu = Perso).
         * @param correctWord        La parola svelata, in chiaro, letta dal db per risoluzione educativa al giocatore battuto.
         * @param winnerUsername     Chi ha dato per primo la soluzione (usato graficamente per "Il vincitore è X").
         * @param winnerResponseTime Il tempo che ha sancito il termine cronometrico.
         */
        public GameResult(Result result, String correctWord, String winnerUsername, int winnerResponseTime) {
            super(MessageType.GAME_RESULT);
            this.result = result;
            this.correctWord = correctWord;
            this.winnerUsername = winnerUsername;
            this.winnerResponseTime = winnerResponseTime;
        }

        /**
         * @brief Mostra al ricevente il suo bilancio.
         * @return Il verdetto personale relativo al ricevitore di questo messaggio.
         */
        public Result getResult() { 
            return result; 
        }

        /**
         * @brief Svela il mistero alla base della challenge generata in GameStart.
         * @return L'originale parola estratta.
         */
        public String getCorrectWord() { 
            return correctWord; 
        }

        /**
         * @brief Mostra il colpevole della fine della partita.
         * @return Username vincente (o null).
         */
        public String getWinnerUsername() { 
            return winnerUsername; 
        }

        /**
         * @brief Valore comparativo finale.
         * @return Millisecondi del timer.
         */
        public int getWinnerResponseTime() { 
            return winnerResponseTime; 
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType())
                    .gameResultData(result, correctWord, winnerUsername, winnerResponseTime)
                    .build();
        }
    }

    /**
     * @class OpponentAnswered
     * @brief Segnale "Ghost" (Server -> Client): Avviso in real-time che l'avversario ha risposto.
     */
    public static class OpponentAnswered extends NetworkMessage {
        private static final long serialVersionUID = 11L;

        /**
         * @brief Inizializza un tracciante di rete vuoto senza payload.
         */
        public OpponentAnswered() { 
            super(MessageType.OPPONENT_ANSWERED); 
        }

        @Override
        public CallbackDTO toDTO() { 
            return new CallbackDTO.Builder(getType()).build(); 
        }
    }

    /**
     * @class WrongAnswer
     * @brief Segnale (Server -> Client): Il tentativo di risposta inviato dal Client è errato.
     */
    public static class WrongAnswer extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 12L;

        /** 
         * @brief Inizializza l'avviso di fallimento del tentativo.
         */
        public WrongAnswer() { 
            super(MessageType.WRONG_ANSWER); 
        }

        @Override
        public CallbackDTO toDTO() { 
            return new CallbackDTO.Builder(getType()).build(); 
        }
    }

    // 4. GESTIONE DELLA CONNESSIONE
    
    /**
     * @class OpponentDisconnected
     * @brief Segnale d'errore (Server -> Client): Arresto della partita a causa di crollo della controparte.
     */
    public static class OpponentDisconnected extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 13L;

        /**
         * @brief Inizializza l'avviso di fallimento TCP dell'avversario.
         */
        public OpponentDisconnected() { 
            super(MessageType.OPPONENT_DISCONNECTED); 
        }

        @Override
        public CallbackDTO toDTO() { 
            return new CallbackDTO.Builder(getType()).build(); 
        }
    }

    /**
     * @class ClientDisconnect
     * @brief Comando (Client -> Server): Avviso pulito (graceful shutdown) di disconnessione o logout.
     */
    public static class ClientDisconnect extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 14L;

        /**
         * @brief Inizializza la richiesta volontaria di distruzione del socket lato server.
         */
        public ClientDisconnect() { 
            super(MessageType.CLIENT_DISCONNECT); 
        }

        @Override
        public CallbackDTO toDTO() { 
            return new CallbackDTO.Builder(getType()).build(); 
        }
    }

    // 5. STORICO (HISTORY)
    
    /**
     * @class HistoryRequest
     * @brief Comando (Client -> Server): Ping di richiesta dei dati SQL completi dell'utente loggato.
     */
    public static class HistoryRequest extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 15L;

        /**
         * @brief Inizializza una richiesta priva di argomenti (il server lo ricava dal socket in sessione).
         */
        public HistoryRequest() { 
            super(MessageType.HISTORY_REQUEST); 
        }

        @Override
        public CallbackDTO toDTO() { 
            return new CallbackDTO.Builder(getType()).build(); 
        }
    }

    /**
     * @class HistoryResponse
     * @brief Risposta Data-Heavy (Server -> Client): Pacchetto contenente il database riassuntivo del player.
     */
    public static class HistoryResponse extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 16L;
        
        /**
         * @brief Lista recod delle partite.
         */
        private final List<CallbackDTO.MatchRecord> matchHistory;
        
        /**
         * @brief Numero totale di vittorie dell'utente.
         */
        private final int totalMatchesWon;

        /**
         * @brief Numero totale di partite disputate.
         */
        private final int totalMatchesPlayed;

        /**
         * @brief Tempo medio di risposta in ms.
         */
        private final double avgResponseTime;
        
        /**
         * @brief Tempo totale giocato in secondi.
         */
        private final int totalPlayedTime;

        /**
         * @brief Compatta ed invia le queries sommate da GameDAO e PlayerDAO.
         * @param matchHistory       Mappatura delle singole entries nella tabella Game per quel Player.
         * @param totalMatchesWon    Estrapolato da PlayerDAO (Vittorie).
         * @param totalMatchesPlayed Estrapolato da PlayerDAO (Partite Giocate).
         * @param avgResponseTime    Operazione di divisione (Tempo Totale / Partite Giocate).
         * @param totalPlayedTime    Estrapolato da PlayerDAO (Costo computazionale in tempo di reazione accumulato).
         */
        public HistoryResponse(List<CallbackDTO.MatchRecord> matchHistory, 
                               int totalMatchesWon, int totalMatchesPlayed, 
                               double avgResponseTime, int totalPlayedTime) {
            super(MessageType.HISTORY_RESPONSE);
            this.matchHistory = matchHistory;
            this.totalMatchesWon = totalMatchesWon;
            this.totalMatchesPlayed = totalMatchesPlayed;
            this.avgResponseTime = avgResponseTime;
            this.totalPlayedTime = totalPlayedTime;
        }

        /**
         * @brief Consente alla UI di renderizzare la TableView coi i record delle sessioni.
         * @return La lista dei records del giocatore corrente.
         */
        public List<CallbackDTO.MatchRecord> getMatchHistory() { 
            return matchHistory; 
        }

        /**
         * @brief Consente di mappare un contatore di vittorie UI o un indicatore grafico (Dashboard Label).
         * @return Int contatore.
         */
        public int getTotalMatchesWon() { 
            return totalMatchesWon; 
        }

        /**
         * @brief Consente di calcolare statistiche di base come il 'Win Rate' UI (Vittorie / Totali).
         * @return Int totale.
         */
        public int getTotalMatchesPlayed() { 
            return totalMatchesPlayed; 
        }

        /**
         * @brief Misurazione prestazionale su tutto l'account utente.
         * @return Double media pesata.
         */
        public double getAvgResponseTime() { 
            return avgResponseTime; 
        }

        /**
         * @brief Statistica aggiuntiva da renderizzare, esprimente l'attività globale.
         * @return Int tempo.
         */
        public int getTotalPlayedTime() { 
            return totalPlayedTime; 
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType())
                    .historyData(matchHistory, totalMatchesWon, totalMatchesPlayed, avgResponseTime, totalPlayedTime)
                    .build();
        }
    }

    // 6. UTILITY

    /**
     * @class TextMessage
     * @brief Tool interno: Consente il transito di comandi diretti da terminale o popup generici e disaccoppiati.
     */
    public static class TextMessage extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 17L;
        /**
         * @brief Testo del messaggio.
         */
        private final String text;

        /**
         * @brief Impacchetta una conversazione diretta per scopi di notifica/alert testuali.
         * @param text La stringa da recapitare.
         */
        public TextMessage(String text) {
            super(MessageType.TEXT_MESSAGE);
            this.text = text;
        }

        /**
         * @brief Estrae la notifica.
         * @return Messaggio testuale.
         */
        public String getText() { 
            return text; 
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType())
                    .message(text)
                    .build();
        }
    }
}