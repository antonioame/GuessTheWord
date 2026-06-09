/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Word;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author francesco-vecchione
 */
public interface WordDAO extends DAO<Word> {
    Optional<Word> selectById(Optional<String> token, Optional<Integer> source);
    List<Word> selectAllWhere(Optional<String> token, Optional<Integer> frequenza, Optional<Integer> source);
    void delete(Optional<String> token, Optional<Integer> source);
}
