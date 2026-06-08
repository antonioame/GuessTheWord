/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Source;
import java.util.Optional;

/**
 *
 * @author francesco-vecchione
 */
public interface SourceDAO extends DAO<Source> {
    Optional<Source> selectById(Optional<Integer> id);
    void delete(Optional<Integer> id);
}
