package gruppo05.gtwserver.db;

import gruppo05.gtwshared.utility.Difficulty;
import gruppo05.gtwshared.utility.Result;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author francesco-vecchione
 * @brief Classe di test per la verifica delle funzionalità di DatabaseManager.
 * 
 * Verifica la corretta creazione delle tabelle, delle viste, l'integrità dei vincoli 
 * e l'attivazione dei relativi trigger custom definiti per il database SQLite del server.
 */
public class DatabaseManagerTest {
    
    /** 
     * @brief Nome del file di database locale SQLite utilizzato per i test. 
     */
    private final static String DB_NAME = "ServerDB";
    
    /** 
     * @brief Riferimento alla classe di utility per il popolamento diretto del database. 
     */
    private final DebugDB ddb;
    
    /**
     * @brief Costruttore predefinito.
     * 
     * Inizializza l'istanza dell'utility DebugDB per il popolamento del database.
     */
    public DatabaseManagerTest() {
        ddb = new DebugDB();
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    /**
     * @brief Configura l'ambiente prima dell'esecuzione di ciascun metodo di test.
     * 
     * Ricrea la struttura delle tabelle e inserisce il dataset deterministico di debug 
     * bypassando i DAO per garantire uno stato noto del database.
     * 
     * @throws SQLException In caso di errore durante la scrittura o l'inizializzazione sul database.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        DatabaseManager.initDB();
        ddb.initDebugDBWithoutDAO();
    }
    
    /**
     * @brief Pulisce l'ambiente al termine di ciascun metodo di test.
     * 
     * Rimuove fisicamente il file del database locale per garantire il completo isolamento 
     * ed evitare l'interferenza di residui di dati tra i vari test consecutivi.
     */
    @AfterEach
    public void tearDown() {
        // Cancella il database alla fine di ogni operazione
        File db = new File(DB_NAME);
        if(db.exists()) db.delete();
    }

    /**
     * @brief Verifica l'ottenimento di una connessione valida verso il database.
     * 
     * Controlla che l'oggetto Connection non sia null e che risulti effettivamente aperto 
     * prima della chiusura del costrutto try-with-resources.
     * 
     * @throws SQLException In caso di anomalie nella connessione JDBC.
     */
    @Test
    public void testGetConnection() throws SQLException {
        try(Connection conn = DatabaseManager.getConnection()) {
            assertNotNull(conn, "La connessione è null");
            assertFalse(conn.isClosed(), "La connessione si è chiusa prima della fine del try-with-resources");
        }
    }    
    
    /**
     * @brief Verifica l'effettiva creazione delle tabelle di sistema all'avvio dell'applicazione.
     * 
     * Ispeziona i metadati del database tramite il driver JDBC per assicurarsi che tutte le tabelle 
     * principali ('admin', 'player', 'game', 'challenge', 'word', 'source') siano registrate correttamente.
     * 
     * @throws SQLException In caso di fallimento nell'interrogazione dei metadati SQL.
     */
    @Test
    public void testInitDBCreateTables() throws SQLException {
        
        try(Connection conn = DatabaseManager.getConnection()) {
            // Ottieni i metadati del db, ovvero le informazioni sugli oggetti presenti nel db
            DatabaseMetaData meta = conn.getMetaData();
            meta.getTables(null, null, "%", new String[]{"TABLE"});
            
            try(ResultSet res = meta.getTables(null, null, "admin", null)) {
                assertTrue(res.next(), "La tabella 'admin' non esiste");
            }
            try(ResultSet res = meta.getTables(null, null, "player", null)) {
                assertTrue(res.next(), "La tabella 'player' non esiste");
            }
            try(ResultSet res = meta.getTables(null, null, "game", null)) {
                assertTrue(res.next(), "La tabella 'game' non esiste");
            }
            try(ResultSet res = meta.getTables(null, null, "challenge", null)) {
                assertTrue(res.next(), "La tabella 'challenge' non esiste");
            }
            try(ResultSet res = meta.getTables(null, null, "word", null)) {
                assertTrue(res.next(), "La tabella 'word' non esiste");
            }
            try(ResultSet res = meta.getTables(null, null, "source", null)) {
                assertTrue(res.next(), "La tabella 'source' non esiste");
            }
        }
    }
    
    /**
     * @brief Verifica l'effettiva creazione delle viste all'interno del database.
     * 
     * Ispeziona i metadati SQL per accertare che la vista 'availableWords' sia configurata 
     * e rilevabile nel sistema come oggetto di tipo VIEW.
     * 
     * @throws SQLException In caso di errore durante la lettura dei metadati del database.
     */
    @Test
    public void testInitDBCreateViews() throws SQLException {
        
        try(Connection conn = DatabaseManager.getConnection()) {
            // Ottieni i metadati del db, ovvero le informazioni sugli oggetti presenti nel db
            DatabaseMetaData meta = conn.getMetaData();
            meta.getTables(null, null, "%", new String[]{"VIEW"});
            
            try(ResultSet res = meta.getTables(null, null, "availableWords", null)) {
                assertTrue(res.next(), "La view 'availableWords' non esiste");
            }
        }
    }
    
    /**
     * @brief Test del trigger 'incrementTotalsInPlayer' per l'aggiornamento incrementale delle statistiche.
     * 
     * Inserisce una nuova sfida e simula il completamento di due partite da parte di giocatori differenti 
     * (FrancoNeri e AlexGiallo). Successivamente rilegge i dati aggregati dei giocatori per verificare che 
     * il trigger di INSERT sulla tabella 'game' abbia correttamente ricalcolato i totali di tempo giocato, 
     * partite giocate e vinte a partire dai rispettivi valori preesistenti.
     * 
     * @throws SQLException In caso di fallimento nelle operazioni SQL.
     * @throws Exception Se la query di controllo restituisce un record imprevisto.
     */
    @Test
    public void testInitDBTriggerOnGame() throws SQLException, Exception {
        
        // Prova aggiunta
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmdChallenge = conn.prepareStatement(DebugDB.INSERT_CHALLENGE);
                PreparedStatement cmdGame = conn.prepareStatement(DebugDB.INSERT_GAME);
                Statement st = conn.createStatement()) {

            // Il comando di abilitazione dei vincoli di integrità referenziale
            // deve essere abilitato fuori dalla transazione
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            
            conn.setAutoCommit(false);

            // Inserimento challenge
            cmdChallenge.setInt(1, 100);
            cmdChallenge.setDate(2, Date.valueOf(LocalDate.of(2026, 6, 9)));
            cmdChallenge.setString(3, Difficulty.EASY.toString());
            cmdChallenge.setString(4, "amore");
            cmdChallenge.setInt(5, 6);   // source già presente

            cmdChallenge.executeUpdate();

            // Partita di FrancoNeri (vittoria)
            cmdGame.setString(1, "FrancoNeri");
            cmdGame.setInt(2, 100);
            cmdGame.setString(3, Result.WIN.toString());
            cmdGame.setInt(4, 18);

            cmdGame.executeUpdate();

            // Partita di AlexGiallo (sconfitta)
            cmdGame.setString(1, "AlexGiallo");
            cmdGame.setInt(2, 100);
            cmdGame.setString(3, Result.LOSE.toString());
            cmdGame.setInt(4, 25);

            cmdGame.executeUpdate();

            conn.commit();
        }
        
        /*
            Stato attuale delle ridondanze per ciascun giocatore all'atto dell'inserimento di questi dati:
                                totalPlayedTime         totalGamesWon       totalGamesPlayed
            FrancoNeri:         18                      1                   1
            AlexGiallo:         80                      2                   4
            ...
        */
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(
                        "SELECT username, totalPlayedTime, totalGamesWon, totalGamesPlayed FROM player WHERE username = ? OR username = ?;")) {
            cmd.setString(1, "FrancoNeri");
            cmd.setString(2, "AlexGiallo");
            
            try (ResultSet rs = cmd.executeQuery()) {
                while(rs.next()) {
                    if(rs.getString("username").equalsIgnoreCase("FrancoNeri")) {
                        assertEquals(rs.getInt("totalPlayedTime"), 18);
                        assertEquals(rs.getInt("totalGamesWon"), 1);
                        assertEquals(rs.getInt("totalGamesPlayed"), 1);
                    } else if(rs.getString("username").equalsIgnoreCase("AlexGiallo")) {
                        assertEquals(rs.getInt("totalPlayedTime"), 80);
                        assertEquals(rs.getInt("totalGamesWon"), 2);
                        assertEquals(rs.getInt("totalGamesPlayed"), 4);
                    } else {
                        throw new Exception("La query ha ritornato un username non esplicitamente indicato");
                    }
                }
            }
        } 
    }
    
    /**
     * @brief Test del comportamento standard dei vincoli referenziali su una sorgente non utilizzata.
     * 
     * Prova ad eliminare la sorgente con id = 7 (che nel dataset iniziale non è referenziata da alcuna sfida). 
     * Trattandosi di un record libero da legami attivi con la tabella 'challenge', il trigger 'deleteOnlyUnreferencedWords' 
     * non intercetta la query a monte, permettendo la rimozione del record da 'source' e, di riflesso, 
     * l'eliminazione a cascata ('ON DELETE CASCADE') di tutte le parole ad essa associate nella tabella 'word'.
     * 
     * @throws SQLException In caso di errore durante l'esecuzione del comando SQL.
     */
    @Test
    public void testInitDBTriggerOnSourceNotUsed() throws SQLException {
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(
                        "DELETE FROM source WHERE id = ?");
                Statement st = conn.createStatement()) {
            cmd.setInt(1, 7);
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            cmd.executeUpdate();
        } 
        
        try (Connection conn = DatabaseManager.getConnection();
                Statement st = conn.createStatement();
                Statement stSource = conn.createStatement();
                Statement stWord = conn.createStatement()) { 
            
            st.execute(DatabaseManager.ENABLE_FOREIGN_KEYS);
            try (ResultSet rsSource = stSource.executeQuery("SELECT * FROM source;");
                    ResultSet rsWord = stWord.executeQuery("SELECT * FROM word;")) {
                while(rsSource.next()) {
                    assertNotEquals(rsSource.getInt("id"), 7);
                }
                while(rsWord.next()) {
                    assertNotEquals(rsWord.getInt("source"), 7);
                }
            }
        }
    }
    
    /**
     * @brief Test del trigger 'deleteOnlyUnreferencedWords' su una sorgente utilizzata in delle sfide.
     * 
     * Esegue una richiesta di rimozione per la sorgente con id = 6, la quale è legata a sfide attive 
     * (tramite le parole 'amicizia' e 'amore'). Il test certifica che l'attivazione del trigger BEFORE DELETE 
     * impedisce la cancellazione del record principale da 'source' (mediante un comando RAISE(IGNORE)), impostando 
     * al contempo il campo 'path' a NULL, e pulendo dalla tabella 'word' solo le parole appartenenti alla medesima sorgente 
     * che non figurano in alcuna sfida attiva ('metallo', 'creatività', 'farfalla', 'ricerca').
     * 
     * @throws SQLException In caso di anomalie nella transazione o nell'esecuzione SQL.
     */
    @Test
    public void testInitDBTriggerOnSourceUsed() throws SQLException {
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(
                        "DELETE FROM source WHERE id = ?")) {
            cmd.setInt(1, 6);
            cmd.executeUpdate();
        } 
        
        /*
            Nel db ci sono le seguenti parole legate alla sorgente con id = 6:
                metallo,    creatività,     farfalla,   amicizia,   ricerca,    amore
            Di cui le seguenti hanno almeno un'istanza di sfida legata ad esse:
                                                        amicizia,               amore
        
            Per cui le seguenti parole devono essere eliminate:
                metallo,    creatività,     farfalla,               ricerca
        
            Mentre il path della sorgente con id = 6 deve essere messo a null
        */
        
        try (Connection conn = DatabaseManager.getConnection();
                Statement cmdSource = conn.createStatement();
                Statement cmdWord = conn.createStatement();
                ResultSet rsSource = cmdSource.executeQuery("SELECT path FROM source WHERE id = 6;");
                ResultSet rsWord = cmdWord.executeQuery("SELECT token FROM word WHERE source = 6;")) { 
            
            while(rsSource.next()) {
                assertNull(rsSource.getString("path"), "Il path della sorgente con id = 6 non era null");
            }
            while(rsWord.next()) {
                assertNotEquals(rsWord.getString("token"), "metallo");
                assertNotEquals(rsWord.getString("token"), "creatività");
                assertNotEquals(rsWord.getString("token"), "farfalla");
                assertNotEquals(rsWord.getString("token"), "ricerca");
            }
        }        
    }
}
