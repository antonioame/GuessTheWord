package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Game;
import gruppo05.gtwshared.utility.Result;
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
public class ConcreteGameDAOTest {
    
    private final static String DB_NAME = "ServerDB";
    private final DebugDB ddb;
    private final GameDAO gdao;
    
    public ConcreteGameDAOTest() {
        ddb = new DebugDB();
        gdao = new ConcreteGameDAO();
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
        Optional<String> player = Optional.of("RobertoViola");
        Optional<Integer> challenge = Optional.of(1);
        
        Optional<Game> result = gdao.selectById(player, challenge);
        
        assertTrue(result.isPresent(), "La partita cercata dovrebbe essere presente.");
        assertEquals("RobertoViola", result.get().getPlayer());
        assertEquals(1, result.get().getChallenge());
        assertNotNull(result.get().getResult());
        assertTrue(result.get().getResponseTime() >= 0);
    }

    @Test
    public void testSelectByIdNotExisting() {
        Optional<String> player = Optional.of("PlayerInesistente");
        Optional<Integer> challenge = Optional.of(1);
        
        Optional<Game> result = gdao.selectById(player, challenge);
        
        assertFalse(result.isPresent(), "La partita non dovrebbe essere trovata.");
    }

    @Test
    public void testSelectByIdOneOptionalEmpty() {
        // Test con il giocatore assente
        Optional<Game> result1 = gdao.selectById(Optional.empty(), Optional.of(1));
        assertFalse(result1.isPresent(), "Se un elemento della chiave composta è vuoto, deve tornare Optional.empty().");
        
        // Test con la sfida assente
        Optional<Game> result2 = gdao.selectById(Optional.of("RobertoViola"), Optional.empty());
        assertFalse(result2.isPresent(), "Se un elemento della chiave composta è vuoto, deve tornare Optional.empty().");
    }

    @Test
    public void testSelectByIdBothOptionalsEmpty() {
        Optional<Game> result = gdao.selectById(Optional.empty(), Optional.empty());
        assertFalse(result.isPresent(), "Se entrambi gli Optional sono vuoti, deve tornare Optional.empty().");
    }

    // test per il metodo selectAll

    @Test
    public void testSelectAll() {
        List<Game> result = gdao.selectAll();
        
        assertNotNull(result, "La lista restituita non deve essere null.");
        // Verifica che ci siano i record inizializzati dal DebugDB
        assertFalse(result.isEmpty(), "La lista delle partite non dovrebbe essere vuota.");
    }

    // test per il metodo insert

    @Test
    public void testInsertValid() {
        // Presuppone che il player 'CarmineMagenta' e la sfida '2' esistano nel DB di Debug
        Game newGame = new Game("CarmineMagenta", 2, Result.WIN, 45);
        gdao.insert(newGame);
        
        Optional<Game> retrieved = gdao.selectById(Optional.of("CarmineMagenta"), Optional.of(2));
        assertTrue(retrieved.isPresent(), "La partita inserita deve essere recuperabile.");
        assertEquals(Result.WIN, retrieved.get().getResult());
        assertEquals(45, retrieved.get().getResponseTime());
    }

    @Test
    public void testInsertNull() {
        int sizeBefore = gdao.selectAll().size();
        gdao.insert(null);
        int sizeAfter = gdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un oggetto null non deve variare il DB.");
    }

    @Test
    public void testInsertInvalidForeignKey() {
        int sizeBefore = gdao.selectAll().size();
        
        // Sfida 999 non esistente nel DB (Violazione del vincolo FK)
        Game invalidGame = new Game("RobertoViola", 999, Result.LOSE, 120);
        gdao.insert(invalidGame);
        
        int sizeAfter = gdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "L'inserimento deve fallire silenziosamente senza aggiungere record.");
    }

    // test per il metodo insertAll

    @Test
    public void testInsertAllValid() {
        int sizeBefore = gdao.selectAll().size();
        List<Game> list = new ArrayList<>();
        
        // Assicurarsi che le chiavi esterne (player e challenge) siano referenziate correttamente nel DebugDB
        list.add(new Game("RobertoViola", 2, Result.WIN, 30));
        list.add(new Game("CarmineMagenta", 3, Result.LOSE, 60));
        
        gdao.insertAll(list);
        
        int sizeAfter = gdao.selectAll().size();
        assertEquals(sizeBefore + 2, sizeAfter, "Il numero totale di partite deve essere aumentato di 2 unità.");
    }

    @Test
    public void testInsertAllNullOrEmpty() {
        int sizeBefore = gdao.selectAll().size();
        
        gdao.insertAll(null);
        assertEquals(sizeBefore, gdao.selectAll().size());
        
        gdao.insertAll(new ArrayList<>());
        assertEquals(sizeBefore, gdao.selectAll().size());
    }

    @Test
    public void testInsertAllWithRollback() {
        int sizeBefore = gdao.selectAll().size();
        List<Game> list = new ArrayList<>();
        
        list.add(new Game("RobertoViola", 3, Result.WIN, 15));
        // Il secondo elemento fallirà a causa di un codice sfida inesistente (999)
        list.add(new Game("RobertoViola", 999, Result.LOSE, 45));
        
        gdao.insertAll(list);
        
        int sizeAfter = gdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "Tutta la transazione deve subire un rollback: nessun record deve essere inserito.");
    }

    // test per il metodo update

    @Test
    public void testUpdateExisting() {
        // Recuperiamo una combinazione esistente (es. RobertoViola, sfida 1)
        Optional<String> player = Optional.of("RobertoViola");
        Optional<Integer> challenge = Optional.of(1);
        
        Optional<Game> original = gdao.selectById(player, challenge);
        assertTrue(original.isPresent());
        
        // Modifichiamo l'esito e il tempo di risposta mantenendo la stessa chiave composta
        Game updatedGame = new Game("RobertoViola", 1, Result.WIN, 999);
        gdao.update(updatedGame);
        
        Optional<Game> retrieved = gdao.selectById(player, challenge);
        assertTrue(retrieved.isPresent());
        assertEquals(Result.WIN, retrieved.get().getResult(), "Il risultato deve risultare aggiornato.");
        assertEquals(999, retrieved.get().getResponseTime(), "Il tempo di risposta deve risultare aggiornato.");
    }

    @Test
    public void testUpdateNull() {
        assertDoesNotThrow(() -> gdao.update(null), "L'aggiornamento di un oggetto null non deve sollevare eccezioni.");
    }

    // test per il metodo delete

    @Test
    public void testDeleteExisting() {
        Optional<String> player = Optional.of("RobertoViola");
        Optional<Integer> challenge = Optional.of(1);
        
        int sizeBefore = gdao.selectAll().size();
        gdao.delete(player, challenge);
        int sizeAfter = gdao.selectAll().size();
        
        assertEquals(sizeBefore - 1, sizeAfter, "Il record selezionato deve essere rimosso.");
        assertFalse(gdao.selectById(player, challenge).isPresent(), "La partita eliminata non deve più essere trovata.");
    }

    @Test
    public void testDeleteOneOptionalEmpty() {
        int sizeBefore = gdao.selectAll().size();
        
        // Passaggio di un solo optional vuoto (la guardia interna 'if(!player.isPresent() || !challenge.isPresent()) return;' deve bloccarsi)
        gdao.delete(Optional.empty(), Optional.of(1));
        
        int sizeAfter = gdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "Il database non deve subire modifiche se manca un componente della chiave.");
    }
}
