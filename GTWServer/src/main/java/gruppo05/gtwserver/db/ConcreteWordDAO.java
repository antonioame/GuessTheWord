package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Word;
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
 * @brief Implementazione dell'interfaccia WordDAO per la gestione della persistenza degli oggetti Word.
 * @invariant
 * La classe gestisce oggetti di tipo Word identificati da una chiave primaria composta da token (String) e source (int).
 */
public class ConcreteWordDAO implements WordDAO {
    
    /**
     * @brief Converte una riga del ResultSet del DB in un oggetto Word.
     * @param[inout] rs Il set dei risultati SQL posizionato sulla riga corrente da mappare.
     * @return L'oggetto Word istanziato e popolato con i dati estratti dal ResultSet.
     * @pre
     * Il ResultSet non deve essere null e deve essere posizionato su una riga valida.
     * @post
     * L'oggetto Word restituito non è null.
     */
    private Word mapWord(ResultSet rs) throws SQLException {
        return new Word(
                rs.getString("token"),
                rs.getInt("frequency"),
                rs.getInt("source"));
    }

    /**
     * @brief Recupera una specifica parola tramite la sua chiave primaria composta.
     *
     * L'interrogazione viene eseguita sulla vista 'availableWords', restituendo solo i record 
     * associati a sorgenti attive aventi un percorso di file system valido (path IS NOT NULL).
     * 
     * @copydoc WordDAO#selectById(Optional, Optional)
     * @post
     * Se almeno uno dei due Optional è vuoto, restituisce un Optional vuoto senza interrogare il database.
     */
    @Override
    public Optional<Word> selectById(Optional<String> token, Optional<Integer> source) {
        if(!token.isPresent() || !source.isPresent()) return Optional.empty();
        
        Optional<Word> result = Optional.empty();
        
        final String query = 
                "SELECT * " +
                "FROM availableWords " +
                "WHERE token = ? AND source = ?;";
        
        try(Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query)) {
            cmd.setString(1, token.get());
            cmd.setInt(2, source.get());
            
            try (ResultSet rs = cmd.executeQuery()) {
                if(rs.next()) {
                    result = Optional.ofNullable(mapWord(rs));
                }
            } // Eccezione gestita dal try-with-resources esterno
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
    
        return result;    
    }
    
    /**
     * @brief Recupera tutte le parole disponibili all'interno del sistema.
     *
     * L'interrogazione viene eseguita sulla vista 'availableWords', restituendo solo i record 
     * associati a sorgenti attive aventi un percorso di file system valido (path IS NOT NULL).
     * 
     * @copydoc DAO#selectAll()
     */
    @Override
    public List<Word> selectAll() {
        List<Word> result = new ArrayList<>();
        
        // Cerca solo tra le parole disponibili, ovvero tra le parole che sono
        // legate ad una sorgente con path DIVERSO da null
        final String query = 
                "SELECT * " +
                "FROM availableWords;";
        
        try (Connection conn = DatabaseManager.getConnection();
                Statement cmd = conn.createStatement();
                ResultSet rs = cmd.executeQuery(query)) {
            
            while(rs.next()) {
                result.add(mapWord(rs));
            }
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }
        return result;      
    }

    /**
     * @brief Filtra le parole disponibili in base ai parametri specificati.
     *
     * Come per il metodo selectAll(), la ricerca si appoggia alla vista 'availableWords' per escludere
     * preventivamente i record legati a sorgenti non configurate.
     *
     * @copydoc WordDAO#selectAllWhere(Optional, Optional, Optional)
     * @post
     * Se tutti i parametri forniti sono vuoti, il metodo delega l'esecuzione a selectAll().
     */
    @Override
    public List<Word> selectAllWhere(Optional<String> token, Optional<Integer> frequenza, Optional<Integer> source) {
        if(!token.isPresent() && !frequenza.isPresent() && !source.isPresent()) return selectAll();
        
        List<Word> result = new ArrayList<>();
        
        // Cerca solo tra le parole disponibili, ovvero tra le parole che sono
        // legate ad una sorgente con path DIVERSO da null
        StringBuffer query = new StringBuffer("Select * FROM availableWords WHERE ");
        
        List<String> conditions = new ArrayList<>();
        
        if(token.isPresent()) conditions.add("token = ?");
        if(frequenza.isPresent()) conditions.add("frequenza = ?");
        if(source.isPresent()) conditions.add("source = ?");
        
        query.append(String.join(" AND ", conditions) + ";");
        
        try(Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query.toString())) {
            int i = 1;
            if(token.isPresent()) cmd.setString(i++, token.get());
            if(frequenza.isPresent()) cmd.setInt(i++, frequenza.get());
            if(source.isPresent()) cmd.setInt(i++, source.get());        
            
            try (ResultSet rs = cmd.executeQuery()) {
                while(rs.next()) {
                    result.add(mapWord(rs));
                }
            } // Eccezione gestita dal try-with-resources esterno
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();            
        }
        
        return result;
    }
    
    /**
     * @brief Inserisce una nuova parola all'interno del database.
     * @copydoc DAO#insert(Object)
     * @post
     * Se il parametro model è null, l'operazione termina senza modificare il database.
     */
    @Override
    public void insert(Word model) {
        if(model == null) return;
        
        final String query = 
                "INSERT INTO word (token, frequency, source) " +
                "VALUES (?,?,?);";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            cmd.setString(1, model.getToken());
            cmd.setInt(2, model.getFrequency());
            cmd.setInt(3, model.getSource());
            
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }    
    }
    
    /**
     * @brief Inserisce una lista di parole all'interno del database mediante un'unica transazione batch.
     * @copydoc DAO#insertAll(List)
     * @post
     * Se la lista è null o vuota, l'operazione termina senza apportare modifiche.
     * In caso di fallimento di uno o più inserimenti, viene eseguito il rollback totale dei comandi del batch.
     */
    @Override
    public void insertAll(List<Word> modelList) {
        if(modelList == null || modelList.isEmpty()) return;
        
        final String query = 
                "INSERT INTO word (token, frequency, source) " +
                "VALUES (?,?,?);";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            try {
                // Il comando di abilitazione dei vincoli di integrità referenziale
                // deve essere abilitato fuori dalla transazione
                st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
                
                // Tutto deve essere eseguito in una transazione
                conn.setAutoCommit(false);
                
                for(Word model : modelList) {
                    cmd.setString(1, model.getToken());
                    cmd.setInt(2, model.getFrequency());
                    cmd.setInt(3, model.getSource());
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
     * @brief Aggiorna la frequenza di una parola esistente identificata dalla chiave composta.
     * @copydoc DAO#update(Object)
     * @post
     * Se il parametro model è null, l'operazione termina senza modificare il database.
     */
    @Override
    public void update(Word model) {
        if(model == null) return;
        
        final String query = 
                "UPDATE word " +
                "SET frequency = ? " +
                "WHERE token = ? AND source = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            cmd.setInt(1, model.getFrequency());
            cmd.setString(2, model.getToken());
            cmd.setInt(3, model.getSource());
            
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }   
    }

    /**
     * @brief Elimina una parola dal database basandosi sulla chiave token-sorgente.
     * @copydoc WordDAO#delete(Optional, Optional)
     * @post
     * Se almeno uno dei due Optional è vuoto, l'operazione viene interrotta senza applicare modifiche.
     */
    @Override
    public void delete(Optional<String> token, Optional<Integer> source) {
        if(!token.isPresent() || !source.isPresent()) return;
        
        final String query = 
                "DELETE FROM word " +
                "WHERE token = ? AND source = ?;";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(query);
                Statement st = conn.createStatement()) {
            
            cmd.setString(1, token.get());
            cmd.setInt(2, source.get());
            
            // Necessario se vogliamo far rispettare i vincoli di integrità referenziale
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            cmd.executeUpdate();
        } catch (SQLException ex) {
            // Debug: da cambiare
            ex.printStackTrace();
        }    
    }
    
}
