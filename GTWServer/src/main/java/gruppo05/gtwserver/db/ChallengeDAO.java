package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Challenge;
import gruppo05.gtwshared.utility.Difficulty;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author francesco-vecchione
 */
public class ChallengeDAO implements DAO<Challenge>{

    private Challenge mapChallenge(ResultSet rs) throws SQLException {
        return new Challenge(
                rs.getInt("code"), 
                rs.getDate("date"),
                Difficulty.valueOf(rs.getString("difficulty").toUpperCase()),
                rs.getString("word"),
                rs.getInt("source"));
    }
    
    @Override
    public Optional<Challenge> selectById(Challenge modelWithId) {
        Optional<Challenge> result = Optional.empty();
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "SELECT * FROM challenge WHERE code = ?")) {
            cmd.setInt(1, modelWithId.getCode());
            
            try (ResultSet rs = cmd.executeQuery()) {
                if(rs.next()) {
                    result = Optional.ofNullable(mapChallenge(rs));
                }
            } // Eccezione gestita dal try-with-resources esterno
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
        
        return result;
    }
    
    @Override
    public List<Challenge> selectAll() {
        List<Challenge> result = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
                Statement cmd = conn.createStatement();
                ResultSet rs = cmd.executeQuery(
                        "SELECT * FROM challenge")) {
            
            while(rs.next()) {
                result.add(mapChallenge(rs));
            }
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();            
        }
        
        return result;
    }
    
    @Override
    public void insert(Challenge model) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "INSERT INTO challenge (code, date, difficulty, word, source) VALUES (?,?,?,?,?)")) {
            cmd.setInt(1, model.getCode());
            cmd.setDate(2, model.getDate());
            cmd.setString(3, model.getDifficulty().toString());
            cmd.setString(4, model.getWord());
            cmd.setInt(5, model.getSource());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }
    }

    @Override
    public void update(Challenge model) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "UPDATE challenge SET date = ?, difficulty = ?, word = ?, source = ? WHERE code = ?")) {
            cmd.setDate(1, model.getDate());
            cmd.setString(2, model.getDifficulty().toString());
            cmd.setString(3, model.getWord());
            cmd.setInt(4, model.getSource());
            cmd.setInt(5, model.getCode());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }
    }

    @Override
    public void delete(Challenge modelWithId) {
        try (Connection conn = DriverManager.getConnection(URL);
                PreparedStatement cmd = conn.prepareStatement(
                        "DELETE FROM challenge WHERE code = ?")) {
            cmd.setInt(1, modelWithId.getCode());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }
    }
}
