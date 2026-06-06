package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta un amministratore del sistema, identificato in modo univoco da un AdminId.
 * @invariant
 * L'identificativo id e la password dell'amministratore sono immutabili (final).
 */
public class Admin {
    
    /**
     * @brief L'identificativo univoco dell'amministratore contenente lo username.
     */
    private final AdminId id;
    
    /**
     * @brief La password associata all'account dell'amministratore.
     */
    private final String password;

    /**
     * @brief Costruttore per creare un nuovo oggetto Admin.
     * @param[in] username Lo username da assegnare all'amministratore.
     * @param[in] password La password da associare all'account.
     * @pre
     * Il parametro username non deve essere null.
     * @post
     * Viene creata una nuova istanza di AdminId memorizzata nel rispettivo campo id.
     */
    public Admin(String username, String password) {
        this.id = new AdminId(username);
        this.password = password;
    }

    /**
     * @brief Restituisce lo username dell'amministratore.
     * @return Una stringa contenente lo username dell'amministratore.
     * @post
     * Il valore restituito non è null ed equivale al valore memorizzato in id.
     */
    public String getUsername() {
        return id.getUsername();
    }

    /**
     * @brief Restituisce la password dell'amministratore.
     * @return Una stringa contenente la password dell'amministratore.
     */
    public String getPassword() {
        return password;
    }
}
