package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Admin;
import java.util.Optional;

/**
 *
 * @author francesco-vecchione
 */
public interface AdminDAO extends DAO<Admin> {
    Optional<Admin> selectById(Optional<String> username);
    void delete(Optional<String> username);
}
