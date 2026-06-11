package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Source;
import java.util.Optional;

/**
 * @author francesco-vecchione
 * @brief Sottointerfaccia DAO per la gestione della persistenza delle sorgenti dati (Source).
 */
public interface SourceDAO extends DAO<Source> {
    
    /**
     * @brief Recupera una sorgente tramite il suo identificativo numerico univoco.
     * @param[in] id Un Optional contenente l'id numerico della sorgente da cercare.
     * @return Un Optional contenente la sorgente trovata, oppure un Optional vuoto.
     * @post
     * Il valore restituito non è null.
     */
    Optional<Source> selectById(Optional<Integer> id);
    
    /**
     * @brief Cancella una sorgente dal database tramite il suo identificativo numerico univoco.
     * @param[in] id Un Optional contenente l'id numerico della sorgente da rimuovere.
     * @post
     * Se l'Optional contiene un id esistente, il record della sorgente viene rimosso dal database.
     */
    void delete(Optional<Integer> id);
}
