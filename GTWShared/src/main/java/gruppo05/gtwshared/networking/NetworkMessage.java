package gruppo05.gtwshared.networking;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Classe base di tutti i messaggi scambiati in rete tra server e client.
 *
 * <p>Ogni messaggio porta un {@link MessageType} che identifica immediatamente
 * l'intenzione del mittente, evitando catene di {@code instanceof}.</p>
 *
 * <p>Il pattern scelto è una gerarchia di classi inner statiche,
 * ognuna con i campi specifici del proprio tipo di messaggio.
 * Tutti implementano {@link Serializable} per poter essere trasmessi via
 * {@link java.io.ObjectOutputStream}.</p>
 *
 * @author chiara
 * @version 2.0
 */
public abstract class NetworkMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Tipo del messaggio, usato per il dispatch. */
    private final MessageType type;

    /** Timestamp di creazione del messaggio (lato mittente). */
    private final LocalDateTime timestamp;

    /**
     * Costruttore base.
     *
     * @param type Tipo del messaggio.
     */
    protected NetworkMessage(MessageType type) {
        this.type      = type;
        this.timestamp = LocalDateTime.now();
    }

    /** Metodo Getter per recuperare il {@link MessageType} del messaggio.
     * @return Il tipo del messaggio. */
    public MessageType getType() { 
        return type; 
    }

    /** Metodo Getter per recuperare il timestamp di creazione del messaggio.
     * @return Il timestamp di creazione. */
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + type + " @ " + timestamp + "]";
    }

    // 1. AUTENTICAZIONE

    /**
     * Client -> Server: richiesta di login.
     */
    public static class LoginRequest extends NetworkMessage {
        private static final long serialVersionUID = 2L;
        private final String username;
        private final String password;

        /** Costruttore.
         * @param username Username inserito dall'utente.
         * @param password Password in chiaro (la cifratura avviene lato server).
         */
        public LoginRequest(String username, String password) {
            super(MessageType.LOGIN_REQUEST);
            this.username = username;
            this.password = password;
        }

        // Metodi Getter
        public String getUsername() { 
            return username; 
        }
        
        public String getPassword() { 
            return password; 
        }
    }

    /**
     * Server -> Client: risposta al tentativo di login.
     */
    public static class LoginResponse extends NetworkMessage {
        private static final long serialVersionUID = 3L;
        /**
         * Esito dell'autenticazione.
         *
         * <p>{@code true} se username e password corrispondono a un utente
         * presente nel database; {@code false} altrimenti.</p>
         */
        private final boolean success;

        /**
         * Messaggio di errore da mostrare all'utente in caso di login fallito.
         *
         * <p>Vale {@code null} quando {@code success == true}: se il login
         * è andato a buon fine non c'è nessun errore da comunicare.</p>
         */
        private final String errorMessage;

       /**
        * Indica se l'utente autenticato ha il ruolo di amministratore.
        *
        * <p>Vale {@code false} quando {@code success == false}: se il login
        * non è riuscito non ha senso parlare di ruoli.</p>
        *
        * <p>Il client usa questo campo per decidere quale schermata mostrare
        * dopo il login:</p>
        * <ul>
        *   <li>{@code true}  -> interfaccia di amministrazione del server
        *       (caricamento documenti, visualizzazione classifiche globali)</li>
        *   <li>{@code false} -> interfaccia di gioco normale
        *       (attesa avversario, partita, storico personale)</li>
        * </ul>
        */
        private final boolean isAdmin;


        /** Costruttore.
         * @param success      {@code true} se le credenziali sono valide.
         * @param errorMessage Messaggio d'errore (usato solo se {@code success == false}).
         * @param isAdmin      {@code true} se l'utente ha ruolo amministratore.
         */
        public LoginResponse(boolean success, String errorMessage, boolean isAdmin) {
            super(MessageType.LOGIN_RESPONSE);
            this.success      = success;
            this.errorMessage = errorMessage;
            this.isAdmin      = isAdmin;
        }

        /** Metodo statico per risposta di successo. */
        public static LoginResponse loginSuccess(boolean isAdmin) {
            return new LoginResponse(true, null, isAdmin);
        }

        /** Metodo statico per risposta di fallimento. */
        public static LoginResponse loginFailed(String reason) {
            return new LoginResponse(false, reason, false);
        }

        // Metodi Getter
        public boolean isSuccess(){ 
            return success; 
        }
        
        public String  getErrorMessage(){ 
            return errorMessage; 
        }
        
        public boolean isAdmin(){ 
            return isAdmin; 
        }
    }

    /**
     * Client -> Server: richiesta di registrazione di un nuovo user.
     */
    public static class RegisterRequest extends NetworkMessage {
        private static final long serialVersionUID = 4L;
        private final String username;
        private final String password;
        
        /** Costruttore.
         * @param username Username inserito dall'utente.
         * @param password Password in chiaro (la cifratura avviene lato server).
         */
        public RegisterRequest(String username, String password) {
            super(MessageType.REGISTER_REQUEST);
            this.username = username;
            this.password = password;
        }

        // Metodi Getter
        public String getUsername() { 
            return username; 
        }
        
        public String getPassword() { 
            return password; 
        }
    }

    
    /**
     * Server -> Client: risposta alla registrazione.
     */
    public static class RegisterResponse extends NetworkMessage {
        private static final long serialVersionUID = 5L;
        private final boolean success;
        private final String  errorMessage;

        /** Costruttore.
         * @param success      {@code true} se le credenziali sono valide.
         * @param errorMessage Messaggio d'errore (usato solo se {@code success == false}).
         */
        public RegisterResponse(boolean success, String errorMessage) {
            super(MessageType.REGISTER_RESPONSE);
            this.success      = success;
            this.errorMessage = errorMessage;
        }

        /** Metodo statico per risposta di successo. */
        public static RegisterResponse registerSuccess() {
            return new RegisterResponse(true, null);
        }

        /** Metodo statico per risposta di fallimento. */
        public static RegisterResponse registerFailed(String reason) {
            return new RegisterResponse(false, reason);
        }

        // Metodi Getter
        public boolean isSuccess() {
            return success; 
        }
        
        public String getErrorMessage() { 
            return errorMessage; 
        }
    }

    // 2. ATTESA

    /**
     * Server -> Client: login andato a buon fine, in attesa dell'avversario.
     */
    public static class WaitingForOpponent extends NetworkMessage {
        private static final long serialVersionUID = 6L;

        /** Nome utente dell'utente autenticato (per eventuale visualizzazione). */
        private final String loggedUsername;

        /** Costruttore.
         * @param loggedUsername nome dell'utente autenticato.
         */
        public WaitingForOpponent(String loggedUsername) {
            super(MessageType.WAITING_FOR_OPPONENT);
            this.loggedUsername = loggedUsername;
        }

        // Metodo Getter
        public String getLoggedUsername() { 
            return loggedUsername; 
        }
    }

    // 3. PARTITA

    /**
     * Server -> Client: la sfida ha inizio.
     * <p>Contiene il testo con le parole cifrate evidenziate e la durata del timer.</p>
     */
    public static class GameStart extends NetworkMessage {
        private static final long serialVersionUID = 7L;

        /** Testo estratto dal documento, con parola/e cifrate. */
        private final String cipheredText;

        /** Durata del timer in secondi. */
        private final int timer;

        /** Indice del client ricevente (0 o 1). */
        private final int playerIndex;

        /** Username dell'avversario (per mostrarlo in UI). */
        private final String opponentUsername;
        
        /** Codice identificativo della sfida (vedi {@link Challege.code}). */
        private final int challengeCode;

        /** Costruttore.
         * @param cipheredText      Testo con parola/e cifrate col cifrario di Cesare.
         * @param timer             Secondi a disposizione.
         * @param playerIndex       Indice del giocatore destinatario (0 o 1).
         * @param opponentUsername  Username dell'altro giocatore.
         * @param challengeCode     Codice della partita.
         */
        public GameStart(String cipheredText, int timer, int playerIndex, String opponentUsername, int challengeCode) {
            super(MessageType.GAME_START);
            this.cipheredText     = cipheredText;
            this.timer            = timer;
            this.playerIndex      = playerIndex;
            this.opponentUsername = opponentUsername;
            this.challengeCode    = challengeCode;
        }

        // Metodi Getter
        public String getCipheredText() { 
            return cipheredText; 
        }
        
        public int getTimer() { 
            return timer; 
        }
        
        public int getPlayerIndex() { 
            return playerIndex; 
        }
        
        public String getOpponentUsername() { 
            return opponentUsername; 
        }
        
        public int getChallengeCode(){
            return challengeCode;
        }
    }

    
    /**
     * Client -> Server: l'utente invia la propria risposta (parola decifrata).
     */
    public static class AnswerSubmission extends NetworkMessage {
        private static final long serialVersionUID = 8L;

        /** Parola proposta dall'utente come soluzione. */
        private final String proposedWord;

        /**
         * Tempo impiegato a rispondere in millisecondi (misurato lato client dall'inizio del timer).
         */
        private final long responseTime;

        /** Costruttore.
         * @param proposedWord      Parola proposta.
         * @param responseTime      Tempo impiegato per rispondere
         */
        public AnswerSubmission(String proposedWord, long responseTime) {
            super(MessageType.ANSWER_SUBMISSION);
            this.proposedWord   = proposedWord;
            this.responseTime = responseTime;
        }

        // Metodi Getter
        public String getProposedWord() {
            return proposedWord; 
        }
        
        public long getResponseTime() { 
            return responseTime; 
        }
    }

    /**
     * Server -> Client: esito della partita.
     */
    public static class GameResult extends NetworkMessage {
        private static final long serialVersionUID = 9L;

        /** Esito dal punto di vista del ricevente. */
        public enum Result { WIN, LOSE, DRAW, TIMEOUT }

        private final Result result;

        /** La parola originale (quella che bisognava trovare). */
        private final String correctWord;

        /** Username del vincitore (o {@code null} in caso di pareggio/timeout). */
        private final String winnerUsername;

        /** Tempo di risposta del vincitore in ms. */
        private final long winnerResponseTime;

        /** Costruttore.
         * @param result               Esito.
         * @param correctWord          Parola corretta.
         * @param winnerUsername       Username del vincitore.
         * @param winnerResponseTime   Tempo di risposta del vincitore.
         */
        public GameResult(Result result, String correctWord,String winnerUsername, long winnerResponseTime){
            super(MessageType.GAME_RESULT);
            this.result               = result;
            this.correctWord          = correctWord;
            this.winnerUsername       = winnerUsername;
            this.winnerResponseTime   = winnerResponseTime;
        }

        // Metodi Getter.
        public Result getResult() { 
            return result; 
        }
        
        public String getCorrectWord() { 
            return correctWord; 
        }
        
        public String getWinnerUsername() { 
            return winnerUsername; 
        }
        
        public long getWinnerResponseTime() { 
            return winnerResponseTime; 
        }
    }

    
    /**
     * Server -> Client: notifica che l'avversario ha già inviato una risposta
     *                   senza specificare se è corretta o meno.
     */
    public static class OpponentAnswered extends NetworkMessage {
        private static final long serialVersionUID = 10L;

        public OpponentAnswered() {
            super(MessageType.OPPONENT_ANSWERED);
        }
    }

    // 4. GESTIONE DELLA CONNESSIONE
    
    /**
     * Server -> Client: l'avversario si è disconnesso.
     */
    public static class OpponentDisconnected extends NetworkMessage {
        private static final long serialVersionUID = 11L;

        public OpponentDisconnected() {
            super(MessageType.OPPONENT_DISCONNECTED);
        }
    }

    
    /**
     * Client -> Server: il client si sta disconnettendo volontariamente.
     */
    public static class ClientDisconnect extends NetworkMessage {
        private static final long serialVersionUID = 12L;

        public ClientDisconnect() {
            super(MessageType.CLIENT_DISCONNECT);
        }
    }

    // 5. STORICO
    
    /**
     * Client -> Server: richiesta dello storico partite dell'utente loggato.
     */
    public static class HistorianRequest extends NetworkMessage {
        private static final long serialVersionUID = 13L;

        public HistorianRequest() {
            super(MessageType.HISTORIAN_REQUEST);
        }
    }

    /**
     * Server -> Client: risposta con lo storico partite e le statistiche.
     */
    public static class HistorianResponse extends NetworkMessage {
        private static final long serialVersionUID = 14L;

        /**
         * Lista di record partita. Ogni mappa contiene coppie chiave-valore come:
         * <ul>
         *   <li>{@code "opponent"} – username avversario</li>
         *   <li>{@code "result"}   – WIN / LOSE / DRAW / TIMEOUT</li>
         *   <li>{@code "date"}     – data e ora della partita</li>
         *   <li>{@code "responseTime"} – tempo di risposta in ms</li>
         * </ul>
         */
        private final List<Map<String, String>> matchHistory;

        /** Numero totale di vittorie dell'utente. */
        private final int totalMatchesWon;

        /** Numero totale di partite disputate. */
        private final int totalMatchesPlayed;

        /** Tempo medio di risposta in ms. */
        private final double avgResponseTime;
        
        /** Tempo totale giocato in secondi. */
        private final int totalPlayedTime;

        /** Costruttore.
         * @param matchHistory         Lista di record partita.
         * @param totalMatchesWon      Totale partite vinte.
         * @param totalMatchesPlayed   Totale partite.
         * @param advResponseTime      Tempo medio di risposta.
         * @param totalPlayedTime      Tempo totale giocato.
         */
        public HistorianResponse(List<Map<String, String>> matchHistory, 
                                 int totalMatchesWon, int totalMatchesPlayed, 
                                 double avgResponseTime, int totalPlayedTime) {
            super(MessageType.HISTORIAN_RESPONSE);
            this.matchHistory       = matchHistory;
            this.totalMatchesWon    = totalMatchesWon;
            this.totalMatchesPlayed = totalMatchesPlayed;
            this.avgResponseTime    = avgResponseTime;
            this.totalPlayedTime    = totalPlayedTime;
        }

        // Metodi getter.
        public List<Map<String, String>> getMatchHistory() { 
            return matchHistory; 
        }
        
        public int getTotalMatchesWon() { 
            return totalMatchesWon; 
        }
        
        public int getTotalMatchesPlayed() { 
            return totalMatchesPlayed; 
        }
        
        public double getAvgResponseTime() { 
            return avgResponseTime; 
        }
        
        public int getTotalPlayedTime() {
            return totalPlayedTime;
        }
    }

    // 6. UTILITY

    /**
     * Messaggio di testo generico (debug, notifiche semplici).
     */
    public static class TextMessage extends NetworkMessage {
        private static final long serialVersionUID = 15L;
        private final String text;

        public TextMessage(String text) {
            super(MessageType.TEXT_MESSAGE);
            this.text = text;
        }

        public String getText() { 
            return text; 
        }
    }
}
