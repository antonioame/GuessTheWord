package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Match;
import gruppo05.gtwshared.utility.Result;
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
public class MatchDAO implements DAO<Match>{

    private Match mapMatch(ResultSet rs) throws SQLException {
        return new Match(
                rs.getString("player"), 
                rs.getInt("challenge"), 
                Result.valueOf(rs.getString("result").toUpperCase()), 
                rs.getInt("timeToAnswer"));
    } 
    
    
    @Override
    public Optional<Match> selectById(Match modelWithId) {
        Optional<Match> result = Optional.empty();
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "SELECT * FROM match WHERE player = ? AND challenge = ?")) {
            cmd.setString(1, modelWithId.getPlayer());
            cmd.setInt(2, modelWithId.getChallenge());
            
            try (ResultSet rs = cmd.executeQuery()) {
                if(rs.next()) {
                    result = Optional.ofNullable(mapMatch(rs));
                }
            } // Eccezione gestita dal try-with-resources esterno
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
        
        return result;
    }
    
    @Override
    public List<Match> selectAll() {
        List<Match> result = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
                Statement cmd = conn.createStatement();
                ResultSet rs = cmd.executeQuery(
                        "SELECT * FROM match")) {
            
            while(rs.next()) {
                result.add(mapMatch(rs));
            }
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();            
        }
        
        return result;
    }

    @Override
    public void insert(Match model) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "INSERT INTO match (player, challenge, result, timeToAnswer) VALUES (?,?,?,?)")) {
            cmd.setString(1, model.getPlayer());
            cmd.setInt(2, model.getChallenge());
            cmd.setString(3, model.getResult().toString());
            cmd.setInt(4, model.getTimeToAnswer());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }    
    }

    @Override
    public void update(Match model) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "UPDATE match SET result = ?, timeToAnswer = ? WHERE player = ? AND challenge = ?")) {
            cmd.setString(1, model.getResult().toString());
            cmd.setInt(2, model.getTimeToAnswer());            
            cmd.setString(3, model.getPlayer());
            cmd.setInt(4, model.getChallenge());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }        
    }

    @Override
    public void delete(Match modelWithId) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "DELETE FROM match WHERE player = ? AND challenge = ?")) {
            cmd.setString(1, modelWithId.getPlayer());
            cmd.setInt(2, modelWithId.getChallenge());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }    
    }
    
}
