package gruppo05.gtwserver.db;

import java.util.List;


public interface DAO<T> {
    List<T> selectAll();
    
    void insert(T model);
    
    void insertAll(List<T> modelList);
    
    void update(T model);
}
