package gruppo05.gtwserver.db;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Interfaccia generica Data Access Object (DAO) per la gestione delle operazioni CRUD sul database.
 * @invariant
 * Il tipo generico T rappresenta il tipo dell'entità o del modello gestito.
 * @invariant
 * Il tipo generico K rappresenta il tipo della chiave primaria o identificativo dell'entità.
 */
public interface DAO<T, K> {
    /**
     * @brief Seleziona un elemento specifico tramite il suo identificativo.
     * @param[in] modelId L'identificativo univoco dell'elemento da cercare.
     * @return Un Optional contenente l'elemento trovato, oppure un Optional vuoto
     * se non è presente alcun elemento con l'id fornito.
     * @pre
     * Il parametro modelId non deve essere null.
     */
    Optional<T> selectById(K modelId);
    
    /**
     * @brief Recupera tutti gli elementi presenti nel DB.
     * @return Una lista contenente tutti gli elementi trovati, oppure una lista
     * vuota se non è presente alcun elemento.
     * @post
     * La lista restituita non è null.
     */
    List<T> selectAll();
    
    /**
     * @brief Inserisce un nuovo elemento nel DB.
     * @param[in] model L'oggetto da inserire.
     * @pre
     * Il parametro model non deve essere null.
     */
    void insert(T model);
    
    /**
     * @brief Inserisce una lista di elementi nel DB.
     * @param[in] modelList La lista degli oggetti da inserire.
     * @pre
     * Il parametro modelList non deve essere null e non deve contenere elementi null.
     */
    void insertAll(List<T> modelList);
    
    /**
     * @brief Aggiorna lo stato di un elemento esistente nel DB.
     * @param[in] model L'oggetto aggiornato da persistere.
     * @pre
     * Il parametro model non deve essere null e deve corrispondere a un elemento già esistente.
     */
    void update(T model);
    
    /**
     * @brief Rimuove un elemento dal DB tramite il suo identificativo.
     * @param[in] modelId L'identificativo univoco dell'elemento da eliminare.
     * @pre
     * Il parametro modelId non deve essere null.
     */
    void delete(K modelId);
    
    /**
     * @brief Recupera tutti gli elementi dal db e li filtra in base ad una condizione.
     * @param[in] condition Il predicato che definisce i criteri di filtraggio.
     * @return Una lista contenente solo gli elementi che soddisfano la condizione,
     * oppure una lista vuota se nessun elemento corrisponde.
     * @pre
     * Il parametro condition non deve essere null.
     * @post
     * La lista restituita non è null ed ha una dimensione minore o uguale alla lista completa.
     */
    default List<T> selectAllWhere(Predicate<T> condition) {
        if(condition == null) return selectAll();
        return selectAll().stream().filter(condition).collect(Collectors.toList());
    }
}
