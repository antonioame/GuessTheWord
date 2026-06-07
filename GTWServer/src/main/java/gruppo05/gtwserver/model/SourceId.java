package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta l'identificativo univoco numerico di una sorgente di dati (Source).
 * @invariant
 * Il codice numerico identificativo della sorgente è immutabile (final).
 */
public class SourceId {
    
    /**
     * @brief Il codice numerico univoco associato alla sorgente.
     */
    private final int id;

    /**
     * @brief Costruttore per creare un nuovo identificativo SourceId.
     * @param[in] id Il codice numerico della sorgente.
     */
    public SourceId(int id) {
        this.id = id;
    }

    /**
     * @brief Restituisce il codice numerico della sorgente.
     * @return Un intero che rappresenta l'identificativo.
     */
    public int getId() {
        return id;
    }

    // Da commentare
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.id;
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
        final SourceId other = (SourceId) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
}
