package gruppo05.gtwshared.networking;

/**
 * @enum MessageType
 * @brief Enumerazione di tutti i tipi di messaggio del protocollo GuessTheWord.
 * 
 * @author chiara
 * @version 1.0
 */
public enum MessageType {

    // 1. AUTENTICAZIONE
    /**
     * @brief Client -> Server: richiesta di login (username + password).
     */
    LOGIN_REQUEST,
    /**
     * @brief Server -> Client: esito del login (successo/fallimento + errore).
     */
    LOGIN_RESPONSE,
    /**
     * @brief Client -> Server: richiesta di registrazione (username + password).
     */
    REGISTER_REQUEST,
    /**
     * @brief Server -> Client: esito della registrazione (successo/fallimento + errore).
     */
    REGISTER_RESPONSE,

    // 2. RICERCA PARTITA E ATTESA
    /**
     * @brief Client -> Server: l'utente loggato richiede di avviare una partita.
     */
    PLAY_REQUEST,
    /**
     * @brief Server -> Client: esito della richiesta di gioco (avversario trovato o in attesa).
     */
    PLAY_RESPONSE,

    // 3. PARTITA
    /**
     * @brief Server -> Client: la sfida inizia (testo cifrato, timer, ecc.).
     */
    GAME_START,
    /**
     * @brief Client -> Server: risposta dell'utente.
     */
    ANSWER_SUBMISSION,
    /**
     * @brief Server -> Client: esito della partita (vincitore, parola corretta, ecc.).
     */
    GAME_RESULT,
    /**
     * @brief Server -> Client: l'avversario ha inviato una risposta.
     */
    OPPONENT_ANSWERED,

    // 4. GESTIONE CONNESSIONE
    /**
     * @brief Server -> Client: l'avversario si è disconnesso.
     */
    OPPONENT_DISCONNECTED,
    /**
     * @brief Client -> Server: il client si disconnette volontariamente.
     */
    CLIENT_DISCONNECT,

    // 5. STORICO
    /**
     * @brief Client -> Server: richiesta dello storico partite del proprio utente.
     */
    HISTORY_REQUEST,
    /**
     * @brief Server -> Client: risposta con dati dello storico.
     */
    HISTORY_RESPONSE,
    
    // 6. UTILITY
    /**
     * @brief Messaggio di testo generico per debug o notifiche.
     */
    TEXT_MESSAGE
}