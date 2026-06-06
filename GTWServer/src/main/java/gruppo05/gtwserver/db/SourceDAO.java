package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Source;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author francesco-vecchione
 */
public class SourceDAO implements DAO<Source>{
 
    private Source mapSource(ResultSet rs) throws SQLException {        
        // NOTA: Se il path presente nel record è null o non valido potrebbe lanciare un'eccezione che non è di tipo SQLException
        // Capire come gestire il caso
        return new Source(
                rs.getInt("id"),
                Paths.get(rs.getString("path")));
        
    }
    
    @Override
    public Optional<Source> selectById(Source modelWithId) {
        Optional<Source> result = Optional.empty();
        try(Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "SELECT * FROM source WHERE id = ?")) {
            cmd.setInt(1, modelWithId.getId());
            
            try (ResultSet rs = cmd.executeQuery()) {
                if(rs.next()) {
                    result = Optional.ofNullable(mapSource(rs));
                }
            } // Eccezione gestita dal try-with-resources esterno
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
    
        return result;        
    }
    
    @Override
    public List<Source> selectAll() {
        List<Source> result = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
                Statement cmd = conn.createStatement();
                ResultSet rs = cmd.executeQuery(
                        "SELECT * FROM source")) {
            
            while(rs.next()) {
                result.add(mapSource(rs));
            }
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
        return result;        
    }

    @Override
    public void insert(Source model) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "INSERT INTO source (id, path) VALUES (?,?)")) {
            cmd.setInt(1, model.getId());
            cmd.setString(2, model.getPath().toString());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }    
    }

    @Override
    public void update(Source model) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "UPDATE source SET path = ? WHERE id = ?")) {
            cmd.setString(1, model.getPath().toString());
            cmd.setInt(2, model.getId());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }    
    }

    @Override
    public void delete(Source modelWithId) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "DELETE FROM source WHERE id = ?")) {
            cmd.setInt(1, modelWithId.getId());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }    
    }
    
}
