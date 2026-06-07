package gruppo05.gtwserver.model;

import java.util.Objects;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta l'identificativo composto univoco di una parola (Word), costituito dalla stringa testuale e dal codice della sorgente.
 * @invariant
 * Entrambi i campi della chiave composta (token e source) sono immutabili (final).
 */
public class WordId {
    
    /**
     * @brief La stringa testuale della parola (token).
     */
    private final String token;
    
    /**
     * @brief L'identificativo numerico della sorgente a cui la parola appartiene.
     */
    private final int source;    

    /**
     * @brief Costruttore per creare una nuova chiave composta WordId.
     * @param[in] token La stringa testuale della parola.
     * @param[in] source L'identificativo numerico della sorgente.
     * @pre
     * Il parametro token non deve essere null.
     */
    public WordId(String token, int source) {
        this.token = token;
        this.source = source;
    }

    /**
     * @brief Restituisce la stringa testuale della parola.
     * @return Una stringa contenente il token.
     */
    public String getToken() {
        return token;
    }
    
    /**
     * @brief Restituisce l'identificativo della sorgente.
     * @return Un intero che rappresenta il codice della sorgente.
     */
    public int getSource() {
        return source;
    }

    // Da commentare
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.token);
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
        final WordId other = (WordId) obj;
        if (!Objects.equals(this.token, other.token)) {
            return false;
        }
        return true;
    }
    
    
}
