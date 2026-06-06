package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta l'identificativo composto univoco di una partita (Game), costituito dallo username del giocatore e dal codice della sfida.
 * @invariant
 * Entrambi i campi della chiave composta (player e challenge) sono immutabili (final).
 */
public class GameId {
    
    /**
     * @brief Lo username del giocatore associato alla partita.
     */
    private final String player;
    
    /**
     * @brief Il codice identificativo numerico della sfida associata alla partita.
     */
    private final int challenge;

    /**
     * @brief Costruttore per creare una nuova chiave composta GameId.
     * @param[in] player Lo username del giocatore.
     * @param[in] challenge Il codice numerico della sfida.
     */
    public GameId(String player, int challenge) {
        this.player = player;
        this.challenge = challenge;
    }

    /**
     * @brief Restituisce lo username del giocatore.
     * @return Una stringa contenente lo username del giocatore.
     */
    public String getPlayer() {
        return player;
    }

    /**
     * @brief Restituisce il codice identificativo della sfida.
     * @return Un intero che rappresenta il codice della sfida.
     */
    public int getChallenge() {
        return challenge;
    }
}
