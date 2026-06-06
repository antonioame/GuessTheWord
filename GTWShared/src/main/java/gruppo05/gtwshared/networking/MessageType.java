package gruppo05.gtwshared.networking;

/**
 * Enumerazione di tutti i tipi di messaggio del protocollo GuessTheWord.
 *
 * <p>Ogni {@link NetworkMessage} trasporta un {@code MessageType} che consente
 * al ricevente di capire subito come interpretare il payload senza l'uso di
 * nessuna catena di instanceof.</p>
 *
 * <pre>
 * FLUSSO DI COMUNICAZIONE CLIENT-SERVER
 * 1. Il client invia una richiesta di login al server.
 * 2. Il server controlla i dati e risponde con l'esito del login.
 * 3. Se l'utente non è registrato, il client può inviare una richiesta di registrazione.
 * 4. Il server registra il nuovo utente e invia una risposta di conferma.
 * 5. Dopo un login effettuato con successo, il giocatore rimane in attesa di un avversario.
 * 6. Quando due giocatori sono disponibili, il server avvia la partita e invia il messaggio di inizio gioco.
 * 7. Il server invia ai due client il testo cifrato e il timer.
 * 8. Durante la partita, il client invia la propria risposta al server.
 * 9. Al termine della partita, il server comunica il risultato finale ai giocatori.
 * 10. Se uno dei due giocatori si disconnette prima della fine della partita, il server avvisa l'altro giocatore.
 * 11. Quando il giocatore decide di uscire, il client invia un messaggio di disconnessione al server.
 * </pre>
 * 
 * @author chiara
 * @version 2.0
 */
public enum MessageType {

    // 1. AUTENTICAZIONE
    /** Client -> Server: richiesta di login (username + password). */
    LOGIN_REQUEST,
    /** Server -> Client: esito del login (successo/fallimento + eventuale errore). */
    LOGIN_RESPONSE,
    /** Client -> Server: richiesta di registrazione (username + password). */
    REGISTER_REQUEST,
    /** Server -> Client: esito della registrazione (successo/fallimento + eventuale errore). */
    REGISTER_RESPONSE,

    // 2. ATTESA
    /** Server -> Client: login OK ma l'avversario non è ancora connesso. */
    WAITING_FOR_OPPONENT,

    // 3. PARTITA
    /** Server -> Client: la sfida inizia (testo cifrato, durata timer, ecc.). */
    GAME_START,
    /** Client -> Server: risposta dell'utente. */
    ANSWER_SUBMISSION,
    /** Server -> Client: esito della partita (vincitore, parola corretta, statistiche). */
    GAME_RESULT,
    /** Server -> Client: l'avversario ha inviato una risposta (aggiornamento in tempo reale). */
    OPPONENT_ANSWERED,

    // 4. GESTIONE CONNESSIONE
    /** Server -> Client: l'avversario si è disconnesso. */
    OPPONENT_DISCONNECTED,
    /** Client -> Server: il client si disconnette volontariamente. */
    CLIENT_DISCONNECT,

    // 5. STORICO
    /** Client -> Server: richiesta dello storico partite del proprio utente. */
    HISTORIAN_REQUEST,
    /** Server -> Client: risposta con dati dello storico. */
    HISTORIAN_RESPONSE,
    
    // 6. UTILITY
    /** Messaggio di testo generico (utile per debug o notifiche). */
    TEXT_MESSAGE
}
