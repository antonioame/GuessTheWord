package gruppo05.gtwshared.networking;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import gruppo05.gtwshared.dto.CallbackDTO;
import gruppo05.gtwshared.utility.Result;

/**
 * @class NetworkMessage
 * @brief Classe base astratta di tutti i messaggi scambiati in rete tra server e client.
 * * @details Ogni NetworkMessage trasporta un {@link MessageType} che consente al ricevente di 
 * capire immediatamente come interpretare il payload. Grazie al metodo astratto {@link #toDTO()},
 * ogni pacchetto di rete ricevuto sa come "auto-convertirsi" in un oggetto DTO sicuro 
 * per essere elaborato dall'interfaccia grafica.
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
 * @author chiara
 * @version 2.2
 */
public abstract class NetworkMessage implements Serializable {

    /**
     * @brief Versione di serializzazione.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @brief Tipo del messaggio, usato per il dispatch.
     */
    private final MessageType type;

    /**
     * @brief Timestamp di creazione del messaggio.
     */
    private final LocalDateTime timestamp;

    /**
     * @brief Costruttore base invocato dalle sottoclassi.
     * @param type Il tipo specifico del messaggio di rete.
     */
    protected NetworkMessage(MessageType type) {
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * @brief Restituisce il tipo di messaggio.
     * @return L'enumeratore MessageType.
     */
    public MessageType getType() { 
        return type; 
    }

    /**
     * @brief Restituisce il momento esatto in cui il messaggio è stato creato.
     * @return Un oggetto LocalDateTime.
     */
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }

    /**
     * @brief Converte il pacchetto di rete nel DTO corrispondente per la UI.
     * @details Metodo astratto che deve essere implementato da tutti i payload specifici.
     * @return Una nuova istanza di CallbackDTO popolata con i dati del messaggio.
     */
    public abstract CallbackDTO toDTO();

    /**
     * @brief Genera una rappresentazione testuale del messaggio per finalità di log o debug.
     * @return Una stringa contenente nome classe, tipo e timestamp.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + type + " @ " + timestamp + "]";
    }

    // 1. AUTENTICAZIONE E REGISTRAZIONE

    /**
     * @class LoginRequest
     * @brief Messaggio Client -> Server: richiesta di accesso.
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
         * @brief Costruisce una richiesta di login.
         * @param username L'username digitato dall'utente.
         * @param password La password digitata dall'utente.
         */
        public LoginRequest(String username, String password) {
            super(MessageType.LOGIN_REQUEST);
            this.username = username;
            this.password = password;
        }

        /** @return L'username inserito. */
        public String getUsername() { 
            return username; 
        }

        /** @return La password inserita. */
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
     * @brief Messaggio Server -> Client: esito del tentativo di accesso.
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
         * @brief Costruisce la risposta del server al login.
         * @param success true se le credenziali sono valide, false altrimenti.
         * @param errorMessage Eventuale messaggio di errore da mostrare (null se success).
         * @param isAdmin true se l'utente autenticato è un amministratore.
         */
        public LoginResponse(boolean success, String errorMessage, boolean isAdmin) {
            super(MessageType.LOGIN_RESPONSE);
            this.success = success;
            this.errorMessage = errorMessage;
            this.isAdmin = isAdmin;
        }

        /**
         * @brief Costruttore statico agevolato per generare una risposta di successo.
         * @param isAdmin Specifica se l'utente ha i permessi admin.
         * @return L'istanza configurata di LoginResponse.
         */
        public static LoginResponse loginSuccess(boolean isAdmin) {
            return new LoginResponse(true, null, isAdmin);
        }

        /**
         * @brief Costruttore statico agevolato per generare una risposta di fallimento.
         * @param reason La stringa descrittiva del motivo del rifiuto.
         * @return L'istanza configurata di LoginResponse.
         */
        public static LoginResponse loginFailed(String reason) {
            return new LoginResponse(false, reason, false);
        }

        /** @return L'esito del login. */
        public boolean isSuccess() { 
            return success; 
        }

        /** @return La stringa di errore (se presente). */
        public String getErrorMessage() { 
            return errorMessage; 
        }

        /** @return Lo stato dei privilegi amministrativi. */
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
     * @brief Messaggio Client -> Server: richiesta di registrazione nuovo account.
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
         * @brief Costruisce la richiesta di registrazione.
         * @param username L'username scelto.
         * @param password La password scelta.
         */
        public RegisterRequest(String username, String password) {
            super(MessageType.REGISTER_REQUEST);
            this.username = username;
            this.password = password;
        }

        /** @return L'username scelto. */
        public String getUsername() { 
            return username; 
        }

        /** @return La password scelta. */
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
     * @brief Messaggio Server -> Client: esito del tentativo di registrazione.
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
         * @brief Costruisce la risposta del server alla registrazione.
         * @param success true se l'account è stato creato, false altrimenti.
         * @param errorMessage Causa del fallimento (es. "Username già in uso").
         */
        public RegisterResponse(boolean success, String errorMessage) {
            super(MessageType.REGISTER_RESPONSE);
            this.success = success;
            this.errorMessage = errorMessage;
        }

        /**
         * @brief Costruttore statico per registrazione riuscita.
         * @return Un'istanza configurata di RegisterResponse.
         */
        public static RegisterResponse registerSuccess() {
            return new RegisterResponse(true, null);
        }

        /**
         * @brief Costruttore statico per registrazione fallita.
         * @param reason Descrizione dell'errore.
         * @return Un'istanza configurata di RegisterResponse.
         */
        public static RegisterResponse registerFailed(String reason) {
            return new RegisterResponse(false, reason);
        }

        /** @return L'esito dell'operazione. */
        public boolean isSuccess() { 
            return success; 
        }

        /** @return Il messaggio di errore, se presente. */
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
     * @brief Messaggio Client -> Server: l'utente segnala la volontà di avviare una partita.
     */
    public static class PlayRequest extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 6L;

        /** @brief Costruisce il messaggio senza payload (basta il tipo). */
        public PlayRequest() {
            super(MessageType.PLAY_REQUEST);
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType()).build();
        }
    }

    /**
     * @class PlayResponse
     * @brief Messaggio Server -> Client: esito della ricerca avversario.
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
         * @brief Costruisce il messaggio di risposta dello stato di gioco.
         * @param status Il valore enumerato Status del DTO (MATCH_FOUND o WAITING).
         */
        public PlayResponse(CallbackDTO.Status status) {
            super(MessageType.PLAY_RESPONSE);
            this.status = status;
        }

        /** @return Lo stato elaborato dal server. */
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
     * @brief Messaggio Server -> Client: notifica dell'inizio effettivo della sfida.
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
         * @brief Codice identificativo della sfida.
         */
        private final int challengeCode;

        /**
         * @brief Costruisce il pacchetto di avvio partita contenente tutti i dati necessari.
         * @param cipheredText     Il testo da decifrare fornito al client.
         * @param timer            Secondi concessi per trovare la soluzione.
         * @param playerIndex      Indice identificativo del giocatore corrente (es. 0 o 1).
         * @param opponentUsername L'username dello sfidante.
         * @param challengeCode    L'ID della sfida.
         */
        public GameStart(String cipheredText, int timer, int playerIndex, String opponentUsername, int challengeCode) {
            super(MessageType.GAME_START);
            this.cipheredText = cipheredText;
            this.timer = timer;
            this.playerIndex = playerIndex;
            this.opponentUsername = opponentUsername;
            this.challengeCode = challengeCode;
        }

        /** @return Il testo cifrato. */
        public String getCipheredText() { 
            return cipheredText; 
        }

        /** @return I secondi previsti dal timer. */
        public int getTimer() { 
            return timer; 
        }

        /** @return L'indice identificativo del giocatore corrente. */
        public int getPlayerIndex() { 
            return playerIndex; 
        }

        /** @return L'username dell'avversario. */
        public String getOpponentUsername() { 
            return opponentUsername; 
        }

        /** @return Il codice della partita in corso. */
        public int getChallengeCode() { 
            return challengeCode; 
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType())
                    .gameStartData(cipheredText, timer, playerIndex, opponentUsername, challengeCode)
                    .build();
        }
    }

    /**
     * @class AnswerSubmission
     * @brief Messaggio Client -> Server: inoltro della soluzione elaborata dall'utente.
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
         * @brief Costruisce la risposta inviata dal client.
         * @param proposedWord La stringa immessa dall'utente.
         * @param responseTime Il tempo misurato dal client.
         */
        public AnswerSubmission(String proposedWord, int responseTime) {
            super(MessageType.ANSWER_SUBMISSION);
            this.proposedWord = proposedWord;
            this.responseTime = responseTime;
        }

        /** @return La parola inviata. */
        public String getProposedWord() { 
            return proposedWord; 
        }

        /** @return Il tempo registrato. */
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
     * @brief Messaggio Server -> Client: resoconto dei risultati al termine della partita.
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
         * @brief Costruisce il bollettino di fine partita.
         * @param result             Il risultato (WIN, LOSE, DRAW) dal punto di vista del destinatario.
         * @param correctWord        La vera parola in chiaro come riferimento.
         * @param winnerUsername     Lo username di chi ha vinto.
         * @param winnerResponseTime Il tempo del vincitore in ms.
         */
        public GameResult(Result result, String correctWord, String winnerUsername, int winnerResponseTime) {
            super(MessageType.GAME_RESULT);
            this.result = result;
            this.correctWord = correctWord;
            this.winnerUsername = winnerUsername;
            this.winnerResponseTime = winnerResponseTime;
        }

        /** @return L'esito della partita. */
        public Result getResult() { 
            return result; 
        }

        /** @return La parola corretta originaria. */
        public String getCorrectWord() { 
            return correctWord; 
        }

        /** @return Lo username del vincitore (null in caso di parità). */
        public String getWinnerUsername() { 
            return winnerUsername; 
        }

        /** @return Il tempo di chiusura del vincitore in ms. */
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
     * @brief Messaggio Server -> Client: avvisa che l'avversario ha sottomesso una risposta.
     */
    public static class OpponentAnswered extends NetworkMessage {
        private static final long serialVersionUID = 11L;

        /** @brief Costruttore vuoto. */
        public OpponentAnswered() {
            super(MessageType.OPPONENT_ANSWERED);
        }

        @Override
        public CallbackDTO toDTO() {
            return new CallbackDTO.Builder(getType()).build();
        }
    }

    // 4. GESTIONE DELLA CONNESSIONE
    
    /**
     * @class OpponentDisconnected
     * @brief Messaggio Server -> Client: avviso di chiusura inaspettata dell'avversario.
     */
    public static class OpponentDisconnected extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 12L;

        /** @brief Costruttore vuoto. */
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
     * @brief Messaggio Client -> Server: avviso di disconnessione pulita e volontaria.
     */
    public static class ClientDisconnect extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 13L;

        /** @brief Costruttore vuoto. */
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
     * @brief Messaggio Client -> Server: richiede al server di reperire lo storico dal database.
     */
    public static class HistoryRequest extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 14L;

        /** @brief Costruttore vuoto. */
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
     * @brief Messaggio Server -> Client: recapita le statistiche utente e i vecchi match.
     */
    public static class HistoryResponse extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 15L;
        
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
         * @brief Costruisce il pacchetto di dati storici aggregati.
         * @param matchHistory       La lista di DTO MatchRecord contenenti i dettagli delle singole partite.
         * @param totalMatchesWon    Contatore totale delle vittorie.
         * @param totalMatchesPlayed Contatore totale delle partecipazioni.
         * @param avgResponseTime    Media generale del tempo di risposta per quell'utente.
         * @param totalPlayedTime    Secondi complessivi di gameplay accumulati.
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

        /** @return La lista tipizzata delle partite. */
        public List<CallbackDTO.MatchRecord> getMatchHistory() { 
            return matchHistory; 
        }

        /** @return Totale vittorie in carriera. */
        public int getTotalMatchesWon() { 
            return totalMatchesWon; 
        }

        /** @return Totale partite disputate in carriera. */
        public int getTotalMatchesPlayed() { 
            return totalMatchesPlayed; 
        }

        /** @return Media tempo di risposta. */
        public double getAvgResponseTime() { 
            return avgResponseTime; 
        }

        /** @return Tempo cumulativo in app (secondi). */
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
     * @brief Messaggio Server/Client: invio di stringhe di testo generiche (Notifiche/Debug).
     */
    public static class TextMessage extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 16L;
        /**
         * @brief Testo del messaggio.
         */
        private final String text;

        /**
         * @brief Costruisce un pacchetto di testo.
         * @param text Il corpo del messaggio.
         */
        public TextMessage(String text) {
            super(MessageType.TEXT_MESSAGE);
            this.text = text;
        }

        /** @return La stringa testuale contenuta nel pacchetto. */
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