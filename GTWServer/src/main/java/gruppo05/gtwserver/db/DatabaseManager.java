package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Admin;
import gruppo05.gtwserver.model.Player;
import gruppo05.gtwshared.utility.Result;
import gruppo05.gtwshared.utility.SecurityUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * 
 * @brief Gestore centralizzato del database SQLite per il server.
 * @invariant
 * Le costanti URL, USERNAME e PASSWORD definiscono i parametri immutabili
 * di connessione al database locale.
 */
public class DatabaseManager {
    
    /**
     * @brief Stringa di connessione JDBC per il database locale SQLite denominato 'ServerDB'.
     */
    private static final String URL = "jdbc:sqlite:ServerDB";
    
    /**
     * @brief Username predefinito per l'autenticazione al database.
     */
    private static final String USERNAME = "root";
    /**
     * @brief Password predefinita per l'autenticazione al database.
     */
    private static final String PASSWORD = "root";
    
    /**
     * @brief Comando SQL per abilitare forzatamente i vincoli d'integrità referenziale (Foreign Keys).
     */
    public static final String ENABLE_FOREIGN_KEYS = "PRAGMA foreign_keys = ON;";    
    
    /**
     * @brief Crea e restituisce una connessione attiva verso il database SQLite.
     * @return L'oggetto Connection istanziato.
     * @post
     * La connessione restituita non è null ed è pronta per l'esecuzione di comandi SQL.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
    
    /**
     * @brief Inizializza la struttura del database creando tabelle e trigger se non esistono.
     * @post
     * Tutte le tabelle di sistema (admin, player, source, word, challenge, game)
     * e il relativo trigger di aggiornamento delle statistiche dei giocatori
     * sono correttamente configurati sul database all'interno di una transazione atomica.
     */
    public static void initDB() {    
        
        String crtTblAdmin = 
                "CREATE TABLE IF NOT EXISTS admin (" +
                "username           TEXT NOT NULL, " +
                "password           TEXT NOT NULL, " +
                "PRIMARY KEY(username) " +
                ");";
        
        String crtTblPlayer = 
                "CREATE TABLE IF NOT EXISTS player (" +
                "username           TEXT NOT NULL, " +
                "password           TEXT NOT NULL, " +
                "totalPlayedTime    INTEGER DEFAULT 0, " + 
                "totalGamesWon      INTEGER DEFAULT 0, " +
                "totalGamesPlayed   INTEGER DEFAULT 0, " +
                "PRIMARY KEY(username) " +
                ");";
        
        String crtTblSource = 
                "CREATE TABLE IF NOT EXISTS source (" +
                "id                 INTEGER NOT NULL, " +
                "path               TEXT, " + 
                "PRIMARY KEY(id) " +
                ");";
        
        String crtTblWord = 
                "CREATE TABLE IF NOT EXISTS word (" +
                "token              TEXT NOT NULL, " + 
                "frequency          INTEGER DEFAULT 0, " +
                "source             INTEGER NOT NULL, " + 
                "PRIMARY KEY(token, source), " +
                "FOREIGN KEY(source)        REFERENCES source(id) " +
                "                           ON DELETE CASCADE " +
                ");";
        
        String crtTblChallenge = 
                "CREATE TABLE IF NOT EXISTS challenge (" +
                "code               INTEGER NOT NULL, " + 
                "date               TEXT NOT NULL, " + 
                "difficulty         TEXT NOT NULL, " + 
                "word               TEXT NOT NULL, " +
                "source             INTEGER NOT NULL, " +
                "PRIMARY KEY(code), " +
                "FOREIGN KEY(word, source)  REFERENCES word(token, source) "  +
                "                           ON DELETE RESTRICT " +
                ");";
        
        String crtTblGame = 
                "CREATE TABLE IF NOT EXISTS game (" +
                "player             TEXT NOT NULL, " +
                "challenge          INTEGER NOT NULL, " +
                "result             TEXT NOT NULL, " +
                "responseTime       INTEGER NOT NULL, " +
                "PRIMARY KEY(player, challenge), " +
                "FOREIGN KEY(player)        REFERENCES player(username) " +    
                "                           ON DELETE RESTRICT, " +
                "FOREIGN KEY(challenge)     REFERENCES challenge(code) " +
                "                           ON DELETE RESTRICT " +
                ");";
        
        // Trigger per la gestione delle ridondanze di player
        String crtTgrGame = 
                "CREATE TRIGGER IF NOT EXISTS incrementTotalsInPlayer " +
                "AFTER INSERT ON game " +
                "FOR EACH ROW " +
                "BEGIN " +
                "   UPDATE player " +
                "   SET     totalPlayedTime = totalPlayedTime + NEW.responseTime, " +
                "           totalGamesWon = totalGamesWon + CASE " +
                "                               WHEN NEW.result = '" + Result.WIN.toString() + "' THEN 1 " +
                "                               ELSE 0 " +
                "                               END, " + 
                "           totalGamesPlayed = totalGamesPlayed + 1 " +
                "   WHERE username = NEW.player; " +
                "END;";
        
        String crtVwWord = 
                "CREATE VIEW IF NOT EXISTS availableWords AS " +
                "       SELECT word.* " +
                "       FROM word " +
                "           JOIN source ON source.id = word.source " +
                "       WHERE source.path IS NOT NULL;";
        
        // In SQLite INSTEAD OF funziona solo per le view, quindi sono
        // obbligato ad utilizzare BEFORE o AFTER. Poiché non voglio che
        // vengano sollevate eccezioni, utilizzo BEFORE.
        // Questo trigger si attiva solo quando ci sono sfide che referenziano 
        // al documento che si vuole cancellare. Ciò che faccio è:
        //  -   Setto a null il path
        //  -   Cancello le parole della sorgente che voglio cancellare che 
        //      non sono legate a nessuna sfida 
        //  -   Blocco la cancellazione effettiva da source
        String crtTgrSource =
                "CREATE TRIGGER IF NOT EXISTS deleteOnlyUnreferencedWords " +
                "BEFORE DELETE ON source " +
                "FOR EACH ROW " +
                "WHEN EXISTS(   SELECT * " +
                "               FROM challenge " +
                "               WHERE source = OLD.id ) " +
                "BEGIN " +
                "   UPDATE source " +
                "   SET path = NULL " +
                "   WHERE id = OLD.id; " +
                "   DELETE FROM word " +
                "   WHERE source = OLD.id " +
                "       AND (token, source) NOT IN ( SELECT word, source " +
                "                                       FROM challenge ); " +
                "   SELECT RAISE(IGNORE); " +
                "END;";
                
        
        try (Connection conn = getConnection();
                Statement cmd = conn.createStatement()) {
            try {
                // Il comando di abilitazione dei vincoli di integrità referenziale
                // deve essere abilitato fuori dalla transazione
                cmd.execute(ENABLE_FOREIGN_KEYS);
                
                // Tutto deve essere eseguito in una transazione
                conn.setAutoCommit(false);

                cmd.execute(crtTblAdmin);
                cmd.execute(crtTblPlayer);
                cmd.execute(crtTblSource);
                cmd.execute(crtTblWord);
                cmd.execute(crtTblChallenge);
                cmd.execute(crtTblGame);
                cmd.execute(crtTgrGame);
                cmd.execute(crtVwWord);
                cmd.execute(crtTgrSource);

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
            // Debug: da cambiare
            ex.printStackTrace();            
        }
    }
    
    
    public static void initDebugUsersDB() {
        AdminDAO adao = new ConcreteAdminDAO();
        PlayerDAO pdao = new ConcretePlayerDAO();
        
        adao.insert(new Admin("s1", SecurityUtils.hashPassword("s1")));
        pdao.insert(new Player("p1", SecurityUtils.hashPassword("p1"), 0, 0, 0));
        pdao.insert(new Player("p2", SecurityUtils.hashPassword("p2"), 0, 0, 0));
    }
}
