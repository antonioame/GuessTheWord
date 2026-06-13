package gruppo05.gtwshared.dto;

import java.time.LocalDateTime;
import java.util.List;

import gruppo05.gtwshared.networking.MessageType;
import gruppo05.gtwshared.networking.NetworkMessage; 
import gruppo05.gtwshared.utility.Result;
import gruppo05.gtwshared.utility.Difficulty;

/**
 * @class CallbackDTO
 * @brief Oggetto per il trasferimento dati (DTO) flessibile per la comunicazione tra livello di Rete e UI.
 * * @details Questa classe implementa il pattern architetturale Builder. Garantisce la creazione di 
 * oggetti immutabili (read-only) le cui variabili vengono inizializzate solo se strettamente 
 * necessarie in base al {@link MessageType} ricevuto, prevenendo stati inconsistenti nella UI.
 * 
 * @version 1.0
 */
public class CallbackDTO {
    
    /**
     * @class MatchRecord
     * @brief Rappresenta in modo compatto un singolo record cronologico di una partita conclusa nello storico.
     */
    public static class MatchRecord {
        
        /** @brief Username dell'avversario sfidato in questa specifica partita. */
        private final String opponent;
        
        /** @brief L'esito finale riportato dal giocatore che richiede lo storico. */
        private final Result result;
        
        /** @brief Timestamp esatto del momento in cui la partita è stata disputata o registrata. */
        private final LocalDateTime date; 
        
        /** @brief Tempo di risposta personale del giocatore nella sfida, espresso in millisecondi. */
        private final int responseTime;

        /**
         * @brief Costruisce un nuovo record immodificabile per lo storico partite.
         * @param opponent     Username dell'avversario.
         * @param result       Esito finale (es. WIN, LOSE).
         * @param date         Data e ora esatta in cui si è svolta la partita.
         * @param responseTime Tempo impiegato dal giocatore.
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
         * @return Il valore enumerato Result (WIN, LOSE, ecc.).
         */
        public Result getResult() { 
            return result; 
        }

        /**
         * @brief Restituisce la data della registrazione del match.
         * @return Oggetto LocalDateTime relativo al match.
         */
        public LocalDateTime getDate() { 
            return date; 
        }

        /**
         * @brief Restituisce il tempo di risposta.
         * @return I millisecondi impiegati nella partita.
         */
        public int getResponseTime() { 
            return responseTime; 
        }
    }

    // CAMPI DEL DTO

    // Campi Generici 
    private final MessageType eventType;
    private final boolean success;
    private final String message; 

    // Campi Autenticazione
    private final String username; 
    private final String password;
    private final boolean isAdmin;

    // Campi Matchmaking (Attesa Partita)
    /**
     * @enum Status
     * @brief Rappresenta gli stati transitori durante la ricerca di un avversario.
     */
    public enum Status { 
        /** @brief Avversario trovato con successo, la partita sta per iniziare. */
        MATCH_FOUND, 
        /** @brief Nessun giocatore disponibile al momento, attesa in coda. */
        WAITING 
    }
    private final Status status; 
    private final Difficulty difficulty; 

    // Campi Dinamica di Partita (GameStart, Submission, Result) 
    private final String cipheredText;
    private final int timer;
    private final int playerIndex; 
    private final String opponentUsername;
    private final String proposedWord;
    private final String correctWord;
    private final Result gameResult;
    private final String winnerUsername; 
    private final int responseTime; 

    // Campi Storico (HistoryResponse) 
    private final List<MatchRecord> matchHistory;
    private final int totalMatchesWon;
    private final int totalMatchesPlayed;
    private final double avgResponseTime;
    private final int totalPlayedTime;

    /**
     * @brief Costruttore privato del DTO, invocato esclusivamente dal metodo build() del Builder.
     * @param builder L'istanza popolata e validata del costruttore interno.
     */
    private CallbackDTO(Builder builder) {
        this.eventType = builder.eventType;
        this.success = builder.success;
        this.message = builder.message;
        
        this.username = builder.username;
        this.password = builder.password;
        this.isAdmin = builder.isAdmin;

        this.status = builder.status;
        this.difficulty = builder.difficulty;
        
        this.cipheredText = builder.cipheredText;
        this.timer = builder.timer;
        this.playerIndex = builder.playerIndex;
        this.opponentUsername = builder.opponentUsername;
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

    // METODI GETTER

    /**
     * @brief Restituisce la tipologia di evento rappresentata da questo DTO.
     * @return Enum MessageType.
     */
    public MessageType getEventType() { 
        return eventType; 
    }

    /**
     * @brief Restituisce lo stato di successo dell'operazione di rete.
     * @return True se l'operazione ha avuto esito positivo.
     */
    public boolean isSuccess() { 
        return success; 
    }

    /**
     * @brief Restituisce il messaggio testuale allegato (notifiche o messaggi di errore).
     * @return Stringa contenente il messaggio.
     */
    public String getMessage() { 
        return message; 
    }

    /**
     * @brief Restituisce lo username elaborato.
     * @return Stringa username.
     */
    public String getUsername() { 
        return username; 
    }

    /**
     * @brief Restituisce la password in chiaro o sottoposta ad hash.
     * @return Stringa password.
     */
    public String getPassword() { 
        return password; 
    }

    /**
     * @brief Verifica se l'utente possiede i privilegi di amministrazione.
     * @return True se l'utente è un amministratore.
     */
    public boolean isAdmin() { 
        return isAdmin; 
    }

    /**
     * @brief Restituisce lo stato attuale della lobby di matchmaking.
     * @return Enum Status (WAITING o MATCH_FOUND).
     */
    public Status getStatus() { 
        return status; 
    }

    /**
     * @brief Restituisce la difficoltà della partita proposta o in corso.
     * @return Enum Difficulty.
     */
    public Difficulty getDifficulty() { 
        return difficulty; 
    }

    /**
     * @brief Restituisce la stringa del testo manipolato/cifrato da sottoporre all'utente.
     * @return Stringa del testo cifrato.
     */
    public String getCipheredText() { 
        return cipheredText; 
    }

    /**
     * @brief Restituisce il tempo massimo concesso per la risoluzione della sfida.
     * @return Intero esprimente i secondi.
     */
    public int getTimer() { 
        return timer; 
    }

    /**
     * @brief Restituisce l'indice identificativo del client nella partita (es. 0 o 1 per UI layout).
     * @return L'indice del giocatore.
     */
    public int getPlayerIndex() { 
        return playerIndex; 
    }

    /**
     * @brief Restituisce l'username dello sfidante abbinato dal server.
     * @return Stringa con l'username dell'avversario.
     */
    public String getOpponentUsername() { 
        return opponentUsername; 
    }

    /**
     * @brief Restituisce la parola digitata e sottomessa dall'utente come tentativo di soluzione.
     * @return La parola proposta.
     */
    public String getProposedWord() { 
        return proposedWord; 
    }

    /**
     * @brief Restituisce la parola segreta in chiaro svelata dal server a fine partita.
     * @return La parola originaria corretta.
     */
    public String getCorrectWord() { 
        return correctWord; 
    }

    /**
     * @brief Restituisce il risultato definitivo della partita appena conclusa.
     * @return Enum Result.
     */
    public Result getGameResult() { 
        return gameResult; 
    }

    /**
     * @brief Restituisce l'username del giocatore che si è aggiudicato la vittoria.
     * @return L'username del vincitore (può essere null in caso di Draw/Pareggio).
     */
    public String getWinnerUsername() { 
        return winnerUsername; 
    }

    /**
     * @brief Restituisce il tempo misurato impiegato per la risposta decisiva.
     * @return Intero esprimente il tempo in ms.
     */
    public int getResponseTime() { 
        return responseTime; 
    }

    /**
     * @brief Restituisce lo storico completo delle partite per l'interfaccia.
     * @return Lista di oggetti MatchRecord.
     */
    public List<MatchRecord> getMatchHistory() { 
        return matchHistory; 
    }

    /**
     * @brief Restituisce il contatore aggregato delle vittorie totali.
     * @return Il totale vittorie dell'utente.
     */
    public int getTotalMatchesWon() { 
        return totalMatchesWon; 
    }

    /**
     * @brief Restituisce il totale complessivo di partite giocate.
     * @return Il conteggio delle partite storiche.
     */
    public int getTotalMatchesPlayed() { 
        return totalMatchesPlayed; 
    }

    /**
     * @brief Restituisce la media calcolata dei tempi di risposta.
     * @return Il valore medio del tempo in ms.
     */
    public double getAvgResponseTime() { 
        return avgResponseTime; 
    }

    /**
     * @brief Restituisce il tempo totale accumulato dall'utente all'interno di partite giocate.
     * @return Il totale in secondi del tempo speso a giocare.
     */
    public int getTotalPlayedTime() { 
        return totalPlayedTime; 
    }

    // INNER CLASS: BUILDER PATTERN

    /**
     * @class Builder
     * @brief Strumento per l'inizializzazione controllata a cascata (chaining) di un CallbackDTO.
     */
    public static class Builder {
        
        // Campo fisso obbligatorio
        private final MessageType eventType;

        // Campi opzionali
        private boolean success = true;
        private String message = null;
        private String username = null;
        private String password = null;
        private boolean isAdmin = false;
        private Status status = null;
        private Difficulty difficulty = null;
        private String cipheredText = null;
        private int timer = 0;
        private int playerIndex = 0;
        private String opponentUsername = null;
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
         * @brief Inizializza un nuovo Builder richiedendo l'unico parametro strettamente necessario.
         * @param eventType La classificazione del messaggio.
         */
        public Builder(MessageType eventType) { 
            this.eventType = eventType; 
        }

        /**
         * @brief Imposta lo stato di successo generale dell'operazione.
         * @param success Valore booleano per l'esito.
         * @return L'istanza corrente del Builder per la concatenazione.
         */
        public Builder success(boolean success) { 
            this.success = success; 
            return this; 
        }

        /**
         * @brief Imposta il messaggio di avviso testuale o d'errore.
         * @param message Il testo da allegare.
         * @return L'istanza corrente del Builder.
         */
        public Builder message(String message) { 
            this.message = message; 
            return this; 
        }

        /**
         * @brief Popola i parametri relativi all'autenticazione.
         * @param username L'username utente.
         * @param password La password inserita.
         * @return L'istanza corrente del Builder.
         */
        public Builder credentials(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        /**
         * @brief Specifica se l'utente possiede i diritti da amministratore.
         * @param isAdmin Valore booleano per i privilegi.
         * @return L'istanza corrente del Builder.
         */
        public Builder isAdmin(boolean isAdmin) { 
            this.isAdmin = isAdmin; 
            return this; 
        }
        
        /**
         * @brief Imposta lo stato di avanzamento nella lobby.
         * @param status Lo stato del matchmaking.
         * @return L'istanza corrente del Builder.
         */
        public Builder playStatus(Status status) { 
            this.status = status; 
            return this; 
        }

        /**
         * @brief Imposta il livello di difficoltà della richiesta o della partita.
         * @param difficulty Il livello desiderato/impostato.
         * @return L'istanza corrente del Builder.
         */
        public Builder difficulty(Difficulty difficulty) { 
            this.difficulty = difficulty; 
            return this; 
        }
        
        /**
         * @brief Compila in blocco i dati essenziali al bootstrap dell'interfaccia di gioco.
         * @param cipheredText     Testo offuscato del testo sorgente.
         * @param timer            Tempo totale del countdown.
         * @param playerIndex      Indice assegnato al client nella sessione.
         * @param opponentUsername Nome dell'avversario.
         * @return L'istanza corrente del Builder.
         */
        public Builder gameStartData(String cipheredText, int timer, int playerIndex, String opponentUsername) {
            this.cipheredText = cipheredText;
            this.timer = timer;
            this.playerIndex = playerIndex;
            this.opponentUsername = opponentUsername;
            return this;
        }

        /**
         * @brief Registra all'interno del DTO i dati inerenti alla risposta formulata dall'utente.
         * @param proposedWord  Stringa della parola ipotizzata dall'utente.
         * @param responseTime  Millisecondi intercorsi dallo Start.
         * @return L'istanza corrente del Builder.
         */
        public Builder answerData(String proposedWord, int responseTime) {
            this.proposedWord = proposedWord;
            this.responseTime = responseTime;
            return this;
        }
        
        /**
         * @brief Compatta le informazioni dell'esito generate dal Server al termine di una sfida.
         * @param gameResult     Risultato netto per il ricevente.
         * @param correctWord    Parola originale in chiaro (in caso di errore serve a mostrarla all'utente).
         * @param winnerUsername L'username che ha completato correttamente e per primo.
         * @param responseTime   Il tempo del vincitore che chiude la partita.
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
         * @brief Archivia l'intero set statistico richiesto dall'interfaccia Storico Utente.
         * @param matchHistory     Lista formattata dei record di ogni singola partita.
         * @param totalWon         Totale cumulativo delle vittorie.
         * @param totalPlayed      Totale partecipazioni ai match.
         * @param avgTime          Mediazione del tempo di reazione dell'utente.
         * @param totalTime        Somma del tempo reale trascorso nei match in secondi.
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
         * @brief Materializza finalmente l'oggetto in sola lettura (Read-Only) da passare ai controller UI.
         * @return Il CallbackDTO generato.
         */
        public CallbackDTO build() {
            return new CallbackDTO(this);
        }
    }
}