package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Admin;
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
public class ConcreteAdminDAOTest {
    
    private final static String DB_NAME = "ServerDB";
    private final DebugDB ddb;
    private final AdminDAO adao;
    
    public ConcreteAdminDAOTest() {
        ddb = new DebugDB();
        adao = new ConcreteAdminDAO();
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
        Optional<String> username = Optional.of("MarioRossi");
        Optional<Admin> result = adao.selectById(username);
        
        assertTrue(result.isPresent(), "L'admin dovrebbe essere presente nel DB.");
        assertEquals("MarioRossi", result.get().getUsername());
        assertEquals("nunciafac@", result.get().getPassword());
    }

    @Test
    public void testSelectByIdNotExisting() {
        Optional<String> username = Optional.of("AdminInesistente");
        Optional<Admin> result = adao.selectById(username);
        
        assertFalse(result.isPresent(), "L'admin non dovrebbe essere trovato.");
    }

    @Test
    public void testSelectByIdEmptyOptional() {
        Optional<Admin> result = adao.selectById(Optional.empty());
        
        assertFalse(result.isPresent(), "Il passaggio di un Optional vuoto deve restituire un Optional vuoto.");
    }

    // test per il metodo selectAll
    
    @Test
    public void testSelectAll() {
        List<Admin> result = adao.selectAll();
        
        assertNotNull(result, "La lista restituita non deve essere null.");
        assertEquals(2, result.size(), "Il DB di debug contiene esattamente 2 amministratori.");
        
        // Verifica la presenza dei dati inseriti nel batch di DebugDB
        boolean containsMario = result.stream().anyMatch(a -> a.getUsername().equals("MarioRossi"));
        boolean containsGiuseppe = result.stream().anyMatch(a -> a.getUsername().equals("GiuseppeVerdi"));
        
        assertTrue(containsMario, "La lista dovrebbe contenere MarioRossi.");
        assertTrue(containsGiuseppe, "La lista dovrebbe contenere GiuseppeVerdi.");
    }

    
    // test per il metodo insert
    
    @Test
    public void testInsertValid() {
        Admin newAdmin = new Admin("PaoloBianchi", "passwordSicura123");
        adao.insert(newAdmin);
        
        // Verifica dell'avvenuto inserimento tramite select
        Optional<Admin> retrieved = adao.selectById(Optional.of("PaoloBianchi"));
        assertTrue(retrieved.isPresent(), "Il nuovo admin deve essere recuperabile dal DB.");
        assertEquals("passwordSicura123", retrieved.get().getPassword());
    }

    @Test
    public void testInsertNull() {
        int sizeBefore = adao.selectAll().size();
        
        // Il metodo gestisce internamente il null con una guardia 'if(model == null) return;'
        adao.insert(null);
        
        int sizeAfter = adao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un oggetto null non deve alterare il DB.");
    }

    
    // test per il metod insertAll
    
    @Test
    public void testInsertAllValid() {
        List<Admin> adminsToAdd = new ArrayList<>();
        adminsToAdd.add(new Admin("ElenaRosa", "pwdElena"));
        adminsToAdd.add(new Admin("StefanoArancio", "pwdStefano"));
        
        adao.insertAll(adminsToAdd);
        
        List<Admin> totalAdmins = adao.selectAll();
        assertEquals(4, totalAdmins.size(), "Il numero totale di admin nel DB deve essere ora pari a 4.");
        
        assertTrue(adao.selectById(Optional.of("ElenaRosa")).isPresent());
        assertTrue(adao.selectById(Optional.of("StefanoArancio")).isPresent());
    }

    @Test
    public void testInsertAllNullOrEmpty() {
        int sizeBefore = adao.selectAll().size();
        
        // Test con lista null
        adao.insertAll(null);
        assertEquals(sizeBefore, adao.selectAll().size(), "Una lista null non deve modificare il DB.");
        
        // Test con lista vuota
        adao.insertAll(new ArrayList<>());
        assertEquals(sizeBefore, adao.selectAll().size(), "Una lista vuota non deve modificare il DB.");
    }

    
    // test per il metodo update
    
    @Test
    public void testUpdateExisting() {
        Admin adminToUpdate = new Admin("MarioRossi", "nuovaPassword99!");
        adao.update(adminToUpdate);
        
        Optional<Admin> retrieved = adao.selectById(Optional.of("MarioRossi"));
        assertTrue(retrieved.isPresent());
        assertEquals("nuovaPassword99!", retrieved.get().getPassword(), "La password deve risultare aggiornata.");
    }

    @Test
    public void testUpdateNull() {
        // Il metodo gestisce internamente il null senza sollevare eccezioni verso l'esterno
        assertDoesNotThrow(() -> adao.update(null), "L'aggiornamento di un oggetto null deve fallire silenziosamente.");
    }

    // test per il metodo delete
    
    @Test
    public void testDeleteExisting() {
        Optional<String> usernameToDelete = Optional.of("GiuseppeVerdi");
        adao.delete(usernameToDelete);
        
        Optional<Admin> retrieved = adao.selectById(usernameToDelete);
        assertFalse(retrieved.isPresent(), "L'admin eliminato non deve essere più presente.");
        assertEquals(1, adao.selectAll().size(), "Nel DB deve essere rimasto solo 1 admin.");
    }

    @Test
    public void testDeleteEmptyOptional() {
        int sizeBefore = adao.selectAll().size();
        
        adao.delete(Optional.empty());
        
        int sizeAfter = adao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "L'eliminazione tramite un Optional vuoto non deve modificare il DB.");
    }
}
