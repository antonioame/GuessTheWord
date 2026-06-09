package gruppo05.gtwserver.model;

import gruppo05.gtwshared.utility.Difficulty;
import java.sql.Date;
import java.util.Objects;
import java.util.Random;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta una sfida (Challenge) all'interno del sistema, definita da una parola segreta, una difficoltà e una sorgente.
 * @invariant
 * Tutti i campi interni dell'oggetto sono immutabili (final).
 */
public class Challenge {
    
    // Da commentare
    private final int code;
    
    /**
     * @brief La data in cui viene o è stata proposta la sfida.
     */
    private final Date date;
    
    /**
     * @brief Il livello di difficoltà associato alla sfida.
     */
    private final Difficulty difficulty;
    
    /**
     * @brief La parola segreta oggetto della sfida.
     */
    private final String word;
    
    /**
     * @brief L'identificativo della sorgente da cui è stata estratta la parola.
     */
    private final int source;

    /**
     * @brief Costruttore completo per creare un oggetto Challenge con un codice identificativo esplicito.
     * @param[in] code Il codice numerico univoco da assegnare alla sfida.
     * @param[in] date La data della sfida.
     * @param[in] difficulty Il livello di difficoltà.
     * @param[in] word La parola segreta.
     * @param[in] source L'identificativo della sorgente.
     * @post
     * Viene creata una nuova istanza di ChallengeId memorizzata nel campo id.
     */
    public Challenge(int code, Date date, Difficulty difficulty, String word, int source) {
        this.code = code;
        this.date = date;
        this.difficulty = difficulty;
        this.word = word;
        this.source = source;
    }
    
    /**
     * @brief Costruttore secondario che genera automaticamente un codice identificativo casuale per la sfida.
     * @param[in] date La data della sfida.
     * @param[in] difficulty Il livello di difficoltà.
     * @param[in] word La parola segreta.
     * @param[in] source L'identificativo della sorgente.
     * @post
     * La sfida viene istanziata richiamando il costruttore principale con un intero casuale compreso tra 1 e Integer.MAX_VALUE.
     */
    public Challenge(Date date, Difficulty difficulty, String word, int source) {
        this(   new Random().nextInt(Integer.MAX_VALUE) + 1,
                date,
                difficulty,
                word,
                source);
    }

    // Da commentare
    public int getCode() {
        return code;
    }

    /**
     * @brief Restituisce la data della sfida.
     * @return Un oggetto Date corrispondente alla data memorizzata.
     */
    public Date getDate() {
        return date;
    }

    /**
     * @brief Restituisce la difficoltà della sfida.
     * @return Il valore dell'enum Difficulty della sfida.
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * @brief Restituisce la parola segreta della sfida.
     * @return Una stringa contenente la parola.
     */
    public String getWord() {
        return word;
    }

    /**
     * @brief Restituisce l'identificativo della sorgente della parola.
     * @return Un intero che rappresenta l'id della sorgente.
     */
    public int getSource() {
        return source;
    }

    // Da commentare
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + this.code;
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
        final Challenge other = (Challenge) obj;
        if (this.code != other.code) {
            return false;
        }
        return true;
    }

    
}
