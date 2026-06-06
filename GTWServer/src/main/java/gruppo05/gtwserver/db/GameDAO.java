package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Game;
import gruppo05.gtwshared.utility.Result;
import java.sql.Connection;
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
public class GameDAO implements DAO<Game>{

    private Game mapGame(ResultSet rs) throws SQLException {
        return new Game(
                rs.getString("player"), 
                rs.getInt("challenge"), 
                Result.valueOf(rs.getString("result").toUpperCase()), 
                rs.getInt("timeToAnswer"));
    } 
    
    
    @Override
    public Optional<Game> selectById(Game modelWithId) {
        Optional<Game> result = Optional.empty();
        
        final String query = 
                "SELECT * " +
                "FROM game " +
                "WHERE player = ? AND challenge = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setString(1, modelWithId.getPlayer());
            cmd.setInt(2, modelWithId.getChallenge());
            
            try (ResultSet rs = cmd.executeQuery()) {
                if(rs.next()) {
                    result = Optional.ofNullable(mapGame(rs));
                }
            } // Eccezione gestita dal try-with-resources esterno
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
        
        return result;
    }
    
    @Override
    public List<Game> selectAll() {
        List<Game> result = new ArrayList<>();
        
        final String query = 
                "SELECT * " +
                "FROM game;";
        
        try (Connection conn = DatabaseManager.getConnection();
                Statement cmd = conn.createStatement();
                ResultSet rs = cmd.executeQuery(query)) {
            
            while(rs.next()) {
                result.add(mapGame(rs));
            }
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();            
        }
        
        return result;
    }

    @Override
    public void insert(Game model) {
        
        final String query = 
                "INSERT INTO game (player, challenge, result, timeToAnswer) " +
                "VALUES (?,?,?,?);";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setString(1, model.getPlayer());
            cmd.setInt(2, model.getChallenge());
            cmd.setString(3, model.getResult().toString());
            cmd.setInt(4, model.getResponseTime());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }    
    }

    @Override
    public void update(Game model) {
        
        final String query = 
                "UPDATE game " +
                "SET result = ?, timeToAnswer = ? " +
                "WHERE player = ? AND challenge = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setString(1, model.getResult().toString());
            cmd.setInt(2, model.getResponseTime());            
            cmd.setString(3, model.getPlayer());
            cmd.setInt(4, model.getChallenge());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }        
    }

    @Override
    public void delete(Game modelWithId) {
        
        final String query = 
                "DELETE FROM game " +
                "WHERE player = ? AND challenge = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            
            // Necessario se vogliamo far rispettare i vincoli di integrità referenziale
            cmd.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            
            cmd.setString(1, modelWithId.getPlayer());
            cmd.setInt(2, modelWithId.getChallenge());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }    
    }
    
}
