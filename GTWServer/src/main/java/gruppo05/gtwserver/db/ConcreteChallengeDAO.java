package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Challenge;
import gruppo05.gtwshared.utility.Difficulty;
import java.sql.Connection;
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
 * 
 * @brief Implementazione dell'interfaccia ChallengeDAO per la gestione della persistenza degli oggetti Challenge.
 * @invariant
 * La classe gestisce oggetti di tipo Challenge identificati da una chiave di tipo ChallengeId.
 */
public class ConcreteChallengeDAO implements ChallengeDAO {

    /**
     * @brief Converte una riga del ResultSet del DB in un oggetto Challenge.
     * @param[inout] rs Il set dei risultati SQL posizionato sulla riga corrente da mappare.
     * @return L'oggetto Challenge istanziato e popolato con i dati estratti dal ResultSet.
     * @pre
     * Il ResultSet non deve essere null e deve essere posizionato su una riga valida.
     * @post
     * L'oggetto Challenge restituito non è null.
     */
    private Challenge mapChallenge(ResultSet rs) throws SQLException {
        return new Challenge(
                rs.getInt("code"), 
                rs.getDate("date"),
                Difficulty.valueOf(rs.getString("difficulty").toUpperCase()),
                rs.getString("word"),
                rs.getInt("source"));
    }
    
    @Override
    public Optional<Challenge> selectById(Optional<Integer> code) {
        if(!code.isPresent()) return Optional.empty();
        
        Optional<Challenge> result = Optional.empty();
        
        final String query = 
                "SELECT * " +
                "FROM challenge " +
                "WHERE code = ?;";        
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setInt(1, code.get());
            
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
        
        final String query = 
                "SELECT * " +
                "FROM challenge;";
        
        try (Connection conn = DatabaseManager.getConnection();
                Statement cmd = conn.createStatement();
                ResultSet rs = cmd.executeQuery(query)) {
            
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
        if(model == null) return;
        
        final String query = 
                "INSERT INTO challenge (code, date, difficulty, word, source) " +
                "VALUES (?,?,?,?,?);";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
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
    public void insertAll(List<Challenge> modelList) {
        if(modelList == null || modelList.isEmpty()) return;
    
        final String query = 
                "INSERT INTO challenge (code, date, difficulty, word, source) " +
                "VALUES (?,?,?,?,?);";        
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            try {
                // Tutto deve essere eseguito in una transazione
                conn.setAutoCommit(false);
                
                for(Challenge model : modelList) {
                    cmd.setInt(1, model.getCode());
                    cmd.setDate(2, model.getDate());
                    cmd.setString(3, model.getDifficulty().toString());
                    cmd.setString(4, model.getWord());
                    cmd.setInt(5, model.getSource());
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
    public void update(Challenge model) {
        if(model == null) return;
        
        final String query = 
                "UPDATE challenge " +
                "SET date = ?, difficulty = ?, word = ?, source = ? " +
                "WHERE code = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
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
    public void delete(Optional<Integer> code) {
        if(!code.isPresent()) return;
        
        final String query = 
                "DELETE FROM challenge " +
                "WHERE code = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            
            // Necessario se vogliamo far rispettare i vincoli di integrità referenziale
            cmd.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            
            cmd.setInt(1, code.get());
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace(); 
        }
    }
}
