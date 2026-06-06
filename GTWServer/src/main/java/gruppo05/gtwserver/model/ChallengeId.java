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
}
