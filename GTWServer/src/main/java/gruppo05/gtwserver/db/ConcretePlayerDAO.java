package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @brief Implementazione dell'interfaccia PlayerDAO per la gestione della persistenza degli oggetti Player.
 * @invariant
 * La classe gestisce oggetti di tipo Player identificati dallo username di tipo String.
 */
public class ConcretePlayerDAO implements PlayerDAO {

    /**
     * @brief Converte una riga del ResultSet del DB in un oggetto Player.
     * @param[inout] rs Il set dei risultati SQL posizionato sulla riga corrente da mappare.
     * @return L'oggetto Player istanziato e popolato con i dati estratti dal ResultSet.
     * @pre
     * Il ResultSet non deve essere null e deve essere posizionato su una riga valida.
     * @post
     * L'oggetto Player restituito non è null.
     */
    private Player mapPlayer(ResultSet rs) throws SQLException {
        return new Player(
                rs.getString("username"),
                rs.getString("password"),
                rs.getInt("totalPlayedTime"),
                rs.getInt("totalGamesWon"),
                rs.getInt("totalGamesPlayed"));
    }
    
    /**
     * @brief Recupera un giocatore tramite il suo username.
     * @copydoc PlayerDAO#selectById(Optional)
     */
    @Override
    public Optional<Player> selectById(Optional<String> username) {
        if(!username.isPresent()) return Optional.empty();
        
        Optional<Player> result = Optional.empty();
        
        final String query = 
                "SELECT * " +
                "FROM player " +
                "WHERE username = ?;";
        
        try(Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setString(1, username.get());
            
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
    
    /**
     * @brief Recupera tutte le istanze di giocatori memorizzate nel database.
     * @copydoc DAO#selectAll()
     */
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

    /**
     * @brief Inserisce un nuovo giocatore all'interno del database.
     * @copydoc DAO#insert(Object)
     * @post
     * Se il parametro model è null, l'operazione termina senza modificare il database.
     * I campi relativi alle statistiche cumulative (totalPlayedTime, totalGamesWon, totalGamesPlayed) vengono inizializzati a 0 dal database come valore di default.
     */
    @Override
    public void insert(Player model) {
        if(model == null) return;
        
        // Le ridondanze sono impostate a 0 di default
        final String query = 
                "INSERT INTO player (username, password) " +
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
     * @brief Inserisce una lista di giocatori all'interno del database.
     * @copydoc DAO#insertAll(List)
     * @post
     * Se la lista è null o vuota, l'operazione termina senza modificare il database.
     * In caso di errore durante il batch, viene eseguito il rollback dell'intera transazione.
     * Per ogni record inserito con successo, le statistiche cumulative partono dal valore predefinito 0.
     */
    @Override
    public void insertAll(List<Player> modelList) {
        if(modelList == null || modelList.isEmpty()) return;
        
        final String query = 
                "INSERT INTO player (username, password) " +
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
    
    /**
     * @brief Aggiorna le credenziali di un giocatore esistente all'interno del database.
     * @copydoc DAO#update(Object)
     * @post
     * Se il parametro model è null, l'operazione termina senza modificare il database.
     * I campi delle statistiche cumulative non vengono alterati direttamente da questa query, in quanto la loro consistenza e il loro incremento sono delegati ai trigger interni del database all'inserimento delle partite.
     */
    @Override
    public void update(Player model) {
        if(model == null) return;
        
        // Le ridondanze vengono aggiornate grazie al trigger
        final String query = 
                "UPDATE player " +
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
     * @brief Cancella un giocatore dal database tramite il suo username.
     * @copydoc PlayerDAO#delete(Optional)
     * @post
     * Se l'Optional è vuoto, l'operazione termina senza apportare modifiche.
     */
    @Override
    public void delete(Optional<String> username) {
        if(!username.isPresent()) return;
        
        final String query = 
                "DELETE FROM player " +
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
