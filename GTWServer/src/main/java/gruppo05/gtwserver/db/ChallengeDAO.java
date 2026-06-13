package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Challenge;
import java.util.Optional;

/**
 * @brief Sottointerfaccia DAO per la gestione della persistenza delle sfide (Challenge).
 */
public interface ChallengeDAO extends DAO<Challenge> {
    
    /**
     * @brief Recupera una sfida tramite il suo codice identificativo univoco.
     * @param[in] code Un Optional contenente il codice numerico della sfida da cercare.
     * @return Un Optional contenente la sfida trovata, oppure un Optional vuoto.
     * @post
     * Il valore restituito non è null.
     */
    Optional<Challenge> selectById(Optional<Integer> code);
    
    /**
     * @brief Cancella una sfida dal database tramite il suo codice identificativo univoco.
     * @param[in] code Un Optional contenente il codice numerico della sfida da rimuovere.
     * @post
     * Se l'Optional contiene un codice esistente, il record della sfida viene rimosso dal database.
     */
    void delete(Optional<Integer> code);    
}
