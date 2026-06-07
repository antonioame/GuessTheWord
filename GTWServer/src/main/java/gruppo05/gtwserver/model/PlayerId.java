package gruppo05.gtwserver.model;

import java.util.Objects;

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
    
    // Da commentare
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.username);
        return hash;
    }

    // Da commentare
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlayerId other = (PlayerId) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        return true;
    }
    
    
}
