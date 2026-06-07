package gruppo05.gtwshared.networking;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import gruppo05.gtwshared.utility.Result;

/**
 * @brief Classe base di tutti i messaggi scambiati in rete tra server e client.
 * 
 * <p>Ogni NetworkMessage trasporta un MessageType che consente al ricevente di 
 * capire subito come interpretare il payload senza l'uso di catene di instanceof.</p>
 * 
 * <p><b>FLUSSO DI COMUNICAZIONE CLIENT-SERVER</b></p>
 * <ol>
 * <li><b>Autenticazione:</b> Il client invia una {@link NetworkMessage.LoginRequest}. 
 * Il server controlla i dati e risponde con {@link NetworkMessage.LoginResponse}, 
 * specificando anche se l'utente ha privilegi di Amministratore.</li>
 * 
 * <li><b>Registrazione:</b> Se l'utente non è registrato, il client invia una 
 * {@link NetworkMessage.RegisterRequest}. Il server registra l'utente e risponde 
 * con {@link NetworkMessage.RegisterResponse}.</li>
 * 
 * <li><b>Richiesta e Attesa Partita:</b> Dopo il login, il client invia 
 * {@link NetworkMessage.PlayRequest} per cercare un avversario. Il server risponde con 
 * {@link NetworkMessage.PlayResponse}, che indica l'avvio imminente (MATCH_FOUND) 
 * o la necessità di attendere (WAITING).</li>
 * 
 * <li><b>Inizio Partita:</b> Quando due giocatori sono disponibili, il server invia 
 * {@link NetworkMessage.GameStart} contenente il testo cifrato, il timer e l'identificativo dell'avversario.</li>
 * <li><b>Fase di Gioco:</b> 
 * <ul>
 * <li>Il client invia la propria risposta al server tramite {@link NetworkMessage.AnswerSubmission}.</li>
 * <li>Se l'avversario risponde per primo, il server notifica l'altro client con {@link NetworkMessage.OpponentAnswered}.</li>
 * </ul>
 * </li>
 * <li><b>Fine Partita:</b> Il server calcola i risultati e invia {@link NetworkMessage.GameResult} 
 * (vincitore, parola corretta, statistiche) a entrambi i giocatori.</li>
 * <li><b>Storico (Opzionale):</b> In qualsiasi momento fuori dalla partita, il client può richiedere 
 * i propri dati passati inviando {@link NetworkMessage.HistorianRequest}, a cui il server 
 * risponde con {@link NetworkMessage.HistorianResponse}.</li>
 * 
 * <li><b>Disconnessione:</b> 
 * <ul>
 * <li>Se un giocatore si disconnette volontariamente, invia {@link NetworkMessage.ClientDisconnect}.</li>
 * <li>Se un giocatore si disconnette bruscamente o volontariamente durante/prima di una partita, 
 * il server avvisa l'altro giocatore con {@link NetworkMessage.OpponentDisconnected}.</li>
 * </ul>
 * </li>
 * </ol>
 * 
 * * @author chiara
 * * @version 2.0
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
     * @brief Costruttore base.
     * @param[in] type Tipo del messaggio.
     */
    protected NetworkMessage(MessageType type) {
        this.type      = type;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * @brief Metodo Getter per recuperare il tipo di messaggio.
     * @return Il tipo del messaggio.
     */
    public MessageType getType() { 
        return type; 
    }

    /**
     * @brief Metodo Getter per recuperare il timestamp di creazione.
     * @return Il timestamp di creazione.
     */
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }

    /**
     * @brief Genera una rappresentazione testuale del messaggio.
     * @return Una stringa che descrive il messaggio.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + type + " @ " + timestamp + "]";
    }

    // 1. AUTENTICAZIONE

    /**
     * @brief Client -> Server: richiesta di login.
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
         * @brief Costruttore.
         * @param[in] username Username inserito dall'utente.
         * @param[in] password Password in chiaro.
         */
        public LoginRequest(String username, String password) {
            super(MessageType.LOGIN_REQUEST);
            this.username = username;
            this.password = password;
        }

        // Metodi Getter
        /**
         * @brief Getter dello username.
         * @return Username dell'utente.
         */
        public String getUsername() { 
            return username; 
        }
        
        /**
         * @brief Getter della password.
         * @return Password dell'utente.
         */
        public String getPassword() { 
            return password; 
        }
    }

    /**
     * @brief Server -> Client: risposta al tentativo di login.
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
         * @brief Costruttore.
         * @param[in] success      true se le credenziali sono valide.
         * @param[in] errorMessage Messaggio d'errore (usato solo se success == false).
         * @param[in] isAdmin      true se l'utente ha ruolo amministratore.
         */
        public LoginResponse(boolean success, String errorMessage, boolean isAdmin) {
            super(MessageType.LOGIN_RESPONSE);
            this.success      = success;
            this.errorMessage = errorMessage;
            this.isAdmin      = isAdmin;
        }

        /**
         * @brief Metodo statico per risposta di successo.
         * @param[in] isAdmin Indica se è amministratore.
         * @return Un nuovo oggetto LoginResponse di successo.
         */
        public static LoginResponse loginSuccess(boolean isAdmin) {
            return new LoginResponse(true, null, isAdmin);
        }

        /**
         * @brief Metodo statico per risposta di fallimento.
         * @param[in] reason Causa del fallimento.
         * @return Un nuovo oggetto LoginResponse di fallimento.
         */
        public static LoginResponse loginFailed(String reason) {
            return new LoginResponse(false, reason, false);
        }

        // Metodi Getter
        /**
         * @brief Restituisce l'esito.
         * @return true se successo, false altrimenti.
         */
        public boolean isSuccess(){ 
            return success; 
        }
        
        /**
         * @brief Restituisce il messaggio d'errore.
         * @return Il messaggio d'errore.
         */
        public String getErrorMessage(){ 
            return errorMessage; 
        }
        
        /**
         * @brief Restituisce lo stato admin.
         * @return true se amministratore.
         */
        public boolean isAdmin(){ 
            return isAdmin; 
        }
    }

    /**
     * @brief Client -> Server: richiesta di registrazione di un nuovo user.
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
         * @brief Costruttore.
         * @param[in] username Username inserito dall'utente.
         * @param[in] password Password in chiaro.
         */
        public RegisterRequest(String username, String password) {
            super(MessageType.REGISTER_REQUEST);
            this.username = username;
            this.password = password;
        }

        // Metodi Getter
        /**
         * @brief Restituisce lo username.
         * @return Lo username.
         */
        public String getUsername() { 
            return username; 
        }
        
        /**
         * @brief Restituisce la password.
         * @return La password.
         */
        public String getPassword() { 
            return password; 
        }
    }

    
    /**
     * @brief Server -> Client: risposta alla registrazione.
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
         * @brief Costruttore.
         * @param[in] success      true se le credenziali sono valide.
         * @param[in] errorMessage Messaggio d'errore (usato solo se success == false).
         */
        public RegisterResponse(boolean success, String errorMessage) {
            super(MessageType.REGISTER_RESPONSE);
            this.success      = success;
            this.errorMessage = errorMessage;
        }

        /**
         * @brief Metodo statico per risposta di successo.
         * @return Un oggetto RegisterResponse di successo.
         */
        public static RegisterResponse registerSuccess() {
            return new RegisterResponse(true, null);
        }

        /**
         * @brief Metodo statico per risposta di fallimento.
         * @param[in] reason Motivo del fallimento.
         * @return Un oggetto RegisterResponse di fallimento.
         */
        public static RegisterResponse registerFailed(String reason) {
            return new RegisterResponse(false, reason);
        }

        // Metodi Getter
        /**
         * @brief Restituisce l'esito.
         * @return true se successo.
         */
        public boolean isSuccess() {
            return success; 
        }
        
        /**
         * @brief Restituisce il messaggio d'errore.
         * @return Il messaggio.
         */
        public String getErrorMessage() { 
            return errorMessage; 
        }
    }

    // 2. RICERCA PARTITA E ATTESA

    /**
     * @brief Client -> Server: l'utente loggato richiede di avviare una partita.
     */
    public static class PlayRequest extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 6L;

        /**
         * @brief Costruttore senza parametri.
         */
        public PlayRequest() {
            super(MessageType.PLAY_REQUEST);
        }
    }

    /**
     * @brief Server -> Client: esito della richiesta di gioco.
     */
    public static class PlayResponse extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 7L;

        /**
         * @brief Stati possibili della risposta.
         */
        public enum Status { 
            /**
             * @brief Partita trovata, l'avversario è pronto.
             */
            MATCH_FOUND, 
            /**
             * @brief In attesa che un altro giocatore faccia richiesta.
             */
            WAITING 
        }

        /**
         * @brief Stato della richiesta elaborata dal server.
         */
        private final Status status;

        /**
         * @brief Costruttore.
         * @param[in] status Lo stato della richiesta (MATCH_FOUND o WAITING).
         */
        public PlayResponse(Status status) {
            super(MessageType.PLAY_RESPONSE);
            this.status = status;
        }

        /**
         * @brief Restituisce lo stato della richiesta.
         * @return Lo stato elaborato dal server.
         */
        public Status getStatus() {
            return status;
        }
    }

    // 3. PARTITA

    /**
     * @brief Server -> Client: la sfida ha inizio.
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
         * @brief Costruttore.
         * @param[in] cipheredText     Testo con parola/e cifrate col cifrario di Cesare.
         * @param[in] timer            Secondi a disposizione.
         * @param[in] playerIndex      Indice del giocatore destinatario (0 o 1).
         * @param[in] opponentUsername Username dell'altro giocatore.
         * @param[in] challengeCode    Codice della partita.
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
        /**
         * @brief Restituisce il testo cifrato.
         * @return Il testo cifrato.
         */
        public String getCipheredText() { 
            return cipheredText; 
        }
        
        /**
         * @brief Restituisce il timer.
         * @return I secondi del timer.
         */
        public int getTimer() { 
            return timer; 
        }
        
        /**
         * @brief Restituisce l'indice.
         * @return L'indice del giocatore.
         */
        public int getPlayerIndex() { 
            return playerIndex; 
        }
        
        /**
         * @brief Restituisce lo username avversario.
         * @return Lo username.
         */
        public String getOpponentUsername() { 
            return opponentUsername; 
        }
        
        /**
         * @brief Restituisce il codice partita.
         * @return Il codice.
         */
        public int getChallengeCode(){
            return challengeCode;
        }
    }

    
    /**
     * @brief Client -> Server: l'utente invia la propria risposta.
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
         * @brief Tempo impiegato a rispondere in millisecondi.
         */
        private final long responseTime;

        /**
         * @brief Costruttore.
         * @param[in] proposedWord Parola proposta.
         * @param[in] responseTime Tempo impiegato per rispondere in millisecondi.
         */
        public AnswerSubmission(String proposedWord, long responseTime) {
            super(MessageType.ANSWER_SUBMISSION);
            this.proposedWord   = proposedWord;
            this.responseTime = responseTime;
        }

        // Metodi Getter
        /**
         * @brief Restituisce la parola proposta.
         * @return La parola.
         */
        public String getProposedWord() {
            return proposedWord; 
        }
        
        /**
         * @brief Restituisce il tempo impiegato.
         * @return Il tempo in ms.
         */
        public long getResponseTime() { 
            return responseTime; 
        }
    }

    /**
     * @brief Server -> Client: esito della partita.
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
        private final long winnerResponseTime;

        /**
         * @brief Costruttore.
         * @param[in] result             Esito.
         * @param[in] correctWord        Parola corretta.
         * @param[in] winnerUsername     Username del vincitore.
         * @param[in] winnerResponseTime Tempo di risposta del vincitore in ms.
         */
        public GameResult(Result result, String correctWord,String winnerUsername, long winnerResponseTime){
            super(MessageType.GAME_RESULT);
            this.result               = result;
            this.correctWord          = correctWord;
            this.winnerUsername       = winnerUsername;
            this.winnerResponseTime   = winnerResponseTime;
        }

        // Metodi Getter.
        /**
         * @brief Restituisce l'esito.
         * @return Il risultato.
         */
        public Result getResult() { 
            return result; 
        }
        
        /**
         * @brief Restituisce la parola corretta.
         * @return La parola.
         */
        public String getCorrectWord() { 
            return correctWord; 
        }
        
        /**
         * @brief Restituisce lo username del vincitore.
         * @return Lo username.
         */
        public String getWinnerUsername() { 
            return winnerUsername; 
        }
        
        /**
         * @brief Restituisce il tempo del vincitore.
         * @return Il tempo in ms.
         */
        public long getWinnerResponseTime() { 
            return winnerResponseTime; 
        }
    }

    
    /**
     * @brief Server -> Client: notifica che l'avversario ha inviato una risposta.
     */
    public static class OpponentAnswered extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 11L;

        /**
         * @brief Costruttore senza parametri.
         */
        public OpponentAnswered() {
            super(MessageType.OPPONENT_ANSWERED);
        }
    }

    // 4. GESTIONE DELLA CONNESSIONE
    
    /**
     * @brief Server -> Client: l'avversario si è disconnesso.
     */
    public static class OpponentDisconnected extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 12L;

        /**
         * @brief Costruttore senza parametri.
         */
        public OpponentDisconnected() {
            super(MessageType.OPPONENT_DISCONNECTED);
        }
    }

    
    /**
     * @brief Client -> Server: il client si sta disconnettendo volontariamente.
     */
    public static class ClientDisconnect extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 13L;

        /**
         * @brief Costruttore senza parametri.
         */
        public ClientDisconnect() {
            super(MessageType.CLIENT_DISCONNECT);
        }
    }

    // 5. STORICO
    
    /**
     * @brief Client -> Server: richiesta dello storico partite.
     */
    public static class HistorianRequest extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 14L;

        /**
         * @brief Costruttore senza parametri.
         */
        public HistorianRequest() {
            super(MessageType.HISTORIAN_REQUEST);
        }
    }

    /**
     * @brief Server -> Client: risposta con lo storico partite.
     */
    public static class HistorianResponse extends NetworkMessage {
        /**
         * @brief Versione di serializzazione.
         */
        private static final long serialVersionUID = 15L;

        /**
         * @brief Lista di record partita.
         * <p>Ogni mappa contiene coppie chiave-valore come:</p>
         * <ul>
         * <li>{@code "opponent"}     – username avversario</li>
         * <li>{@code "result"}       – WIN / LOSE / DRAW / TIMEOUT</li>
         * <li>{@code "date"}         – data e ora della partita</li>
         * <li>{@code "responseTime"} – tempo di risposta in ms</li>
         * </ul>
         */
        private final List<Map<String, String>> matchHistory;

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
         * @brief Costruttore.
         * @param[in] matchHistory       Lista di record partita.
         * @param[in] totalMatchesWon    Totale partite vinte.
         * @param[in] totalMatchesPlayed Totale partite.
         * @param[in] avgResponseTime    Tempo medio di risposta.
         * @param[in] totalPlayedTime    Tempo totale giocato.
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
        /**
         * @brief Restituisce lo storico partite.
         * @return La lista dei record partita.
         */
        public List<Map<String, String>> getMatchHistory() { 
            return matchHistory; 
        }
        
        /**
         * @brief Restituisce le partite vinte.
         * @return Il numero di partite vinte.
         */
        public int getTotalMatchesWon() { 
            return totalMatchesWon; 
        }
        
        /**
         * @brief Restituisce le partite giocate.
         * @return Il numero di partite giocate.
         */
        public int getTotalMatchesPlayed() { 
            return totalMatchesPlayed; 
        }
        
        /**
         * @brief Restituisce il tempo medio.
         * @return Il tempo medio di risposta in ms.
         */
        public double getAvgResponseTime() { 
            return avgResponseTime; 
        }
        
        /**
         * @brief Restituisce il tempo giocato.
         * @return Il tempo giocato totale in secondi.
         */
        public int getTotalPlayedTime() {
            return totalPlayedTime;
        }
    }

    // 6. UTILITY

    /**
     * @brief Messaggio di testo generico.
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
         * @brief Costruttore.
         * @param[in] text Il testo del messaggio da trasportare.
         */
        public TextMessage(String text) {
            super(MessageType.TEXT_MESSAGE);
            this.text = text;
        }

        /**
         * @brief Restituisce il testo.
         * @return Il testo in formato stringa.
         */
        public String getText() { 
            return text; 
        }
    }
}