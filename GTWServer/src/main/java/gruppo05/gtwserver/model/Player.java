package gruppo05.gtwserver.model;

import java.util.Objects;

/**
 * @brief Rappresenta un giocatore (Player) registrato nel sistema con le relative statistiche di gioco aggregate.
 * @invariant
 * Tutti i campi dell'oggetto sono immutabili (final).
 * @invariant
 * Il numero di partite vinte (totalGamesWon) deve essere minore o uguale al
 * numero di partite totali giocate (totalGamesPlayed).
 */
public class Player {
    
    /**
     * @brief Lo username del giocatore, utilizzato come chiave primaria.
     */
    private final String username;
    
    /**
     * @brief La password associata all'account del giocatore.
     */
    private final String password;
    
    /**
     * @brief Il tempo totale cumulativo speso in partita dal giocatore.
     */
    private final int totalPlayedTime;
    
    /**
     * @brief Il numero totale di partite vinte dal giocatore.
     */
    private final int totalGamesWon;
    
    /**
     * @brief Il numero totale di partite giocate dal giocatore.
     */
    private final int totalGamesPlayed;

    /**
     * @brief Costruttore completo per istanziare un oggetto Player con le sue credenziali e statistiche storiche.
     * @param[in] username Lo username univoco del giocatore.
     * @param[in] password La password dell'account.
     * @param[in] totalPlayedTime Il tempo totale di gioco accumulato.
     * @param[in] totalGamesWon Il totale delle partite vinte.
     * @param[in] totalGamesPlayed Il totale delle partite disputate.
     * @pre
     * Il parametro username non deve essere null.
     * @pre
     * I parametri totalPlayedTime, totalGamesWon e totalGamesPlayed devono essere maggiori o uguali a 0.
     * @post
     * Viene creata una nuova istanza di PlayerId memorizzata nel rispettivo campo id.
     */
    public Player(String username, String password, int totalPlayedTime, int totalGamesWon, int totalGamesPlayed) {
        this.username = username;
        this.password = password;
        this.totalPlayedTime = totalPlayedTime;
        this.totalGamesWon = totalGamesWon;
        this.totalGamesPlayed = totalGamesPlayed;
    }

    /**
     * @brief Lo username del giocatore, utilizzato come chiave primaria.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @brief Restituisce la password del giocatore.
     * @return Una stringa contenente la password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @brief Restituisce il tempo totale di gioco accumulato dal giocatore.
     * @return Un intero che rappresenta il tempo totale giocato.
     */
    public int getTotalPlayedTime() {
        return totalPlayedTime;
    }

    /**
     * @brief Restituisce il numero totale di partite vinte dal giocatore.
     * @return Un intero che rappresenta le partite vinte.
     */
    public int getTotalGamesWon() {
        return totalGamesWon;
    }

    /**
     * @brief Restituisce il numero totale di partite disputate dal giocatore.
     * @return Un intero che rappresenta le partite giocate.
     */
    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }

    /**
     * @brief Calcola l'hash code del giocatore basandosi sullo username.
     * @return Il valore dell'hash code calcolato.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.username);
        return hash;
    }

    /**
     * @brief Confronta questo giocatore con l'oggetto specificato per verificarne l'uguaglianza.
     * @param[in] obj L'oggetto da confrontare con il giocatore corrente.
     * @return true se l'oggetto specificato è uguale a questo giocatore, false altrimenti.
     * @post
     * Il risultato è true se e solo se l'oggetto passato non è null,
     * è un'istanza di Player e ha lo stesso username di questo oggetto.
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
        final Player other = (Player) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        return true;
    }
 
    
}
