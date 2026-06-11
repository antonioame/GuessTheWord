package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Game;
import java.util.Optional;

/**
 * @author francesco-vecchione
 * @brief Sottointerfaccia DAO per la gestione della persistenza delle partite (Game).
 */
public interface GameDAO extends DAO<Game> {
    
    /**
     * @brief Recupera una partita tramite la sua chiave primaria composta.
     * @param[in] player Un Optional contenente lo username del giocatore.
     * @param[in] challenge Un Optional contenente il codice numerico della sfida.
     * @return Un Optional contenente la partita trovata, oppure un Optional vuoto.
     * @post
     * Il valore restituito non è null.
     */
    Optional<Game> selectById(Optional<String> player, Optional<Integer> challenge);
    
    /**
     * @brief Cancella una partita dal database tramite la sua chiave primaria composta.
     * @param[in] player Un Optional contenente lo username del giocatore.
     * @param[in] challenges Un Optional contenente il codice numerico della sfida.
     * @post
     * Se entrambi gli Optional contengono valori validi ed esistenti, il record della partita viene rimosso dal database.
     */
    void delete(Optional<String> player, Optional<Integer> challenges);
}
