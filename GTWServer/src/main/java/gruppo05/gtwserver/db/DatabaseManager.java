package gruppo05.gtwserver.db;

import gruppo05.gtwshared.utility.Result;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author francesco-vecchione
 */
public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:ServerDB";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    
    // Abilitare le foreign keys, che in SQLite sono disabilitati di default.
    public static final String ENABLE_FOREIGN_KEYS = "PRAGMA foreign_keys = ON;";    
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
    
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
                "totalGamesWon    INTEGER DEFAULT 0, " +
                "totalGamesPlayed INTEGER DEFAULT 0, " +
                "PRIMARY KEY(username) " +
                ");";
        String crtTblSource = 
                "CREATE TABLE IF NOT EXISTS source (" +
                "id                 INTEGER NOT NULL, " +
                "path               TEXT NOT NULL, " + 
                "PRIMARY KEY(id) " +
                ");";
        String crtTblWord = 
                "CREATE TABLE IF NOT EXISTS word (" +
                "token              TEXT NOT NULL, " + 
                "frequency          INTEGER DEFAULT 0, " +
                "source             INTEGER NOT NULL, " + 
                "PRIMARY KEY(token, source), " +
                "FOREIGN KEY(source)        REFERENCES source(id)" +
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
                "FOREIGN KEY(word, source)  REFERENCES word(token, source)"  +
                "                           ON DELETE RESTRICT " +
                ");";
        String crtTblGame = 
                "CREATE TABLE IF NOT EXISTS game (" +
                "player             TEXT NOT NULL, " +
                "challenge          INTEGER NOT NULL, " +
                "result             TEXT NOT NULL, " +
                "responseTime       INTEGER NOT NULL, " +
                "PRIMARY KEY(player, challenge), " +
                "FOREIGN KEY(player)        REFERENCES player(username)" +    
                "                           ON DELETE RESTRICT, " +
                "FOREIGN KEY(challenge)     REFERENCES challenge(code)" +
                "                           ON DELETE RESTRICT " +
                ");";
        
        String crtTgrPlayer = 
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
        
        try (Connection conn = getConnection();
                Statement cmd = conn.createStatement()) {
            try {
                // Tutto deve essere eseguito in una transazione
                conn.setAutoCommit(false);

                cmd.execute(ENABLE_FOREIGN_KEYS);
                cmd.execute(crtTblAdmin);
                cmd.execute(crtTblPlayer);
                cmd.execute(crtTblSource);
                cmd.execute(crtTblWord);
                cmd.execute(crtTblChallenge);
                cmd.execute(crtTblGame);
                cmd.execute(crtTgrPlayer);

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
}
