package gruppo05.gtwserver.model;

import java.util.Objects;

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

    // Da commentare
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.player);
        hash = 41 * hash + this.challenge;
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
        final GameId other = (GameId) obj;
        if (this.challenge != other.challenge) {
            return false;
        }
        if (!Objects.equals(this.player, other.player)) {
            return false;
        }
        return true;
    }
    
    
}
