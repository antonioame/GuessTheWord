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
                rs.getInt("totalMatchesWon"),
                rs.getInt("totalMatchesPlayed"));
    }
    
    @Override
    public Optional<Player> selectById(Player modelWithId) {
        Optional<Player> result = Optional.empty();
        try(Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "SELECT * FROM player WHERE username = ?")) {
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
        try (Connection conn = DriverManager.getConnection(URL);
                Statement cmd = conn.createStatement();
                ResultSet rs = cmd.executeQuery(
                        "SELECT * FROM player")) {
            
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
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "INSERT INTO player (username, password, totalPlayedTime, totalMatchesWon, totalMatchesPlayed) VALUES (?,?,?,?,?)")) {
            cmd.setString(1, model.getUsername());
            cmd.setString(2, model.getPassword());
            cmd.setInt(3, model.getTotalPlayedTime());
            cmd.setInt(4, model.getTotalMatchesWon());
            cmd.setInt(5, model.getTotalMatchesPlayed());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }    
    }

    
    // NOTA: Capire come gestire l'addizione delle ridondanze
    @Override
    public void update(Player model) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "UPDATE player SET password = ?, totalPlayedTime = ?, totalMatchesWon = ?, totalMatchesPlayed = ? WHERE username = ?")) {
            cmd.setString(1, model.getPassword());
            cmd.setInt(2, model.getTotalPlayedTime());
            cmd.setInt(3, model.getTotalMatchesWon());
            cmd.setInt(4, model.getTotalMatchesPlayed());
            cmd.setString(5, model.getUsername());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }     
    }

    @Override
    public void delete(Player modelWithId) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "DELETE FROM player WHERE username = ?")) {
            cmd.setString(1, modelWithId.getUsername());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }        
    }
    
}
