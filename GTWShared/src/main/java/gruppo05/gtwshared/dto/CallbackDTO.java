package gruppo05.gtwshared.dto;

import java.time.LocalDateTime;
import java.util.List;

import gruppo05.gtwshared.networking.MessageType;
import gruppo05.gtwshared.networking.NetworkMessage; 
import gruppo05.gtwshared.utility.Result;

/**
 * @class CallbackDTO
 * @brief DTO flessibile per raggruppare e trasportare i dati da passare alle callback 
 * dell'interfaccia grafica.
 * 
 * * @details Questa classe utilizza il pattern architetturale Builder per garantire un'inizializzazione 
 * chiara, sicura e pulita. Permette di costruire oggetti immutabili valorizzando esclusivamente 
 * i campi strettamente necessari in base al {@link MessageType} ricevuto, ignorando gli altri.
 * 
 * @author chiara
 * @version 2.0
 */
public class CallbackDTO {
    
    /**
     * @class MatchRecord
     * @brief Rappresenta un singolo record cronologico di una partita conclusa nello storico dell'utente.
     */
    public static class MatchRecord {
        private final String opponent;
        private final Result result;
        private final LocalDateTime date; 
        private final int responseTime;

        /**
         * @brief Costruisce un nuovo record per lo storico partite.
         * 
         * @param opponent     Username dell'avversario affrontato nella partita.
         * @param result       Esito finale della partita (WIN, LOSE, DRAW, TIMEOUT).
         * @param date         Data e ora esatta in cui si è svolta la partita.
         * @param responseTime Tempo di risposta medio o totale impiegato (in millisecondi).
         */
        public MatchRecord(String opponent, Result result, LocalDateTime date, int responseTime) {
            this.opponent = opponent;
            this.result = result;
            this.date = date;
            this.responseTime = responseTime;
        }

        /**
         * @brief Restituisce lo username dell'avversario.
         * @return Una stringa contenente il nome dell'avversario.
         */
        public String getOpponent() { 
            return opponent; 
        }
        
        /**
         * @brief Restituisce l'esito della partita.
         * @return Il valore enumerato Result corrispondente all'esito.
         */
        public Result getResult() { 
            return result; 
        }
        
        /**
         * @brief Restituisce la data e l'ora della partita.
         * @return L'oggetto LocalDateTime della partita.
         */
        public LocalDateTime getDate() { 
            return date; 
        }
        
        /**
         * @brief Restituisce il tempo di risposta.
         * @return Il tempo registrato in millisecondi.
         */
        public int getResponseTime() { 
            return responseTime; 
        }
    }

    // Campi Generici 
    private final MessageType eventType;
    private final boolean success;
    private final String message; ///< Utilizzato per i messaggi di errore (Login/Register) o per i testi generici (TextMessage).

    // Campi Autenticazione 
    private final String username; 
    private final String password;
    private final boolean isAdmin;

    // Campi Attesa Partita 
    
    /**
     * @enum Status
     * @brief Rappresenta gli stati possibili di una richiesta di ricerca partita.
     */
    public enum Status { 
        /** @brief Partita trovata con successo, l'avversario è pronto. */
        MATCH_FOUND, 
        /** @brief Nessun avversario disponibile, il giocatore viene messo in attesa. */
        WAITING 
    }

    private final Status status; ///< Stato della richiesta elaborata dal server (MATCH_FOUND o WAITING).

    // Campi Partita 
    private final String cipheredText;
    private final int timer;
    private final int playerIndex; ///< Indice identificativo del client all'interno della partita (0 o 1).
    private final String opponentUsername;
    private final int challengeCode;
    private final String proposedWord;
    private final String correctWord;
    private final Result gameResult;
    private final String winnerUsername; ///< Username del giocatore che ha vinto (può essere null in caso di pareggio).
    private final int responseTime; ///< Utilizzato per inviare il proprio tempo (AnswerSubmission) o per ricevere quello del vincitore (GameResult).

    // Campi Storico (History)
    private final List<MatchRecord> matchHistory;
    private final int totalMatchesWon;
    private final int totalMatchesPlayed;
    private final double avgResponseTime;
    private final int totalPlayedTime;

    /**
     * @brief Costruttore privato del DTO, invocato esclusivamente dal Builder.
     * @param builder L'istanza del Builder contenente tutti i dati pre-configurati.
     */
    private CallbackDTO(Builder builder) {
        this.eventType = builder.eventType;
        this.success = builder.success;
        this.message = builder.message;
        
        this.username = builder.username;
        this.password = builder.password;
        this.isAdmin = builder.isAdmin;

        this.status = builder.status;
        
        this.cipheredText = builder.cipheredText;
        this.timer = builder.timer;
        this.playerIndex = builder.playerIndex;
        this.opponentUsername = builder.opponentUsername;
        this.challengeCode = builder.challengeCode;
        this.proposedWord = builder.proposedWord;
        this.correctWord = builder.correctWord;
        this.gameResult = builder.gameResult;
        this.winnerUsername = builder.winnerUsername;
        this.responseTime = builder.responseTime;
        
        this.matchHistory = builder.matchHistory;
        this.totalMatchesWon = builder.totalMatchesWon;
        this.totalMatchesPlayed = builder.totalMatchesPlayed;
        this.avgResponseTime = builder.avgResponseTime;
        this.totalPlayedTime = builder.totalPlayedTime;
    }

    // Metodi Getter 
    
    /** @return Il tipo di evento/messaggio associato a questo DTO. */
    public MessageType getEventType() { 
        return eventType; 
    }
    
    /** @return True se l'azione di rete è andata a buon fine, false altrimenti. */
    public boolean isSuccess() { 
        return success; 
    }
    
    /** @return Il messaggio di testo o di errore trasportato, se presente. */
    public String getMessage() { 
        return message; 
    }
    
    /** @return Lo username dell'utente coinvolto nell'azione. */
    public String getUsername() { 
        return username; 
    }
    
    /** @return La password associata alla richiesta di autenticazione. */
    public String getPassword() { 
        return password; 
    }
    
    /** @return True se l'utente ha i privilegi di amministratore. */
    public boolean isAdmin() { 
        return isAdmin; 
    }

    /** @return Lo stato della richiesta di gioco (es. MATCH_FOUND o WAITING). */
    public Status getStatus() { 
        return status; 
    }
    
    /** @return Il testo cifrato fornito dal server per la partita attuale. */
    public String getCipheredText() { 
        return cipheredText; 
    }
    
    /** @return I secondi previsti per il timer della partita. */
    public int getTimer() { 
        return timer; 
    }
    
    /** @return L'indice numerico assegnato al giocatore corrente (0 o 1). */
    public int getPlayerIndex() { 
        return playerIndex; 
    }
    
    /** @return L'username dell'avversario per la partita in corso. */
    public String getOpponentUsername() { 
        return opponentUsername; 
    }
    
    /** @return Il codice identificativo univoco della sfida in corso. */
    public int getChallengeCode() { 
        return challengeCode; 
    }
    
    /** @return La parola proposta come soluzione dal giocatore. */
    public String getProposedWord() { 
        return proposedWord; 
    }
    
    /** @return La parola in chiaro (soluzione corretta) rivelata a fine partita. */
    public String getCorrectWord() { 
        return correctWord; 
    }
    
    /** @return Il risultato finale della partita. */
    public Result getGameResult() { 
        return gameResult; 
    }
    
    /** @return L'username del giocatore che ha vinto la sfida (null se pareggio). */
    public String getWinnerUsername() { 
        return winnerUsername; 
    }
    
    /** @return Il tempo di risposta (proprio o del vincitore) espresso in ms. */
    public int getResponseTime() { 
        return responseTime; 
    }
    
    /** @return La lista contenente lo storico strutturato delle partite. */
    public List<MatchRecord> getMatchHistory() { 
        return matchHistory; 
    }
    
    /** @return Il numero totale di partite vinte dall'utente. */
    public int getTotalMatchesWon() { 
        return totalMatchesWon; 
    }
    
    /** @return Il numero complessivo di partite disputate. */
    public int getTotalMatchesPlayed() { 
        return totalMatchesPlayed; 
    }
    
    /** @return Il tempo di risposta medio calcolato su tutte le partite. */
    public double getAvgResponseTime() { 
        return avgResponseTime; 
    }
    
    /** @return Il tempo totale di gioco accumulato dall'utente, in secondi. */
    public int getTotalPlayedTime() { 
        return totalPlayedTime; 
    }


    /**
     * @class Builder
     * @brief Classe interna di supporto per generare istanze di CallbackDTO in modo incrementale.
     */
    public static class Builder {
        private final MessageType eventType;

        private boolean success = true;
        private String message = null;
        private String username = null;
        private String password = null;
        private boolean isAdmin = false;
        private Status status = null;
        private String cipheredText = null;
        private int timer = 0;
        private int playerIndex = 0;
        private String opponentUsername = null;
        private int challengeCode = 0;
        private String proposedWord = null;
        private String correctWord = null;
        private Result gameResult = null;
        private String winnerUsername = null;
        private int responseTime = 0;
        private List<MatchRecord> matchHistory = null;
        private int totalMatchesWon = 0;
        private int totalMatchesPlayed = 0;
        private double avgResponseTime = 0.0;
        private int totalPlayedTime = 0;

        /**
         * @brief Inizializza un nuovo Builder.
         * @param eventType Il tipo di messaggio/evento (l'unico parametro sempre obbligatorio).
         */
        public Builder(MessageType eventType) {
            this.eventType = eventType;
        }

        /**
         * @brief Imposta lo stato di successo per risposte a richieste specifiche (es. Login/Register).
         * @param success true per indicare operazione riuscita, false per fallimento.
         * @return L'istanza corrente del Builder.
         */
        public Builder success(boolean success) { 
            this.success = success; 
            return this; 
        }
        
        /**
         * @brief Imposta un messaggio testuale da veicolare nel DTO (notifica, errore o chat).
         * @param message Il corpo del messaggio.
         * @return L'istanza corrente del Builder.
         */
        public Builder message(String message) { 
            this.message = message; 
            return this; 
        }
        
        /**
         * @brief Imposta l'username dell'utente loggato.
         * @param username L'username da impostare.
         * @return L'istanza corrente del Builder.
         */
        public Builder username(String username) { 
            this.username = username; 
            return this; 
        }
        
        /**
         * @brief Imposta simultaneamente username e password (utile per le fasi di Login e Registrazione).
         * @param username Il nome utente.
         * @param password La password in chiaro.
         * @return L'istanza corrente del Builder.
         */
        public Builder credentials(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }
        
        /**
         * @brief Imposta il flag per definire se l'utente possiede privilegi di amministratore.
         * @param isAdmin true se amministratore, false per i giocatori normali.
         * @return L'istanza corrente del Builder.
         */
        public Builder isAdmin(boolean isAdmin) { 
            this.isAdmin = isAdmin; 
            return this; 
        }

        /**
         * @brief Imposta lo stato della ricerca avversario.
         * @param status Il valore enumerato (MATCH_FOUND o WAITING).
         * @return L'istanza corrente del Builder.
         */
        public Builder playStatus(Status status) { 
            this.status = status; 
            return this; 
        }
        
        /**
         * @brief Imposta i dati necessari al client per iniziare una partita.
         * @param cipheredText     Testo cifrato.
         * @param timer            Secondi disponibili per la risoluzione.
         * @param playerIndex      Indice assegnato al client.
         * @param opponentUsername Username dell'avversario affrontato.
         * @param challengeCode    Codice identificativo della partita.
         * @return L'istanza corrente del Builder.
         */
        public Builder gameStartData(String cipheredText, int timer, int playerIndex, String opponentUsername, int challengeCode) {
            this.cipheredText = cipheredText;
            this.timer = timer;
            this.playerIndex = playerIndex;
            this.opponentUsername = opponentUsername;
            this.challengeCode = challengeCode;
            return this;
        }

        /**
         * @brief Imposta i dati relativi all'invio di una risposta dal giocatore.
         * @param proposedWord La parola ipotizzata e inviata al server.
         * @param responseTime Il tempo di risposta misurato in millisecondi.
         * @return L'istanza corrente del Builder.
         */
        public Builder answerData(String proposedWord, int responseTime) {
            this.proposedWord = proposedWord;
            this.responseTime = responseTime;
            return this;
        }
        
        /**
         * @brief Imposta i dati di riepilogo al termine di una partita.
         * @param gameResult     Il risultato definitivo calcolato dal server.
         * @param correctWord    La vera parola da indovinare (per mostrarla se l'utente ha sbagliato).
         * @param winnerUsername L'username di chi ha vinto.
         * @param responseTime   Il tempo impiegato dal vincitore per trionfare.
         * @return L'istanza corrente del Builder.
         */
        public Builder gameResultData(Result gameResult, String correctWord, String winnerUsername, int responseTime) {
            this.gameResult = gameResult;
            this.correctWord = correctWord;
            this.winnerUsername = winnerUsername;
            this.responseTime = responseTime;
            return this;
        }
        
        /**
         * @brief Imposta i dati globali da restituire per la visualizzazione dello storico utente.
         * @param matchHistory     Lista dei singoli record delle partite giocate.
         * @param totalWon         Conteggio delle vittorie assolute.
         * @param totalPlayed      Conteggio delle partite avviate.
         * @param avgTime          Media del tempo impiegato in tutte le giocate.
         * @param totalTime        Sommatoria del tempo di gioco in secondi.
         * @return L'istanza corrente del Builder.
         */
        public Builder historyData(List<MatchRecord> matchHistory, int totalWon, int totalPlayed, double avgTime, int totalTime) {
            this.matchHistory = matchHistory;
            this.totalMatchesWon = totalWon;
            this.totalMatchesPlayed = totalPlayed;
            this.avgResponseTime = avgTime;
            this.totalPlayedTime = totalTime;
            return this;
        }

        /**
         * @brief Completa l'assemblaggio dei dati e genera il DTO finale.
         * @return Una nuova istanza immutabile di CallbackDTO perfettamente popolata.
         */
        public CallbackDTO build() {
            return new CallbackDTO(this);
        }
    }
}