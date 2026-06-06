package gruppo05.gtwserver.db;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author francesco-vecchione
 */
public interface DAO<T> {
    public static final String URL = "jdbc:sqlite:ServerDB";
    Optional<T> selectById(T modelWithId);
    List<T> selectAll();
    void insert(T model);
    void update(T model);
    void delete(T modelWithId);
}
