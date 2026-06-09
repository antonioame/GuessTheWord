/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author francesco-vecchione
 */
public class DatabaseManagerTest {
    
    private final static String DB_NAME = "ServerDB";
    
    public DatabaseManagerTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
        // Cancella il database alla fine di ogni operazione
        File db = new File(DB_NAME);
        if(db.exists()) db.delete();
    }

    @Test
    public void testGetConnection() throws SQLException {
        try(Connection conn = DatabaseManager.getConnection()) {
            assertNotNull(conn, "La connessione è null");
            assertFalse(conn.isClosed(), "La connessione si è chiusa prima della fine del try-with-resources");
        }
    }    
    
    @Test
    public void testInitDBCreateTables() throws SQLException {
        DatabaseManager.initDB();
        
        try(Connection conn = DatabaseManager.getConnection()) {
            // Ottieni i metadati del db, ovvero le informazioni sugli oggetti presenti nel db
            DatabaseMetaData meta = conn.getMetaData();
            
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
    
    @Test
    public void testInitDBTrigger() throws SQLException {
        
    }
}
