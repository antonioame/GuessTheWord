package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta l'identificativo univoco di un giocatore (Player).
 * @invariant
 * Lo username del giocatore è immutabile (final).
 */
public class PlayerId {
    
    /**
     * @brief Lo username che identifica il giocatore.
     */
    private final String username;

    /**
     * @brief Costruttore per creare un nuovo identificativo PlayerId.
     * @param[in] username Lo username del giocatore.
     */
    public PlayerId(String username) {
        this.username = username;
    }

    /**
     * @brief Restituisce lo username del giocatore.
     * @return Una stringa contenente lo username.
     */
    public String getUsername() {
        return username;
    }
}
