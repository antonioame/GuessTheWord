package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Player;
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
public class PlayerDAO implements DAO<Player>{
    
    private Player mapPlayer(ResultSet rs) throws SQLException {
        return new Player(
                rs.getString("username"),
                rs.getString("password"),
                rs.getInt("totalPlayedTime"),
                rs.getInt("totalGamesWon"),
                rs.getInt("totalGamesPlayed"));
    }
    
    @Override
    public Optional<Player> selectById(Player modelWithId) {
        Optional<Player> result = Optional.empty();
        
        final String query = 
                "SELECT * " +
                "FROM player " +
                "WHERE username = ?;";
        
        try(Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setString(1, modelWithId.getUsername());
            
            try (ResultSet rs = cmd.executeQuery()) {
                if(rs.next()) {
                    result = Optional.ofNullable(mapPlayer(rs));
                }
            } // Eccezione gestita dal try-with-resources esterno
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
    
        return result;    
    }
    
    @Override
    public List<Player> selectAll() {
        List<Player> result = new ArrayList<>();
        
        final String query = 
                "SELECT * " +
                "FROM player;";
        
        try (Connection conn = DatabaseManager.getConnection();
                Statement cmd = conn.createStatement();
                ResultSet rs = cmd.executeQuery(query)) {
            
            while(rs.next()) {
                result.add(mapPlayer(rs));
            }
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
        return result;    
    }

    @Override
    public void insert(Player model) {
        
        // Le ridondanze sono impostate a 0 di default
        final String query = 
                "INSERT INTO player (username, password) " +
                "VALUES (?,?);";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setString(1, model.getUsername());
            cmd.setString(2, model.getPassword());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }    
    }

    
    @Override
    public void update(Player model) {
        
        // Le ridondanze vengono aggiornate grazie al trigger
        final String query = 
                "UPDATE player " +
                "SET password = ? " +
                "WHERE username = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setString(1, model.getPassword());
            cmd.setString(2, model.getUsername());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }     
    }

    @Override
    public void delete(Player modelWithId) {
        
        final String query = 
                "DELETE FROM player " +
                "WHERE username = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            
            // Necessario se vogliamo far rispettare i vincoli di integrità referenziale
            cmd.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            
            cmd.setString(1, modelWithId.getUsername());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }        
    }
    
}
