package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Source;
import java.nio.file.Paths;
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
 * @brief Implementazione dell'interfaccia SourceDAO per la gestione della persistenza degli oggetti Source.
 * @invariant
 * La classe gestisce oggetti di tipo Source identificati da una chiave di tipo SourceId.
 */
public class ConcreteSourceDAO implements SourceDAO {
 
    /**
     * @brief Converte una riga del ResultSet del DB in un oggetto Source.
     * @param[inout] rs Il set dei risultati SQL posizionato sulla riga corrente da mappare.
     * @return L'oggetto Source istanziato e popolato con i dati estratti dal ResultSet.
     * @pre
     * Il ResultSet non deve essere null e deve essere posizionato su una riga valida.
     * @post
     * L'oggetto Source restituito non è null.
     */
    private Source mapSource(ResultSet rs) throws SQLException {        
        // NOTA: Se il path presente nel record è null o non valido potrebbe lanciare un'eccezione che non è di tipo SQLException
        // Capire come gestire il caso
        return new Source(
                rs.getInt("id"),
                Paths.get(rs.getString("path")));
        
    }
    
    @Override
    public Optional<Source> selectById(Optional<Integer> id) {
        if(!id.isPresent()) return Optional.empty();
        
        Optional<Source> result = Optional.empty();
        
        final String query = 
                "SELECT * " +
                "FROM source " +
                "WHERE id = ? AND path IS NOT NULL;";
        
        try(Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setInt(1, id.get());
            
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
        
        final String query = 
                "SELECT * " +
                "FROM source " +
                "WHERE path IS NOT NULL;";
        
        try (Connection conn = DatabaseManager.getConnection();
                Statement cmd = conn.createStatement();
                ResultSet rs = cmd.executeQuery(query)) {
            
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
        if(model == null) return;
        
        final String query = 
                "INSERT INTO source (id, path) " +
                "VALUES (?,?);";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            cmd.setInt(1, model.getId());
            cmd.setString(2, model.getPath().toString());
            
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }    
    }

    @Override
    public void insertAll(List<Source> modelList) {
        if(modelList == null || modelList.isEmpty()) return;
        
        final String query = 
                "INSERT INTO source (id, path) " +
                "VALUES (?,?);";     
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            try {
                // Il comando di abilitazione dei vincoli di integrità referenziale
                // deve essere abilitato fuori dalla transazione
                st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
                
                // Tutto deve essere eseguito in una transazione
                conn.setAutoCommit(false);
                
                for(Source model : modelList) {
                    cmd.setInt(1, model.getId());
                    cmd.setString(2, model.getPath().toString());
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
    public void update(Source model) {
        if(model == null) return;
        
        final String query = 
                "UPDATE source " +
                "SET path = ? " +
                "WHERE id = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            cmd.setString(1, model.getPath().toString());
            cmd.setInt(2, model.getId());
            
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }    
    }

    @Override
    public void delete(Optional<Integer> id) {
        if(!id.isPresent()) return;
        
        String query = 
                "DELETE FROM source " +
                "WHERE id = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            
            cmd.setInt(1, id.get());
            
            // Necessario se vogliamo far rispettare i vincoli di integrità referenziale
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }    
    }
    
}
