package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Admin;
import java.util.Optional;

/**
 * @brief Sottointerfaccia DAO per la gestione della persistenza degli amministratori.
 */
public interface AdminDAO extends DAO<Admin> {
    
    /**
     * @brief Recupera un amministratore tramite il suo username.
     * @param[in] username Un Optional contenente lo username dell'amministratore da cercare.
     * @return Un Optional contenente l'oggetto Admin trovato, oppure un Optional vuoto 
     * se l'amministratore non esiste o se il parametro di input è vuoto.
     * @post
     * Il valore restituito non è null.
     */
    Optional<Admin> selectById(Optional<String> username);
    
    /**
     * @brief Cancella un amministratore dal database tramite il suo username.
     * @param[in] username Un Optional contenente lo username dell'amministratore da rimuovere.
     * @post
     * Se l'Optional in input contiene uno username valido e presente nel database,
     * il record associato viene rimosso definitivamente.
     */
    void delete(Optional<String> username);
}
