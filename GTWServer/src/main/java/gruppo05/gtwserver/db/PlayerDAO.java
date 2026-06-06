package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Player;
import gruppo05.gtwserver.model.PlayerId;
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
public class PlayerDAO implements DAO<Player, PlayerId>{
    
    private Player mapPlayer(ResultSet rs) throws SQLException {
        return new Player(
                rs.getString("username"),
                rs.getString("password"),
                rs.getInt("totalPlayedTime"),
                rs.getInt("totalGamesWon"),
                rs.getInt("totalGamesPlayed"));
    }
    
    @Override
    public Optional<Player> selectById(PlayerId modelId) {
        if(modelId == null) return Optional.empty();
        
        Optional<Player> result = Optional.empty();
        
        final String query = 
                "SELECT * " +
                "FROM player " +
                "WHERE username = ?;";
        
        try(Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setString(1, modelId.getUsername());
            
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
        if(model == null) return;
        
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
    public void insertAll(List<Player> modelList) {
        if(modelList == null || modelList.isEmpty()) return;
        
        final String query = 
                "INSERT INTO player (username, password) " +
                "VALUES (?,?);";     
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            try {
                // Tutto deve essere eseguito in una transazione
                conn.setAutoCommit(false);
                
                for(Player model : modelList) {
                    cmd.setString(1, model.getUsername());
                    cmd.setString(2, model.getPassword());
                    // Aggiungi la query al pacchetto di comandi da eseguire
                    cmd.addBatch();
                } 
                
                cmd.executeBatch();
                conn.commit();
            } catch (SQLException sqle) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new SQLException("Commit fallito - Rollback fallito", ex);
                }
                throw new SQLException("Commit fallito - Rollback effettuato", sqle);
            }
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }        
    }
    
    @Override
    public void update(Player model) {
        if(model == null) return;
        
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
    public void delete(PlayerId modelId) {
        if(modelId == null) return;
        
        final String query = 
                "DELETE FROM player " +
                "WHERE username = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            
            // Necessario se vogliamo far rispettare i vincoli di integrità referenziale
            cmd.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            
            cmd.setString(1, modelId.getUsername());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }        
    }
    
}
