package gruppo05.gtwserver.model;

import gruppo05.gtwshared.utility.Result;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta una singola partita giocata da un utente in una specifica sfida.
 * @invariant
 * Tutti i campi interni dell'oggetto, inclusa la chiave composta GameId, sono immutabili (final).
 */
public class Game {
    
    /**
     * @brief L'identificativo composto (giocatore, sfida) della partita.
     */
    private final GameId id;
    
    /**
     * @brief Il risultato finale ottenuto nella partita (es. WIN, LOSS).
     */
    private final Result result;
    
    /**
     * @brief Il tempo di risposta impiegato dal giocatore espresso in secondi o millisecondi.
     */
    private final int responseTime;

    /**
     * @brief Costruttore per creare un nuovo oggetto Game.
     * @param[in] player Lo username del giocatore.
     * @param[in] challenge Il codice identificativo della sfida.
     * @param[in] result Il risultato della partita.
     * @param[in] timeToAnswer Il tempo impiegato per rispondere.
     * @pre
     * Il parametro player non deve essere null.
     * @post
     * Viene creata una nuova istanza di GameId memorizzata nel rispettivo campo id.
     */
    public Game(String player, int challenge, Result result, int timeToAnswer) {
        this.id = new GameId(player, challenge);
        this.result = result;
        this.responseTime = timeToAnswer;
    }

    /**
     * @brief Restituisce lo username del giocatore associato alla partita.
     * @return Una stringa contenente lo username del giocatore.
     */
    public String getPlayer() {
        return id.getPlayer();
    }

    /**
     * @brief Restituisce l'identificativo della sfida associata alla partita.
     * @return Un intero che rappresenta il codice della sfida.
     */
    public int getChallenge() {
        return id.getChallenge();
    }

    /**
     * @brief Restituisce il risultato finale della partita.
     * @return Il valore dell'enum Result associato alla partita.
     */
    public Result getResult() {
        return result;
    }

    /**
     * @brief Restituisce il tempo di risposta impiegato per completare la partita.
     * @return Un valore intero che indica il tempo di risposta.
     */
    public int getResponseTime() {
        return responseTime;
    }
}
