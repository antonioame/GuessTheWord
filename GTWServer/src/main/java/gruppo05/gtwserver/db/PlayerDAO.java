package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Player;
import java.util.Optional;

/**
 * @author francesco-vecchione
 * @brief Sottointerfaccia DAO per la gestione della persistenza dei giocatori (Player).
 */
public interface PlayerDAO extends DAO<Player> {
    
    /**
     * @brief Recupera un giocatore tramite il suo username.
     * @param[in] username Un Optional contenente lo username del giocatore da cercare.
     * @return Un Optional contenente il giocatore trovato, oppure un Optional vuoto.
     * @post
     * Il valore restituito non è null.
     */
    Optional<Player> selectById(Optional<String> username);
    
    /**
     * @brief Cancella un giocatore dal database tramite il suo username.
     * @param[in] username Un Optional contenente lo username del giocatore da rimuovere.
     * @post
     * Se l'Optional contiene uno username esistente, il record del giocatore viene rimosso dal database.
     */
    void delete(Optional<String> username);
}
