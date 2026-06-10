package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Source;
import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
public class ConcreteSourceDAOTest {
    
    private final static String DB_NAME = "ServerDB";
    private final DebugDB ddb;
    private final SourceDAO sdao;
    
    public ConcreteSourceDAOTest() {
        ddb = new DebugDB();
        sdao = new ConcreteSourceDAO();
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        DatabaseManager.initDB();
        ddb.initDebugDBWithoutDAO();
    }
    
    @AfterEach
    public void tearDown() {
        // Cancella il database alla fine di ogni operazione
        File db = new File(DB_NAME);
        if(db.exists()) db.delete();
    }
    
    // test per il metodo selectById

    @Test
    public void testSelectByIdExisting() {
        // Presuppone l'esistenza della sorgente valida con ID 6 nel database di debug
        Optional<Integer> id = Optional.of(6);
        Optional<Source> result = sdao.selectById(id);
        
        assertTrue(result.isPresent(), "La sorgente con ID 6 dovrebbe essere presente.");
        assertEquals(6, result.get().getId());
        assertNotNull(result.get().getPath(), "Il path associato alla sorgente non deve essere null.");
    }

    @Test
    public void testSelectByIdNotExisting() {
        Optional<Integer> id = Optional.of(999);
        Optional<Source> result = sdao.selectById(id);
        
        assertFalse(result.isPresent(), "La sorgente con ID inesistente non deve essere trovata.");
    }

    @Test
    public void testSelectByIdEmptyOptional() {
        Optional<Source> result = sdao.selectById(Optional.empty());
        
        assertFalse(result.isPresent(), "L'invio di un Optional vuoto deve ritornare un Optional vuoto.");
    }

    @Test
    public void testSelectByIdWithNullPathInDB() {
        // Forza l'inserimento nel DB di un record sporco con PATH NULL saltando il DAO per testare la nuova clausola WHERE
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("INSERT INTO source (id, path) VALUES (99, NULL);");
        } catch (SQLException e) {
            fail("Impossibile preparare il record di test con PATH NULL: " + e.getMessage());
        }

        // La nuova clausola "AND path IS NOT NULL" deve bloccare il recupero di questo record
        Optional<Source> result = sdao.selectById(Optional.of(99));
        assertFalse(result.isPresent(), "Il metodo selectById deve ignorare i record il cui path è NULL.");
    }

    // test per il metodo selectAll

    @Test
    public void testSelectAll() {
        List<Source> result = sdao.selectAll();
        
        assertNotNull(result, "La lista restituita non deve essere null.");
        assertFalse(result.isEmpty(), "La lista delle sorgenti valide non dovrebbe essere vuota.");
    }

    @Test
    public void testSelectAllFiltersOutNullPaths() {
        int sizeBefore = sdao.selectAll().size();

        // Forza l'inserimento nel DB di un record con PATH NULL saltando il DAO
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("INSERT INTO source (id, path) VALUES (88, NULL);");
        } catch (SQLException e) {
            fail("Impossibile preparare il record di test con PATH NULL: " + e.getMessage());
        }

        int sizeAfter = sdao.selectAll().size();
        // Grazie alla correzione della condizione "WHERE path IS NOT NULL", il record orfano viene correttamente escluso
        assertEquals(sizeBefore, sizeAfter, "Il metodo selectAll deve escludere i record che hanno un path NULL.");
    }

    // test per il metodo insert

    @Test
    public void testInsertValid() {
        Source newSource = new Source(10, Paths.get("gamedata/sources/source10.txt"));
        sdao.insert(newSource);
        
        Optional<Source> retrieved = sdao.selectById(Optional.of(10));
        assertTrue(retrieved.isPresent(), "La nuova sorgente deve essere presente nel DB.");
        assertEquals(Paths.get("gamedata/sources/source10.txt"), retrieved.get().getPath());
    }

    @Test
    public void testInsertNull() {
        int sizeBefore = sdao.selectAll().size();
        sdao.insert(null);
        int sizeAfter = sdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un oggetto null non deve alterare lo stato del DB.");
    }

    @Test
    public void testInsertDuplicateId() {
        Source duplicateSource = new Source(6, Paths.get("gamedata/sources/duplicate.txt"));
        
        int sizeBefore = sdao.selectAll().size();
        sdao.insert(duplicateSource);
        int sizeAfter = sdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un ID duplicato deve fallire senza aggiungere record.");
    }

    // test per il metodo insertAll

    @Test
    public void testInsertAllValid() {
        List<Source> list = new ArrayList<>();
        list.add(new Source(20, Paths.get("gamedata/sources/source20.txt")));
        list.add(new Source(21, Paths.get("gamedata/sources/source21.txt")));
        
        int sizeBefore = sdao.selectAll().size();
        sdao.insertAll(list);
        int sizeAfter = sdao.selectAll().size();
        
        assertEquals(sizeBefore + 2, sizeAfter, "Il database deve contenere le 2 nuove sorgenti inserite.");
        assertTrue(sdao.selectById(Optional.of(20)).isPresent());
        assertTrue(sdao.selectById(Optional.of(21)).isPresent());
    }

    @Test
    public void testInsertAllNullOrEmpty() {
        int sizeBefore = sdao.selectAll().size();
        
        sdao.insertAll(null);
        assertEquals(sizeBefore, sdao.selectAll().size());
        
        sdao.insertAll(new ArrayList<>());
        assertEquals(sizeBefore, sdao.selectAll().size());
    }

    @Test
    public void testInsertAllWithRollback() {
        int sizeBefore = sdao.selectAll().size();
        List<Source> list = new ArrayList<>();
        list.add(new Source(30, Paths.get("gamedata/sources/source30.txt")));
        // Il secondo elemento fallisce per violazione di chiave primaria (ID 6 già esistente)
        list.add(new Source(6, Paths.get("gamedata/sources/source6_fail.txt")));
        
        sdao.insertAll(list);
        
        int sizeAfter = sdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "La transazione deve eseguire il rollback: nessuna sorgente deve essere aggiunta.");
        assertFalse(sdao.selectById(Optional.of(30)).isPresent(), "La sorgente transitoria non deve essere stata salvata.");
    }

    // test per il metodo update

    @Test
    public void testUpdateExisting() {
        Optional<Integer> id = Optional.of(6);
        Optional<Source> original = sdao.selectById(id);
        assertTrue(original.isPresent());
        
        Source updatedSource = new Source(6, Paths.get("gamedata/sources/new_path_2026.txt"));
        sdao.update(updatedSource);
        
        Optional<Source> retrieved = sdao.selectById(id);
        assertTrue(retrieved.isPresent());
        assertEquals(Paths.get("gamedata/sources/new_path_2026.txt"), retrieved.get().getPath(), "Il percorso (path) deve risultare aggiornato.");
    }

    @Test
    public void testUpdateNull() {
        assertDoesNotThrow(() -> sdao.update(null), "L'aggiornamento di un oggetto null non deve sollevare eccezioni.");
    }

    // test per il metodo delete

    @Test
    public void testDeleteNoReferences() {
        Source tempSource = new Source(40, Paths.get("gamedata/sources/temp.txt"));
        sdao.insert(tempSource);
        
        int sizeAfterInsert = sdao.selectAll().size();
        sdao.delete(Optional.of(40));
        int sizeAfterDelete = sdao.selectAll().size();
        
        assertEquals(sizeAfterInsert - 1, sizeAfterDelete, "La sorgente isolata deve essere eliminata correttamente.");
        assertFalse(sdao.selectById(Optional.of(40)).isPresent());
    }

    @Test
    public void testDeleteWithReferences() throws SQLException {
        int sizeBefore = sdao.selectAll().size();
        
        // La sorgente 6 è legata a vincoli di chiave esterna attivi (es. tabelle word/challenge)
        sdao.delete(Optional.of(6));
        
        int sizeAfter = sdao.selectAll().size();
        
        // La sorgente deve essere comunque "eliminata" impostando il path a null
        // per constatare che sia presente nel db però non possiamo affidarci
        // al metodo selectAll() in quanto esso esclude di base le fonti "eliminate"
        // ovvero quelle che hanno il path a null.
        
        // Controllo che la fonte sia stata "eliminata" logicamente
        assertEquals(sizeBefore-1, sizeAfter, "La sorgente dovrebbe essere stata eliminata logicamente dal db nonostante i vincoli di integrità referenziale");

        // Controllo se è effettivamente la sorgente è ancora presente ed ha il
        // suo path impostato a null
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement cmd = conn.prepareStatement(
                        "SELECT * FROM source WHERE id = ?")) {
            cmd.setInt(1, 6);
            
            try (ResultSet rs = cmd.executeQuery()) {
                while(rs.next()) {
                    assertEquals(6, rs.getInt("id"));
                    assertNull(rs.getString("path"));
                }
            }
        }
    }

    @Test
    public void testDeleteEmptyOptional() {
        int sizeBefore = sdao.selectAll().size();
        sdao.delete(Optional.empty());
        int sizeAfter = sdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'invio di un Optional vuoto non deve modificare lo stato del database.");
    }
}
