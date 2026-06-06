package gruppo05.gtwserver.db;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author francesco-vecchione
 */
public interface DAO<T, K> {
    Optional<T> selectById(K modelId);
    List<T> selectAll();
    void insert(T model);
    void insertAll(List<T> modelList);
    void update(T model);
    void delete(K modelId);
    
    default List<T> selectAllWhere(Predicate<T> condition) {
        return selectAll().stream().filter(condition).collect(Collectors.toList());
    }
}
