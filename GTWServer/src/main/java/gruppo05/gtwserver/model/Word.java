package gruppo05.gtwserver.model;

import java.util.Objects;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta una parola (Word) registrata nel sistema con la relativa frequenza di comparsa in una specifica sorgente.
 * @invariant
 * Tutti i campi interni dell'oggetto, inclusa la chiave composta WordId, sono immutabili (final).
 */
public class Word {
    
    // Da commentare
    private final String token;
    
    /**
     * @brief La frequenza di occorrenza della parola all'interno della propria sorgente.
     */
    private final int frequency;

    // Da commentare
    private final int source;
    
    /**
     * @brief Costruttore per creare un nuovo oggetto Word.
     * @param[in] token La stringa testuale della parola.
     * @param[in] frequency La frequenza associata alla parola.
     * @param[in] source L'identificativo numerico della sorgente di provenienza.
     * @pre
     * Il parametro token non deve essere null.
     * @pre
     * Il parametro frequency deve essere maggiore o uguale a 0.
     * @post
     * Viene creata una nuova istanza di WordId memorizzata nel rispettivo campo id.
     */
    public Word(String token, int frequency, int source) {
        this.token = token;
        this.frequency = frequency;
        this.source = source;
    }

    // Da commentare
    public String getToken() {
        return token;
    }

    /**
     * @brief Restituisce la frequenza di occorrenza della parola.
     * @return Un intero che rappresenta la frequenza memorizzata.
     */
    public int getFrequency() {
        return frequency;
    }
    
    // Da commentare
    public int getSource() {
        return source;
    }

    // Da commentare
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.token);
        hash = 43 * hash + this.source;
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
        final Word other = (Word) obj;
        if (this.source != other.source) {
            return false;
        }
        if (!Objects.equals(this.token, other.token)) {
            return false;
        }
        return true;
    }
    
    
}
