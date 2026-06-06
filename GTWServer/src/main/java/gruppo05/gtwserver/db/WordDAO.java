package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Word;
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
public class WordDAO implements DAO<Word>{
    
    private Word mapWord(ResultSet rs) throws SQLException {
        return new Word(
                rs.getString("token"),
                rs.getInt("frequency"),
                rs.getInt("source"));
    }

    @Override
    public Optional<Word> selectById(Word modelWithId) {
        Optional<Word> result = Optional.empty();
        
        final String query = 
                "SELECT * " +
                "FROM word " +
                "WHERE token = ? AND source = ?;";
        
        try(Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setString(1, modelWithId.getToken());
            cmd.setInt(2, modelWithId.getSource());
            
            try (ResultSet rs = cmd.executeQuery()) {
                if(rs.next()) {
                    result = Optional.ofNullable(mapWord(rs));
                }
            } // Eccezione gestita dal try-with-resources esterno
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
    
        return result;    
    }
    
    @Override
    public List<Word> selectAll() {
        List<Word> result = new ArrayList<>();
        
        final String query = 
                "SELECT * " +
                "FROM word;";
        
        try (Connection conn = DatabaseManager.getConnection();
                Statement cmd = conn.createStatement();
                ResultSet rs = cmd.executeQuery(query)) {
            
            while(rs.next()) {
                result.add(mapWord(rs));
            }
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
        return result;      
    }

    @Override
    public void insert(Word model) {
        
        final String query = 
                "INSERT INTO word (token, frequency, source) " +
                "VALUES (?,?,?);";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setString(1, model.getToken());
            cmd.setInt(2, model.getFrequency());
            cmd.setInt(3, model.getSource());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }    
    }

    @Override
    public void update(Word model) {
        
        final String query = 
                "UPDATE word " +
                "SET frequency = ? " +
                "WHERE token = ? AND source = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setInt(1, model.getFrequency());
            cmd.setString(2, model.getToken());
            cmd.setInt(3, model.getSource());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }   
    }

    @Override
    public void delete(Word modelWithId) {
        
        final String query = 
                "DELETE FROM word " +
                "WHERE token = ? AND source = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            
            // Necessario se vogliamo far rispettare i vincoli di integrità referenziale
            cmd.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            
            cmd.setString(1, modelWithId.getToken());
            cmd.setInt(2, modelWithId.getSource());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }    
    }
    
}
