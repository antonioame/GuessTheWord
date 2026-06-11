package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Admin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author francesco-vecchione
 * @brief Implementazione dell'interfaccia AdminDAO per la gestione della persistenza degli oggetti Admin.
 * @invariant
 * La classe gestisce oggetti di tipo Admin identificati dallo username di tipo String.
 */
public class ConcreteAdminDAO implements AdminDAO {

    /**
     * @brief Converte una riga del ResultSet del DB in un oggetto Admin.
     * @param[inout] rs Il set dei risultati SQL posizionato sulla riga corrente da mappare.
     * @return L'oggetto Admin istanziato e popolato con i dati estratti dal ResultSet.
     * @pre
     * Il ResultSet non deve essere null e deve essere posizionato su una riga valida.
     * @post
     * L'oggetto Admin restituito non è null.
     */
    private Admin mapAdmin(ResultSet rs) throws SQLException {
        return new Admin(
                rs.getString("username"), 
                rs.getString("password"));
    }
    
    /**
     * @brief Recupera un amministratore tramite il suo username.
     * @copydoc AdminDAO#selectById(Optional)
     */
    @Override
    public Optional<Admin> selectById(Optional<String> username) {
        if(!username.isPresent()) return Optional.empty();
        
        Optional<Admin> result = Optional.empty();
        
        final String query = 
                "SELECT * " +
                "FROM admin " +
                "WHERE username = ?;";
        
        try(Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setString(1, username.get());
            
            try (ResultSet rs = cmd.executeQuery()) {
                if(rs.next()) {
                    result = Optional.ofNullable(mapAdmin(rs));
                }
            } // Eccezione gestita dal try-with-resources esterno
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
    
        return result;
    }
    
    /**
     * @brief Recupera tutte le istanze di amministratori memorizzate nel database.
     * @copydoc DAO#selectAll()
     */
    @Override
    public List<Admin> selectAll() {
        List<Admin> result = new ArrayList<>();
        
        final String query = 
                "SELECT * " +
                "FROM admin;";
        
        try (Connection conn = DatabaseManager.getConnection();
                Statement cmd = conn.createStatement();
                ResultSet rs = cmd.executeQuery(query)) {
            
            while(rs.next()) {
                result.add(mapAdmin(rs));
            }
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
        return result;
    }
    
    /**
     * @brief Inserisce un nuovo amministratore all'interno del database.
     * @copydoc DAO#insert(Object)
     * @post
     * Se il parametro model è null, l'operazione termina senza modificare il database.
     */
    @Override
    public void insert(Admin model) {
        if(model == null) return;
        
        final String query = 
                "INSERT INTO admin (username, password) " +
                "VALUES (?,?);";      
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            cmd.setString(1, model.getUsername());
            cmd.setString(2, model.getPassword());
            
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
    }
    
    /**
     * @brief Inserisce una lista di amministratori all'interno del database.
     * @copydoc DAO#insertAll(List)
     * @post
     * Se la lista è null o vuota, l'operazione termina senza modificare il database.
     * In caso di errore durante il batch, viene eseguito il rollback dell'intera transazione.
     */
    @Override
    public void insertAll(List<Admin> modelList) {
        if(modelList == null || modelList.isEmpty()) return;
        
        final String query = 
                "INSERT INTO admin (username, password) " +
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
                
                for(Admin model : modelList) {
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

    /**
     * @brief Aggiorna la password di un amministratore esistente all'interno del database.
     * @copydoc DAO#update(Object)
     * @post
     * Se il parametro model è null, l'operazione termina senza modificare il database.
     */
    @Override
    public void update(Admin model) {
        if(model == null) return;
        
        final String query = 
                "UPDATE admin " +
                "SET password = ? " +
                "WHERE username = ?;";        
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            cmd.setString(1, model.getPassword());
            cmd.setString(2, model.getUsername());
            
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
    }

    /**
     * @brief Cancella un amministratore dal database tramite il suo username.
     * @copydoc AdminDAO#delete(Optional)
     * @post
     * Se l'Optional è vuoto, l'operazione termina senza apportare modifiche.
     */
    @Override
    public void delete(Optional<String> username) {
        if(!username.isPresent()) return;
        
        final String query = 
                "DELETE FROM admin " +
                "WHERE username = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            
            cmd.setString(1, username.get());
            
            // Necessario se vogliamo far rispettare i vincoli di integrità referenziale
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
    }
}
