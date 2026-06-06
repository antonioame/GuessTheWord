package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta una parola (Word) registrata nel sistema con la relativa frequenza di comparsa in una specifica sorgente.
 * @invariant
 * Tutti i campi interni dell'oggetto, inclusa la chiave composta WordId, sono immutabili (final).
 */
public class Word {
    
    /**
     * @brief L'identificativo composto (stringa testuale, codice sorgente) della parola.
     */
    private final WordId id;
    
    /**
     * @brief La frequenza di occorrenza della parola all'interno della propria sorgente.
     */
    private final int frequency;

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
        id = new WordId(token, source);
        this.frequency = frequency;
    }

    /**
     * @brief Restituisce il valore testuale della parola.
     * @return Una stringa contenente il token della parola.
     */
    public String getToken() {
        return id.getToken();
    }

    /**
     * @brief Restituisce la frequenza di occorrenza della parola.
     * @return Un intero che rappresenta la frequenza memorizzata.
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * @brief Restituisce l'identificativo della sorgente associata alla parola.
     * @return Un intero che rappresenta il codice della sorgente.
     */
    public int getSource() {
        return id.getSource();
    }
    
    
}
