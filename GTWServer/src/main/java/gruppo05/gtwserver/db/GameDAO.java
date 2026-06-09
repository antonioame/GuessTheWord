package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Game;
import java.util.Optional;

/**
 *
 * @author francesco-vecchione
 */
public interface GameDAO extends DAO<Game> {
    Optional<Game> selectById(Optional<String> player, Optional<Integer> challenge);
    void delete(Optional<String> player, Optional<Integer> challenges);
}
