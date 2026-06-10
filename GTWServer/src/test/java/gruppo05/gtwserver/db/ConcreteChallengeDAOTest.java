package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Challenge;
import gruppo05.gtwshared.utility.Difficulty;
import java.io.File;
import java.sql.Date;
import java.time.LocalDate;
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
public class ConcreteChallengeDAOTest {
    
    private final static String DB_NAME = "ServerDB";
    private final DebugDB ddb;
    private final ChallengeDAO cdao;
    
    public ConcreteChallengeDAOTest() {
        ddb = new DebugDB();
        cdao = new ConcreteChallengeDAO();
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
        Optional<Integer> code = Optional.of(1);
        Optional<Challenge> result = cdao.selectById(code);
        
        assertTrue(result.isPresent(), "La sfida con codice 1 dovrebbe essere presente.");
        assertEquals(1, result.get().getCode());
        assertEquals("amore", result.get().getWord());
        assertEquals(6, result.get().getSource());
        assertEquals(Difficulty.EASY, result.get().getDifficulty());
    }

    @Test
    public void testSelectByIdNotExisting() {
        Optional<Integer> code = Optional.of(999);
        Optional<Challenge> result = cdao.selectById(code);
        
        assertFalse(result.isPresent(), "La sfida con codice inesistente non deve essere trovata.");
    }

    @Test
    public void testSelectByIdEmptyOptional() {
        Optional<Challenge> result = cdao.selectById(Optional.empty());
        
        assertFalse(result.isPresent(), "L'invio di un Optional vuoto deve ritornare un Optional vuoto.");
    }

    // test per il metodo selectAll

    @Test
    public void testSelectAll() {
        List<Challenge> result = cdao.selectAll();
        
        assertNotNull(result);
        assertEquals(5, result.size(), "Il database di debug contiene inizialmente 5 sfide.");
    }

    // test per il metodo insert

    @Test
    public void testInsertValid() {
        // "metallo" associato alla sorgente 6 esiste nella tabella word del DebugDB
        Challenge newChallenge = new Challenge(10, Date.valueOf(LocalDate.of(2026, 6, 10)), Difficulty.NORMAL, "metallo", 6);
        cdao.insert(newChallenge);
        
        Optional<Challenge> retrieved = cdao.selectById(Optional.of(10));
        assertTrue(retrieved.isPresent(), "La nuova sfida deve essere presente nel DB.");
        assertEquals("metallo", retrieved.get().getWord());
    }

    @Test
    public void testInsertNull() {
        int sizeBefore = cdao.selectAll().size();
        cdao.insert(null);
        int sizeAfter = cdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un oggetto null non deve alterare il DB.");
    }

    @Test
    public void testInsertInvalidForeignKey() {
        int sizeBefore = cdao.selectAll().size();
        
        // Questa combinazione chiave esterna (parola, sorgente) non esiste nella tabella word
        Challenge invalidChallenge = new Challenge(11, Date.valueOf(LocalDate.of(2026, 6, 10)), Difficulty.EASY, "parolaInesistente", 6);
        cdao.insert(invalidChallenge);
        
        int sizeAfter = cdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "L'inserimento deve fallire e non aggiungere record per via del vincolo FK.");
    }

    // test per il metodo insertAll

    @Test
    public void testInsertAllValid() {
        List<Challenge> list = new ArrayList<>();
        // "creatività" (sorgente 6) e "ricerca" (sorgente 6) esistono in word ma non sono usate in challenge
        list.add(new Challenge(20, Date.valueOf(LocalDate.of(2026, 6, 10)), Difficulty.HARD, "creatività", 6));
        list.add(new Challenge(21, Date.valueOf(LocalDate.of(2026, 6, 10)), Difficulty.EASY, "ricerca", 6));
        
        cdao.insertAll(list);
        
        assertEquals(7, cdao.selectAll().size(), "Il numero totale di sfide deve essere ora pari a 7.");
        assertTrue(cdao.selectById(Optional.of(20)).isPresent());
        assertTrue(cdao.selectById(Optional.of(21)).isPresent());
    }

    @Test
    public void testInsertAllNullOrEmpty() {
        int sizeBefore = cdao.selectAll().size();
        
        cdao.insertAll(null);
        assertEquals(sizeBefore, cdao.selectAll().size());
        
        cdao.insertAll(new ArrayList<>());
        assertEquals(sizeBefore, cdao.selectAll().size());
    }

    @Test
    public void testInsertAllWithRollback() {
        int sizeBefore = cdao.selectAll().size();
        List<Challenge> list = new ArrayList<>();
        list.add(new Challenge(30, Date.valueOf(LocalDate.of(2026, 6, 10)), Difficulty.HARD, "creatività", 6));
        // Il secondo elemento viola il vincolo di chiave esterna (sorgente 99 non esiste)
        list.add(new Challenge(31, Date.valueOf(LocalDate.of(2026, 6, 10)), Difficulty.EASY, "ricerca", 99));
        
        cdao.insertAll(list);
        
        int sizeAfter = cdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "La transazione deve fare rollback completo: nessuna sfida deve essere inserita.");
    }

    // test per il metodo update

    @Test
    public void testUpdateExisting() {
        Optional<Challenge> original = cdao.selectById(Optional.of(1));
        assertTrue(original.isPresent());
        
        // Modifichiamo la difficoltà e la data mantenendo i vincoli validi
        Challenge updatedChallenge = new Challenge(1, Date.valueOf(LocalDate.of(2026, 6, 15)), Difficulty.HARD, "amore", 6);
        cdao.update(updatedChallenge);
        
        Optional<Challenge> retrieved = cdao.selectById(Optional.of(1));
        assertTrue(retrieved.isPresent());
        assertEquals(Difficulty.HARD, retrieved.get().getDifficulty(), "La difficoltà deve risultare aggiornata.");
        assertEquals(Date.valueOf(LocalDate.of(2026, 6, 15)).toString(), retrieved.get().getDate().toString(), "La data deve risultare aggiornata.");
    }

    @Test
    public void testUpdateNull() {
        assertDoesNotThrow(() -> cdao.update(null));
    }

    // test per il metodo delete

    @Test
    public void testDeleteNoReferences() {
        // Inseriamo prima una sfida che non ha alcuna partita (game) collegata
        Challenge challengeWithNoGames = new Challenge(40, Date.valueOf(LocalDate.of(2026, 6, 10)), Difficulty.EASY, "biblioteca", 7);
        cdao.insert(challengeWithNoGames);
        
        int sizeAfterInsert = cdao.selectAll().size();
        
        // Cancellazione della sfida appena inserita
        cdao.delete(Optional.of(40));
        
        int sizeAfterDelete = cdao.selectAll().size();
        assertEquals(sizeAfterInsert - 1, sizeAfterDelete, "La sfida senza riferimenti esterni deve essere eliminata con successo.");
        assertFalse(cdao.selectById(Optional.of(40)).isPresent());
    }

    @Test
    public void testDeleteWithReferences() {
        int sizeBefore = cdao.selectAll().size();
        
        // La sfida 1 è referenziata nella tabella game (partite di RobertoViola e CarmineMagenta)
        // Il vincolo è ON DELETE RESTRICT
        cdao.delete(Optional.of(1));
        
        int sizeAfter = cdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "La sfida non deve essere eliminata a causa del vincolo ON DELETE RESTRICT sulla tabella game.");
        assertTrue(cdao.selectById(Optional.of(1)).isPresent(), "La sfida referenziata deve essere ancora presente nel DB.");
    }

    @Test
    public void testDeleteEmptyOptional() {
        int sizeBefore = cdao.selectAll().size();
        cdao.delete(Optional.empty());
        int sizeAfter = cdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'invio di un Optional vuoto non deve alterare lo stato del database.");
    }
}
