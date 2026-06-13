package gruppo05.gtwserver.db;

import gruppo05.gtwshared.utility.Difficulty;
import gruppo05.gtwshared.utility.Result;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * @brief Classe di utility per il testing e il debug del Database.
 * 
 * Consente il popolamento diretto delle tabelle del database tramite query SQL batch,
 * bypassando i DAO per garantire uno stato iniziale pulito, controllato e deterministico 
 * prima dell'esecuzione dei test d'integrazione o di sistema.
 */
public class DebugDB {
    
    /** 
     * @brief Query per l'inserimento diretto di un amministratore. 
     */
    public static final String INSERT_ADMIN = 
            "INSERT INTO admin (username, password) " +
            "VALUES (?,?);";
    
    /** 
     * @brief Query per l'inserimento diretto di un giocatore. 
     */
    public static final String INSERT_PLAYER = 
            "INSERT INTO player (username, password) " +
            "VALUES (?,?);";
    
    /** 
     * @brief Query per l'inserimento diretto del record di una partita. 
     */
    public static final String INSERT_GAME = 
            "INSERT INTO game (player, challenge, result, responseTime) " +
            "VALUES (?,?,?,?);";
    
    /** 
     * @brief Query per l'inserimento diretto di una sfida (challenge). 
     */
    public static final String INSERT_CHALLENGE = 
            "INSERT INTO challenge (code, date, difficulty, word, source) " +
            "VALUES (?,?,?,?,?);";
    
    /** 
     * @brief Query per l'inserimento diretto di una parola. 
     */
    public static final String INSERT_WORD = 
            "INSERT INTO word (token, frequency, source) " +
            "VALUES (?,?,?);";
    
    /** 
     * @brief Query per l'inserimento diretto di una sorgente di testo. 
     */
    public static final String INSERT_SOURCE = 
            "INSERT INTO source (id, path) " +
            "VALUES (?,?);";
    
    /**
     * @brief Inizializza il database con un dataset completo di debug senza ricorrere ai DAO.
     * 
     * Il metodo esegue l'intera procedura di popolamento all'interno di una transazione SQL atomica.
     * Prima dell'esecuzione dei batch, vengono attivati i vincoli di integrità referenziale (Foreign Keys).
     * In caso di errore durante uno qualsiasi degli inserimenti, viene invocato il rollback per ripristinare 
     * lo stato precedente.
     * 
     * @note Stato delle ridondanze atteso per ciascun giocatore a seguito dell'esecuzione dei trigger di database:
     * | Giocatore       | totalPlayedTime | totalGamesWon | totalGamesPlayed |
     * | :-------------- | :-------------- | :------------ | :--------------- |
     * | FrancoNeri      | 0               | 0             | 0                |
     * | AlexGiallo      | 55              | 2             | 3                |
     * | CarloBlu        | 50              | 1             | 2                |
     * | RobertoViola    | 60              | 1             | 3                |
     * | CarmineMagenta  | 50              | 0             | 2                |
     * 
     * @throws SQLException Se si verifica un errore irreparabile di commit o rollback.
     */
    public void initDebugDBWithoutDAO() throws SQLException {
        try(Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmdAdmin = conn.prepareStatement(INSERT_ADMIN);
                PreparedStatement cmdPlayer = conn.prepareStatement(INSERT_PLAYER);
                PreparedStatement cmdGame = conn.prepareStatement(INSERT_GAME);
                PreparedStatement cmdChallenge = conn.prepareStatement(INSERT_CHALLENGE);
                PreparedStatement cmdWord = conn.prepareStatement(INSERT_WORD);
                PreparedStatement cmdSource = conn.prepareStatement(INSERT_SOURCE);
                Statement st = conn.createStatement();) {
            
            try {
                
                // Il comando di abilitazione dei vincoli di integrità referenziale
                // deve essere abilitato fuori dalla transazione
                st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
                
                conn.setAutoCommit(false);

                insertDebugAdmins(cmdAdmin);
                insertDebugPlayers(cmdPlayer);
                insertDebugSource(cmdSource);
                insertDebugWords(cmdWord);
                insertDebugChallenges(cmdChallenge);
                insertDebugGames(cmdGame);

                conn.commit();
                
            } catch (SQLException sqle) {
                
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new SQLException("Commit fallito - Rollback fallito", ex.getMessage(), ex);
                }
                throw new SQLException("Commit fallito - Rollback effettuato", sqle.getMessage(), sqle);
            }
            
        } catch (SQLException ex) {
            // Debug
            ex.printStackTrace();
        }
    }
    
    /**
     * @brief Popola in modalità batch gli amministratori di debug.
     * @param[inout] cmd Lo statement preparato associato alla tabella admin.
     * @throws SQLException In caso di anomalie nei parametri o nella scrittura del batch.
     */
    private void insertDebugAdmins(PreparedStatement cmd) throws SQLException {
        
        cmd.setString(1, "MarioRossi");
        cmd.setString(2, "nunciafac@");
        // Aggiungi query al batch
        cmd.addBatch();
        
        cmd.setString(1, "GiuseppeVerdi");
        cmd.setString(2, "nop3rfavoreno");
        // Aggiungi query al batch
        cmd.addBatch();
        
        // Esegui
        cmd.executeBatch();
    }
    
    /**
     * @brief Popola in modalità batch i giocatori di debug.
     * @param[inout] cmd Lo statement preparato associato alla tabella player.
     * @throws SQLException In caso di anomalie nei parametri o nella scrittura del batch.
     */    
    private void insertDebugPlayers(PreparedStatement cmd) throws SQLException {
        
        cmd.setString(1, "FrancoNeri");
        cmd.setString(2, "piPPoparrucco");
        // Aggiungi query al batch
        cmd.addBatch();
        
        cmd.setString(1, "AlexGiallo");
        cmd.setString(2, "nunciafacc");
        // Aggiungi query al batch
        cmd.addBatch();
        
        cmd.setString(1, "CarloBlu");
        cmd.setString(2, "oscaRr4fon");
        // Aggiungi query al batch
        cmd.addBatch();
        
        cmd.setString(1, "RobertoViola");
        cmd.setString(2, "ob0bbyConlar4Igan");
        // Aggiungi query al batch
        cmd.addBatch();
        
        cmd.setString(1, "CarmineMagenta");
        cmd.setString(2, "nuChIvtrop9P");
        // Aggiungi query al batch
        cmd.addBatch();        
        
        // Esegui
        cmd.executeBatch();
    }
    
    /**
     * @brief Popola in modalità batch le sessioni di gioco di debug.
     * @param[inout] cmd Lo statement preparato associato alla tabella game.
     * @throws SQLException In caso di anomalie nei parametri o nella scrittura del batch.
     */
    private void insertDebugGames(PreparedStatement cmd) throws SQLException {
        
        // game 1 RobertoViola vs CarmineMagenta
        cmd.setString(1, "RobertoViola");
        cmd.setInt(2, 1);
        cmd.setString(3, Result.WIN.toString());
        cmd.setInt(4, 20);
        // Aggiungi query al batch
        cmd.addBatch();
        cmd.setString(1, "CarmineMagenta");
        cmd.setInt(2, 1);
        cmd.setString(3, Result.LOSE.toString());
        cmd.setInt(4, 25);
        // Aggiungi query al batch
        cmd.addBatch();

        // game 2 CarloBlu vs AlexGiallo
        cmd.setString(1, "CarloBlu");
        cmd.setInt(2, 2);
        cmd.setString(3, Result.DRAW.toString());
        cmd.setInt(4, 30);
        // Aggiungi query al batch
        cmd.addBatch();
        cmd.setString(1, "AlexGiallo");
        cmd.setInt(2, 2);
        cmd.setString(3, Result.DRAW.toString());
        cmd.setInt(4, 30);
        // Aggiungi query al batch
        cmd.addBatch();
        
        // game 3 RobertoViola vs CarloBlu
        cmd.setString(1, "RobertoViola");
        cmd.setInt(2, 3);
        cmd.setString(3, Result.LOSE.toString());
        cmd.setInt(4, 15);
        // Aggiungi query al batch
        cmd.addBatch();
        cmd.setString(1, "CarloBlu");
        cmd.setInt(2, 3);
        cmd.setString(3, Result.WIN.toString());
        cmd.setInt(4, 20);
        // Aggiungi query al batch
        cmd.addBatch();
        
        // game 4 AlexGiallo vs CarmineMagenta
        cmd.setString(1, "AlexGiallo");
        cmd.setInt(2, 4);
        cmd.setString(3, Result.WIN.toString());
        cmd.setInt(4, 20);
        // Aggiungi query al batch
        cmd.addBatch();
        cmd.setString(1, "CarmineMagenta");
        cmd.setInt(2, 4);
        cmd.setString(3, Result.LOSE.toString());
        cmd.setInt(4, 25);
        // Aggiungi query al batch
        cmd.addBatch();
        
        // game 5 AlexGiallo vs RobertoViola
        cmd.setString(1, "AlexGiallo");
        cmd.setInt(2, 5);
        cmd.setString(3, Result.WIN.toString());
        cmd.setInt(4, 5);
        // Aggiungi query al batch
        cmd.addBatch();      
        cmd.setString(1, "RobertoViola");
        cmd.setInt(2, 5);
        cmd.setString(3, Result.LOSE.toString());
        cmd.setInt(4, 25);
        // Aggiungi query al batch
        cmd.addBatch();     

        // Esegui
        cmd.executeBatch();
    }
    
    /**
     * @brief Popola in modalità batch le sfide giornaliere (challenges) di debug.
     * @param[inout] cmd Lo statement preparato associato alla tabella challenge.
     * @throws SQLException In caso di anomalie nei parametri o nella scrittura del batch.
     */
    private void insertDebugChallenges(PreparedStatement cmd) throws SQLException {
        
        cmd.setInt(1, 1);
        //cmd.setDate(2, Date.valueOf(LocalDate.of(2026, 6, 9)));               // non funziona per SQLite
        cmd.setString(2, Date.valueOf(LocalDate.of(2026, 6, 9)).toString());
        cmd.setString(3, Difficulty.EASY.toString());
        cmd.setString(4, "amore");
        cmd.setInt(5, 6);
        // Aggiungi query al batch
        cmd.addBatch();      

        cmd.setInt(1, 2);
        //cmd.setDate(2, Date.valueOf(LocalDate.of(2026, 6, 9)));               // non funziona per SQLite
        cmd.setString(2, Date.valueOf(LocalDate.of(2026, 6, 9)).toString());
        cmd.setString(3, Difficulty.HARD.toString());
        cmd.setString(4, "amicizia");
        cmd.setInt(5, 6);
        // Aggiungi query al batch
        cmd.addBatch();
        
        cmd.setInt(1, 3);
        //cmd.setDate(2, Date.valueOf(LocalDate.of(2026, 6, 9)));               // non funziona per SQLite
        cmd.setString(2, Date.valueOf(LocalDate.of(2026, 6, 9)).toString());
        cmd.setString(3, Difficulty.NORMAL.toString());
        cmd.setString(4, "tramonto");
        cmd.setInt(5, 9);
        // Aggiungi query al batch
        cmd.addBatch();
        
        cmd.setInt(1, 4);
        //cmd.setDate(2, Date.valueOf(LocalDate.of(2026, 6, 9)));               // non funziona per SQLite
        cmd.setString(2, Date.valueOf(LocalDate.of(2026, 6, 9)).toString());
        cmd.setString(3, Difficulty.EASY.toString());
        cmd.setString(4, "farfalla");
        cmd.setInt(5, 9);
        // Aggiungi query al batch
        cmd.addBatch();
        
        cmd.setInt(1, 5);
        //cmd.setDate(2, Date.valueOf(LocalDate.of(2026, 6, 9)));               // non funziona per SQLite
        cmd.setString(2, Date.valueOf(LocalDate.of(2026, 6, 9)).toString());
        cmd.setString(3, Difficulty.HARD.toString());
        cmd.setString(4, "giardino");
        cmd.setInt(5, 9);
        // Aggiungi query al batch
        cmd.addBatch();
        
        // Esegui
        cmd.executeBatch();
    }
    
    /**
     * @brief Popola in modalità batch il dizionario di parole di debug.
     * @param[inout] cmd Lo statement preparato associato alla tabella word.
     * @throws SQLException In caso di anomalie nei parametri o nella scrittura del batch.
     */
    private void insertDebugWords(PreparedStatement cmd) throws SQLException {
        
        cmd.setString(1, "metallo");
        cmd.setInt(2, 30);
        cmd.setInt(3, 6);
        // Aggiungi query al batch
        cmd.addBatch();  
        
        cmd.setString(1, "biblioteca");
        cmd.setInt(2, 40);
        cmd.setInt(3, 7);
        // Aggiungi query al batch
        cmd.addBatch();  
        
        cmd.setString(1, "creatività");
        cmd.setInt(2, 20);
        cmd.setInt(3, 6);
        // Aggiungi query al batch
        cmd.addBatch();  
        
        cmd.setString(1, "farfalla");
        cmd.setInt(2, 15);
        cmd.setInt(3, 9);
        // Aggiungi query al batch
        cmd.addBatch();  
        
        cmd.setString(1, "tecnologia");
        cmd.setInt(2, 2);
        cmd.setInt(3, 7);
        // Aggiungi query al batch
        cmd.addBatch();  
        
        cmd.setString(1, "amicizia");
        cmd.setInt(2, 18);
        cmd.setInt(3, 6);
        // Aggiungi query al batch
        cmd.addBatch();  
        
        cmd.setString(1, "tramonto");
        cmd.setInt(2, 35);
        cmd.setInt(3, 9);
        // Aggiungi query al batch
        cmd.addBatch();  
        
        cmd.setString(1, "ricerca");
        cmd.setInt(2, 76);
        cmd.setInt(3, 6);
        // Aggiungi query al batch
        cmd.addBatch();  
        
        cmd.setString(1, "amore");
        cmd.setInt(2, 117);
        cmd.setInt(3, 6);
        // Aggiungi query al batch
        cmd.addBatch();  
        
        cmd.setString(1, "giardino");
        cmd.setInt(2, 11);
        cmd.setInt(3, 9);
        // Aggiungi query al batch
        cmd.addBatch();  

        // Esegui
        cmd.executeBatch();
    }
    
    /**
     * @brief Popola in modalità batch i file sorgente di debug.
     * @param[inout] cmd Lo statement preparato associato alla tabella source.
     * @throws SQLException In caso di anomalie nei parametri o nella scrittura del batch.
     */
    private void insertDebugSource(PreparedStatement cmd) throws SQLException {
        
        cmd.setInt(1, 6);
        cmd.setString(2, "/fake/path/LuLu.txt");
        // Aggiungi query al batch
        cmd.addBatch();
        
        cmd.setInt(1, 9);
        cmd.setString(2, "/fake/path/Euphy.word");
        // Aggiungi query al batch
        cmd.addBatch();        
        
        cmd.setInt(1, 7);
        cmd.setString(2, "/fake/path/Cornelia.txt");
        // Aggiungi query al batch
        cmd.addBatch();    
        
        // Esegui
        cmd.executeBatch();
    }
    
}
