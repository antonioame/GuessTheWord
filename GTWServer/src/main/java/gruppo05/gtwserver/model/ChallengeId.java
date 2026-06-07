package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta l'identificativo univoco numerico di una sfida (Challenge).
 * @invariant
 * Il codice numerico identificativo della sfida è immutabile (final).
 */
public class ChallengeId {
    
    /**
     * @brief Il codice numerico univoco associato alla sfida.
     */
    private final int code;

    /**
     * @brief Costruttore per creare un nuovo identificativo ChallengeId.
     * @param[in] code Il codice numerico della sfida.
     */
    public ChallengeId(int code) {
        this.code = code;
    }

    /**
     * @brief Restituisce il codice numerico della sfida.
     * @return Un intero che rappresenta il codice identificativo.
     */
    public int getCode() {
        return code;
    }

    // Da commentare
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + this.code;
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
        final ChallengeId other = (ChallengeId) obj;
        if (this.code != other.code) {
            return false;
        }
        return true;
    }
    
    
}
