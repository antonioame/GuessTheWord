package gruppo05.gtwserver.model;

import java.util.Objects;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta un amministratore del sistema, identificato in modo univoco da un AdminId.
 * @invariant
 * L'identificativo id e la password dell'amministratore sono immutabili (final).
 */
public class Admin {
    
    // Da commentare
    private final String username;
    
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
        this.username = username;
        this.password = password;
    }

    // Da commentare
    public String getUsername() {
        return username;
    }

    /**
     * @brief Restituisce la password dell'amministratore.
     * @return Una stringa contenente la password dell'amministratore.
     */
    public String getPassword() {
        return password;
    }

    // Da commentare
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.username);
        return hash;
    }

    // Da commentare
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
        final Admin other = (Admin) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        return true;
    }

    
}
