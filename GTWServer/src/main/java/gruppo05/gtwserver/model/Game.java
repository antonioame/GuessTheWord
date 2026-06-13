package gruppo05.gtwserver.model;

import gruppo05.gtwshared.utility.Result;
import java.util.Objects;

/**
 * @brief Rappresenta una singola partita giocata da un utente in una specifica sfida.
 * @invariant
 * I campi interni dell'oggetto, che costituiscono la chiave primaria composta
 * (player e challenge) e i dati di gioco, sono immutabili (final).
 */
public class Game {
    
    /**
     * @brief Lo username del giocatore, parte della chiave primaria composta.
     */
    private final String player;
    
    /**
     * @brief Il codice identificativo della sfida, parte della chiave primaria composta.
     */
    private final int challenge;
    
    /**
     * @brief Il risultato finale ottenuto nella partita (es. WIN, LOSS).
     */
    private final Result result;
    
    /**
     * @brief Il tempo di risposta impiegato dal giocatore espresso in secondi o millisecondi.
     */
    private final int responseTime;

    /**
     * @brief Costruttore per creare un nuovo oggetto Game.
     * @param[in] player Lo username del giocatore.
     * @param[in] challenge Il codice identificativo della sfida.
     * @param[in] result Il risultato della partita.
     * @param[in] timeToAnswer Il tempo impiegato per rispondere.
     * @pre
     * Il parametro player non deve essere null.
     * @post
     * Viene creata una nuova istanza di GameId memorizzata nel rispettivo campo id.
     */
    public Game(String player, int challenge, Result result, int timeToAnswer) {
        this.player = player;
        this.challenge = challenge;
        this.result = result;
        this.responseTime = timeToAnswer;
    }

    /**
     * @brief Restituisce lo username del giocatore.
     * @return Lo username del giocatore.
     */
    public String getPlayer() {
        return player;
    }
    
    /**
     * @brief Restituisce il codice identificativo della sfida.
     * @return Il codice numerico della sfida.
     */
    public int getChallenge() {
        return challenge;
    }

    /**
     * @brief Restituisce il risultato finale della partita.
     * @return Il valore dell'enum Result associato alla partita.
     */
    public Result getResult() {
        return result;
    }

    /**
     * @brief Restituisce il tempo di risposta impiegato per completare la partita.
     * @return Un valore intero che indica il tempo di risposta.
     */
    public int getResponseTime() {
        return responseTime;
    }

    /**
     * @brief Calcola l'hash code dell'oggetto Game basandosi sulla chiave primaria composta.
     * @return Il valore dell'hash code calcolato.
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.player);
        hash = 47 * hash + this.challenge;
        return hash;
    }

    /**
     * @brief Confronta questo oggetto Game con l'oggetto specificato per verificarne l'uguaglianza.
     * @param[in] obj L'oggetto da confrontare con la partita corrente.
     * @return true se l'oggetto specificato è uguale a questa partita, false altrimenti.
     * @post
     * Il risultato è true se e solo se l'oggetto passato non è null,
     * è un'istanza di Game e presenta gli stessi valori per player e challenge.
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
        final Game other = (Game) obj;
        if (this.challenge != other.challenge) {
            return false;
        }
        if (!Objects.equals(this.player, other.player)) {
            return false;
        }
        return true;
    }
    
    
}
