package gruppo05.gtwserver.model;

import java.util.Objects;

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

    // Da commentare    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.username);
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
        final AdminId other = (AdminId) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        return true;
    }
    
    
}
