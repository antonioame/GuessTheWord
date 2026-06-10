package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Word;
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
public class ConcreteWordDAOTest {
    
    private final static String DB_NAME = "ServerDB";
    private final DebugDB ddb;
    private final WordDAO wdao;
    
    public ConcreteWordDAOTest() {
        ddb = new DebugDB();
        wdao = new ConcreteWordDAO();
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
        // Presuppone l'esistenza nel DebugDB della parola "metallo" associata alla sorgente 6
        Optional<String> token = Optional.of("metallo");
        Optional<Integer> source = Optional.of(6);
        
        Optional<Word> result = wdao.selectById(token, source);
        
        assertTrue(result.isPresent(), "La parola cercata dovrebbe essere presente.");
        assertEquals("metallo", result.get().getToken());
        assertEquals(6, result.get().getSource());
        assertTrue(result.get().getFrequency() >= 0);
    }

    @Test
    public void testSelectByIdNotExisting() {
        Optional<String> token = Optional.of("parolaInesistente");
        Optional<Integer> source = Optional.of(6);
        
        Optional<Word> result = wdao.selectById(token, source);
        assertFalse(result.isPresent(), "La parola non dovrebbe essere trovata.");
    }

    @Test
    public void testSelectByIdEmptyOptionals() {
        Optional<Word> result1 = wdao.selectById(Optional.empty(), Optional.of(6));
        Optional<Word> result2 = wdao.selectById(Optional.of("metallo"), Optional.empty());
        Optional<Word> result3 = wdao.selectById(Optional.empty(), Optional.empty());
        
        assertFalse(result1.isPresent());
        assertFalse(result2.isPresent());
        assertFalse(result3.isPresent());
    }

    // test per il metodo selectAll

    @Test
    public void testSelectAll() {
        List<Word> result = wdao.selectAll();
        
        assertNotNull(result, "La lista restituita non deve essere null.");
        assertFalse(result.isEmpty(), "La lista delle parole disponibili non deve essere vuota.");
        
        // Verifica che tutte le parole caricate appartengano a una sorgente valida (clausola della vista availableWords)
        boolean hasCasa = result.stream().anyMatch(w -> w.getToken().equals("metallo") && w.getSource() == 6);
        assertTrue(hasCasa, "La lista dovrebbe contenere la parola 'metallo' legata alla sorgente 6.");
    }

    // test per il metodo selectAllWhere

    @Test
    public void testSelectAllWhereAllFiltersPresent() {
        // Filtro completo su un record specifico
        Optional<String> token = Optional.of("metallo");
        Optional<Integer> frequenza = Optional.of(10); // Valore indicativo, coerente con DebugDB
        Optional<Integer> source = Optional.of(6);
        
        List<Word> result = wdao.selectAllWhere(token, frequenza, source);
        assertNotNull(result);
        // Se i parametri corrispondono esattamente, restituisce il record filtrato
        result.forEach(w -> {
            assertEquals("metallo", w.getToken());
            assertEquals(6, w.getSource());
        });
    }

    @Test
    public void testSelectAllWhereOnlyToken() {
        Optional<String> token = Optional.of("metallo");
        
        List<Word> result = wdao.selectAllWhere(token, Optional.empty(), Optional.empty());
        assertNotNull(result);
        assertFalse(result.isEmpty());
        result.forEach(w -> assertEquals("metallo", w.getToken()));
    }

    @Test
    public void testSelectAllWhereNoFilters() {
        // Se non viene passato alcun filtro, deve comportarsi come selectAll()
        List<Word> noFiltersResult = wdao.selectAllWhere(Optional.empty(), Optional.empty(), Optional.empty());
        List<Word> allResult = wdao.selectAll();
        
        assertEquals(allResult.size(), noFiltersResult.size(), "Senza filtri deve restituire lo stesso numero di elementi di selectAll.");
    }

    // test per il metodo insert

    @Test
    public void testInsertValid() {
        // Inserimento di una parola agganciata a una sorgente esistente (ID 6)
        Word newWord = new Word("albero", 5, 6);
        wdao.insert(newWord);
        
        Optional<Word> retrieved = wdao.selectById(Optional.of("albero"), Optional.of(6));
        assertTrue(retrieved.isPresent(), "La parola inserita deve essere recuperabile.");
        assertEquals(5, retrieved.get().getFrequency());
    }

    @Test
    public void testInsertNull() {
        int sizeBefore = wdao.selectAll().size();
        wdao.insert(null);
        int sizeAfter = wdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un oggetto null non deve alterare il DB.");
    }

    @Test
    public void testInsertDuplicatePrimaryKey() {
        // Tentativo di inserire una chiave composta (token + source) già esistente
        Word duplicate = new Word("metallo", 99, 6);
        int sizeBefore = wdao.selectAll().size();
        
        wdao.insert(duplicate);
        
        int sizeAfter = wdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "Il database non deve accettare chiavi primarie duplicate.");
    }

    @Test
    public void testInsertForeignKeyViolation() {
        // Sorgente 999 inesistente nel database
        Word orphanWord = new Word("orfana", 1, 999);
        int sizeBefore = wdao.selectAll().size();
        
        wdao.insert(orphanWord);
        
        int sizeAfter = wdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "L'inserimento deve fallire a causa del vincolo FK sulla tabella source.");
    }

    // test per il metodo insertAll

    @Test
    public void testInsertAllValid() {
        List<Word> words = new ArrayList<>();
        words.add(new Word("gatto", 3, 6));
        words.add(new Word("cane", 4, 6));
        
        int sizeBefore = wdao.selectAll().size();
        wdao.insertAll(words);
        int sizeAfter = wdao.selectAll().size();
        
        assertEquals(sizeBefore + 2, sizeAfter, "Le due parole devono essere inserite correttamente.");
        assertTrue(wdao.selectById(Optional.of("gatto"), Optional.of(6)).isPresent());
        assertTrue(wdao.selectById(Optional.of("cane"), Optional.of(6)).isPresent());
    }

    @Test
    public void testInsertAllNullOrEmpty() {
        int sizeBefore = wdao.selectAll().size();
        
        wdao.insertAll(null);
        assertEquals(sizeBefore, wdao.selectAll().size());
        
        wdao.insertAll(new ArrayList<>());
        assertEquals(sizeBefore, wdao.selectAll().size());
    }

    @Test
    public void testInsertAllWithRollback() {
        int sizeBefore = wdao.selectAll().size();
        List<Word> words = new ArrayList<>();
        words.add(new Word("valida", 2, 6));
        // Fallirà per violazione di chiave primaria (metallo-6 già presente)
        words.add(new Word("metallo", 12, 6));
        
        wdao.insertAll(words);
        
        int sizeAfter = wdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "La transazione deve eseguire il rollback totale in caso di errore nel batch.");
        assertFalse(wdao.selectById(Optional.of("valida"), Optional.of(6)).isPresent(), "La parola transitoria non deve essere salvata.");
    }

    // test per il metodo update

    @Test
    public void testUpdateExisting() {
        Optional<String> token = Optional.of("metallo");
        Optional<Integer> source = Optional.of(6);
        
        Optional<Word> original = wdao.selectById(token, source);
        assertTrue(original.isPresent());
        
        // Modifica solo la frequenza mantendo la stessa chiave composta
        Word wordToUpdate = new Word("metallo", 150, 6);
        wdao.update(wordToUpdate);
        
        Optional<Word> retrieved = wdao.selectById(token, source);
        assertTrue(retrieved.isPresent());
        assertEquals(150, retrieved.get().getFrequency(), "La frequenza della parola deve risultare aggiornata.");
    }

    @Test
    public void testUpdateNull() {
        assertDoesNotThrow(() -> wdao.update(null), "L'aggiornamento di un oggetto null non deve sollevare eccezioni.");
    }

    // test per il metodo delete

    @Test
    public void testDeleteExisting() {
        // Inseriamo prima un token di test isolato
        Word tempWord = new Word("eliminaMe", 1, 6);
        wdao.insert(tempWord);
        
        int sizeAfterInsert = wdao.selectAll().size();
        
        wdao.delete(Optional.of("eliminaMe"), Optional.of(6));
        
        int sizeAfterDelete = wdao.selectAll().size();
        assertEquals(sizeAfterInsert - 1, sizeAfterDelete, "Il record deve essere eliminato correttamente.");
        assertFalse(wdao.selectById(Optional.of("eliminaMe"), Optional.of(6)).isPresent());
    }

    @Test
    public void testDeleteEmptyOptionals() {
        int sizeBefore = wdao.selectAll().size();
        
        wdao.delete(Optional.empty(), Optional.of(6));
        wdao.delete(Optional.of("metallo"), Optional.empty());
        
        int sizeAfter = wdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "Il passaggio di Optional vuoti non deve alterare lo stato del DB.");
    }
}
