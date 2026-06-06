package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta l'identificativo univoco di un amministratore del sistema.
 * @invariant
 * Lo username dell'amministratore è immutabile (final).
 */
public class AdminId {
    /**
     * @brief Lo username che identifica l'amministratore.
     */
    private final String username;

    /**
     * @brief Costruttore per creare un nuovo identificativo AdminId.
     * @param[in] username Lo username dell'amministratore.
     */
    public AdminId(String username) {
        this.username = username;
    }

    /**
     * @brief Restituisce lo username dell'amministratore.
     * @return Una stringa contenente lo username.
     */
    public String getUsername() {
        return username;
    }
}
