package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Challenge;
import java.util.Optional;

/**
 *
 * @author francesco-vecchione
 */
public interface ChallengeDAO extends DAO<Challenge> {
    Optional<Challenge> selectById(Optional<Integer> code);
    void delete(Optional<Integer> code);    
}
