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
 * 
 * @brief Implementazione dell'interfaccia GameDAO per la gestione della persistenza degli oggetti Game.
 * @invariant
 * La classe gestisce oggetti di tipo Game identificati da una chiave di tipo GameId.
 */
public class ConcreteGameDAO implements GameDAO {

    /**
     * @brief Converte una riga del ResultSet del DB in un oggetto Game.
     * @param[inout] rs Il set dei risultati SQL posizionato sulla riga corrente da mappare.
     * @return L'oggetto Game istanziato e popolato con i dati estratti dal ResultSet.
     * @pre
     * Il ResultSet non deve essere null e deve essere posizionato su una riga valida.
     * @post
     * L'oggetto Game restituito non è null.
     */
    private Game mapGame(ResultSet rs) throws SQLException {
        return new Game(
                rs.getString("player"), 
                rs.getInt("challenge"), 
                Result.valueOf(rs.getString("result").toUpperCase()), 
                rs.getInt("responseTime"));
    } 
    
    
    @Override
    public Optional<Game> selectById(Optional<String> player, Optional<Integer> challenge) {
        // Essendo che il metodo deve tornare un unico record, entrambi i campi della chiave
        // devono essere svalorizzati
        if(!player.isPresent() || !challenge.isPresent()) return Optional.empty();
        
        Optional<Game> result = Optional.empty();
        
        final String query = 
                "SELECT * " +
                "FROM game " +
                "WHERE player = ? AND challenge = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setString(1, player.get());
            cmd.setInt(2, challenge.get());
            
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
        if(model == null) return;
        
        final String query = 
                "INSERT INTO game (player, challenge, result, responseTime) " +
                "VALUES (?,?,?,?);";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            cmd.setString(1, model.getPlayer());
            cmd.setInt(2, model.getChallenge());
            cmd.setString(3, model.getResult().toString());
            cmd.setInt(4, model.getResponseTime());
            
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }    
    }

    @Override
    public void insertAll(List<Game> modelList) {
        if(modelList == null || modelList.isEmpty()) return;

        final String query = 
                "INSERT INTO game (player, challenge, result, responseTime) " +
                "VALUES (?,?,?,?);";        
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            try {
                // Il comando di abilitazione dei vincoli di integrità referenziale
                // deve essere abilitato fuori dalla transazione
                st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
                
                // Tutto deve essere eseguito in una transazione
                conn.setAutoCommit(false);
                
                for(Game model : modelList) {
                    cmd.setString(1, model.getPlayer());
                    cmd.setInt(2, model.getChallenge());
                    cmd.setString(3, model.getResult().toString());
                    cmd.setInt(4, model.getResponseTime());
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
    public void update(Game model) {
        if(model == null) return;
        
        final String query = 
                "UPDATE game " +
                "SET result = ?, responseTime = ? " +
                "WHERE player = ? AND challenge = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            cmd.setString(1, model.getResult().toString());
            cmd.setInt(2, model.getResponseTime());            
            cmd.setString(3, model.getPlayer());
            cmd.setInt(4, model.getChallenge());
            
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }        
    }

    @Override
    public void delete(Optional<String> player, Optional<Integer> challenge) {
        // Essendo che il metodo deve tornare un unico record, entrambi i campi della chiave
        // devono essere svalorizzati
        if(!player.isPresent() || !challenge.isPresent()) return;
        
        final String query = 
                "DELETE FROM game " +
                "WHERE player = ? AND challenge = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            
            cmd.setString(1, player.get());
            cmd.setInt(2, challenge.get());
            
            // Necessario se vogliamo far rispettare i vincoli di integrità referenziale
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }    
    }
    
}
