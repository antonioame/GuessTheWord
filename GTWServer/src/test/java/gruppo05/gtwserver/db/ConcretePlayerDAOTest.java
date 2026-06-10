package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Player;
import java.io.File;
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
public class ConcretePlayerDAOTest {
    
    private final static String DB_NAME = "ServerDB";
    private final DebugDB ddb;
    private final PlayerDAO pdao;
    
    public ConcretePlayerDAOTest() {
        ddb = new DebugDB();
        pdao = new ConcretePlayerDAO();
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
        Optional<String> username = Optional.of("RobertoViola");
        Optional<Player> result = pdao.selectById(username);
        
        assertTrue(result.isPresent(), "Il giocatore dovrebbe essere presente nel DB.");
        assertEquals("RobertoViola", result.get().getUsername());
        // Verifica che i contatori/statistiche vengano mappati correttamente
        assertTrue(result.get().getTotalGamesPlayed() >= 0);
        assertTrue(result.get().getTotalGamesWon() >= 0);
        assertTrue(result.get().getTotalPlayedTime() >= 0);
    }

    @Test
    public void testSelectByIdNotExisting() {
        Optional<String> username = Optional.of("UtenteInesistente");
        Optional<Player> result = pdao.selectById(username);
        
        assertFalse(result.isPresent(), "Il giocatore non dovrebbe essere trovato.");
    }

    @Test
    public void testSelectByIdEmptyOptional() {
        Optional<Player> result = pdao.selectById(Optional.empty());
        
        assertFalse(result.isPresent(), "Il passaggio di un Optional vuoto deve restituire un Optional vuoto.");
    }

    // test per il metodo selectAll

    @Test
    public void testSelectAll() {
        List<Player> result = pdao.selectAll();
        
        assertNotNull(result, "La lista restituita non deve essere null.");
        assertFalse(result.isEmpty(), "La lista dei giocatori non dovrebbe essere vuota.");
        
        // Verifica la presenza di almeno uno dei player del DebugDB
        boolean containsRoberto = result.stream().anyMatch(p -> p.getUsername().equals("RobertoViola"));
        assertTrue(containsRoberto, "La lista dovrebbe contenere RobertoViola.");
    }

    // test per il metodo insert

    @Test
    public void testInsertValid() {
        Player newPlayer = new Player("LuigiNeri", "securePwd987", 0, 0, 0);
        pdao.insert(newPlayer);
        
        Optional<Player> retrieved = pdao.selectById(Optional.of("LuigiNeri"));
        assertTrue(retrieved.isPresent(), "Il nuovo giocatore deve essere recuperabile dal DB.");
        assertEquals("securePwd987", retrieved.get().getPassword());
        
        // Verifica che le ridondanze siano effettivamente a 0 di default nel DB
        assertEquals(0, retrieved.get().getTotalPlayedTime());
        assertEquals(0, retrieved.get().getTotalGamesWon());
        assertEquals(0, retrieved.get().getTotalGamesPlayed());
    }

    @Test
    public void testInsertNull() {
        int sizeBefore = pdao.selectAll().size();
        pdao.insert(null);
        int sizeAfter = pdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un oggetto null non deve alterare il DB.");
    }

    @Test
    public void testInsertDuplicateUsername() {
        int sizeBefore = pdao.selectAll().size();
        
        // Tentativo di inserire un giocatore con uno username già esistente (es. RobertoViola)
        Player duplicatePlayer = new Player("RobertoViola", "nuovaPassword", 0, 0, 0);
        pdao.insert(duplicatePlayer);
        
        int sizeAfter = pdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un duplicato chiave deve fallire mantenendo invariato il DB.");
    }

    // test per il metodo insertAll

    @Test
    public void testInsertAllValid() {
        List<Player> playersToAdd = new ArrayList<>();
        playersToAdd.add(new Player("AnnaGialli", "pwdAnna", 0, 0, 0));
        playersToAdd.add(new Player("PedroAzzurri", "pwdPedro", 0, 0, 0));
        
        int sizeBefore = pdao.selectAll().size();
        pdao.insertAll(playersToAdd);
        int sizeAfter = pdao.selectAll().size();
        
        assertEquals(sizeBefore + 2, sizeAfter, "Il numero di giocatori nel DB deve essere aumentato di 2.");
        assertTrue(pdao.selectById(Optional.of("AnnaGialli")).isPresent());
        assertTrue(pdao.selectById(Optional.of("PedroAzzurri")).isPresent());
    }

    @Test
    public void testInsertAllNullOrEmpty() {
        int sizeBefore = pdao.selectAll().size();
        
        pdao.insertAll(null);
        assertEquals(sizeBefore, pdao.selectAll().size(), "Una lista null non deve modificare il DB.");
        
        pdao.insertAll(new ArrayList<>());
        assertEquals(sizeBefore, pdao.selectAll().size(), "Una lista vuota non deve modificare il DB.");
    }

    @Test
    public void testInsertAllWithRollback() {
        int sizeBefore = pdao.selectAll().size();
        List<Player> playersToAdd = new ArrayList<>();
        playersToAdd.add(new Player("SoniaGrigi", "pwdSonia", 0, 0, 0));
        // Il secondo elemento fallirà perché "RobertoViola" è già presente nel DB di debug (violazione del vincolo UNIQUE/PK)
        playersToAdd.add(new Player("RobertoViola", "pwdRoberto", 0, 0, 0));
        
        pdao.insertAll(playersToAdd);
        
        int sizeAfter = pdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "La transazione deve eseguire il rollback: nessun giocatore deve essere inserito.");
        assertFalse(pdao.selectById(Optional.of("SoniaGrigi")).isPresent(), "SoniaGrigi non deve essere stata salvata.");
    }

    // test per il metodo update

    @Test
    public void testUpdateExisting() {
        Optional<String> username = Optional.of("RobertoViola");
        Optional<Player> original = pdao.selectById(username);
        assertTrue(original.isPresent());
        
        // Modifica della password del giocatore
        Player playerToUpdate = new Player("RobertoViola", "cambiataPassword2026", 0, 0, 0);
        pdao.update(playerToUpdate);
        
        Optional<Player> retrieved = pdao.selectById(username);
        assertTrue(retrieved.isPresent());
        assertEquals("cambiataPassword2026", retrieved.get().getPassword(), "La password deve risultare aggiornata.");
    }

    @Test
    public void testUpdateNull() {
        assertDoesNotThrow(() -> pdao.update(null), "L'aggiornamento di un oggetto null non deve sollevare eccezioni.");
    }

    // test per il metodo delete

    @Test
    public void testDeleteNoReferences() {
        // Inseriamo prima un utente temporaneo senza partite associate
        Player tempPlayer = new Player("UserFinto", "pwdFinta", 0, 0, 0);
        pdao.insert(tempPlayer);
        
        int sizeAfterInsert = pdao.selectAll().size();
        
        pdao.delete(Optional.of("UserFinto"));
        
        int sizeAfterDelete = pdao.selectAll().size();
        assertEquals(sizeAfterInsert - 1, sizeAfterDelete, "Il giocatore senza vincoli attivi deve essere eliminato.");
        assertFalse(pdao.selectById(Optional.of("UserFinto")).isPresent());
    }

    @Test
    public void testDeleteWithReferences() {
        int sizeBefore = pdao.selectAll().size();
        
        // RobertoViola ha delle partite registrate nella tabella 'game'
        // Il vincolo d'integrità referenziale (ON DELETE RESTRICT) impedirà la cancellazione
        pdao.delete(Optional.of("RobertoViola"));
        
        int sizeAfter = pdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "Il database non deve permettere la cancellazione a causa dei vincoli FK della tabella game.");
        assertTrue(pdao.selectById(Optional.of("RobertoViola")).isPresent(), "Il giocatore referenziato deve essere ancora presente.");
    }

    @Test
    public void testDeleteEmptyOptional() {
        int sizeBefore = pdao.selectAll().size();
        pdao.delete(Optional.empty());
        int sizeAfter = pdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'eliminazione tramite un Optional vuoto non deve apportare modifiche.");
    }
}
