package gruppo05.gtwserver.model;

import java.util.Objects;

/**
 * @author francesco-vecchione
 * @brief Rappresenta una parola (Word) registrata nel sistema con la relativa frequenza di comparsa in una specifica sorgente.
 * @invariant
 * Tutti i campi interni dell'oggetto, che costituiscono la chiave primaria
 * composta (token e source) e la frequenza, sono immutabili (final).
 */
public class Word {
    
    /**
     * @brief La stringa testuale della parola, parte della chiave primaria composta.
     */
    private final String token;
    
    /**
     * @brief La frequenza di occorrenza della parola all'interno della propria sorgente.
     */
    private final int frequency;

    /**
     * @brief L'identificativo numerico della sorgente di provenienza, parte della chiave primaria composta.
     */
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

    /**
     * @brief Restituisce la stringa testuale della parola.
     * @return La stringa che rappresenta la parola.
     */
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
    
    /**
     * @brief Restituisce l'identificativo numerico della sorgente di provenienza.
     * @return L'identificativo della sorgente.
     */
    public int getSource() {
        return source;
    }

    /**
     * @brief Calcola l'hash code dell'oggetto Word basandosi sulla chiave primaria composta.
     * @return Il valore dell'hash code calcolato.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.token);
        hash = 43 * hash + this.source;
        return hash;
    }

    /**
     * @brief Confronta questo oggetto Word con l'oggetto specificato per verificarne l'uguaglianza.
     * @param[in] obj L'oggetto da confrontare con la parola corrente.
     * @return true se l'oggetto specificato è uguale a questa parola, false altrimenti.
     * @post
     * Il risultato è true se e solo se l'oggetto passato non è null,
     * è un'istanza di Word e presenta gli stessi valori per token e source.
     */
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
