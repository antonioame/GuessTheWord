package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Player;
import java.util.Optional;

/**
 *
 * @author francesco-vecchione
 */
public interface PlayerDAO extends DAO<Player> {
    Optional<Player> selectById(Optional<String> username);
    void delete(Optional<String> username);
}
