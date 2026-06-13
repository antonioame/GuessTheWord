package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Word;
import java.util.List;
import java.util.Optional;

/**
 * @brief Sottointerfaccia DAO per la gestione della persistenza delle parole (Word).
 */
public interface WordDAO extends DAO<Word> {
    
    /**
     * @brief Recupera una parola tramite la sua chiave primaria composta.
     * @param[in] token Un Optional contenente il testo della parola.
     * @param[in] source Un Optional contenente l'identificativo della sorgente.
     * @return Un Optional contenente la parola trovata, oppure un Optional vuoto.
     * @post
     * Il valore restituito non è null.
     */
    Optional<Word> selectById(Optional<String> token, Optional<Integer> source);
    
    /**
     * @brief Filtra e recupera le parole in base ai criteri specificati.
     * @param[in] token Un Optional contenente il testo della parola da filtrare.
     * @param[in] frequenza Un Optional contenente la frequenza di occorrenza da filtrare.
     * @param[in] source Un Optional contenente l'identificativo della sorgente da filtrare.
     * @return Una lista di parole che soddisfano i criteri di ricerca forniti.
     * @post
     * Il valore restituito non è null.
     */
    List<Word> selectAllWhere(Optional<String> token, Optional<Integer> frequenza, Optional<Integer> source);
    
    /**
     * @brief Cancella una parola dal database tramite la sua chiave primaria composta.
     * @param[in] token Un Optional contenente il testo della parola.
     * @param[in] source Un Optional contenente l'identificativo della sorgente.
     * @post
     * Se entrambi gli Optional contengono valori validi ed esistenti, il record della parola viene rimosso dal database.
     */
    void delete(Optional<String> token, Optional<Integer> source);
}
