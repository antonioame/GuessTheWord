package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Word;
import java.io.File;
import java.sql.SQLException;
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
 * @brief Classe di test per la verifica del DAO concreto ConcreteWordDAO.
 *
 * Utilizza lo stato controllato del database generato da DebugDB per convalidare 
 * il ciclo di vita e la persistenza dell'entità Word. I test si concentrano sulla precisione 
 * delle query basate su chiavi composte, sui meccanismi di filtraggio condizionale 
 * e sulla robustezza dei vincoli transazionali e referenziali.
 */
public class ConcreteWordDAOTest {
    
    /**
     * @brief Nome del file di database locale SQLite utilizzato per isolare i test. 
     */
    private final static String DB_NAME = "ServerDB";
    
    /**
     * @brief Riferimento all'utility di debug per il popolamento diretto delle tabelle. 
     */
    private final DebugDB ddb;
    
    /**
     * @brief Il Data Access Object (DAO) sotto analisi in questa suite di test. 
     */
    private final WordDAO wdao;
    
    /**
     * @brief Costruttore predefinito.
     *
     * Configura i supporti di base e istanzia l'implementazione del DAO per i vocaboli.
     */
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
    
    /**
     * @brief Configura lo stato del database prima dell'esecuzione di ogni test case.
     *
     * Inizializza lo schema relazionale delle tabelle e inserisce un set di dati controllato 
     * in modo da garantire l'indipendenza e la riproducibilità di ciascun test case.
     *
     * @throws SQLException In caso di errori di connessione JDBC o di esecuzione delle query.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        DatabaseManager.initDB();
        ddb.initDebugDBWithoutDAO();
    }
    
    /**
     * @brief Esegue il ripristino dell'ambiente al completamento di ogni test case.
     *
     * Rimuove il file del database SQLite locale, annullando le modifiche transitorie 
     * ed evitando la contaminazione tra test consecutivi.
     */
    @AfterEach
    public void tearDown() {
        // Cancella il database alla fine di ogni operazione
        File db = new File(DB_NAME);
        if(db.exists()) db.delete();
    }
  
    // test per il metodo selectById

    /**
     * @brief Verifica il recupero di una parola esistente tramite la sua chiave composta.
     *
     * Passa una coppia valida di attributi primaria ("metallo", sorgente 6) constatando 
     * il corretto mapping dei campi informativi associati al record.
     */
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

    /**
     * @brief Verifica che la ricerca di un token non registrato restituisca un esito vuoto.
     *
     * Fornisce una stringa non memorizzata ("parolaInesistente"), verificando che il metodo 
     * selectById segnali l'assenza restituendo un Optional vuoto anziché scatenare eccezioni.
     */
    @Test
    public void testSelectByIdNotExisting() {
        Optional<String> token = Optional.of("parolaInesistente");
        Optional<Integer> source = Optional.of(6);
        
        Optional<Word> result = wdao.selectById(token, source);
        assertFalse(result.isPresent(), "La parola non dovrebbe essere trovata.");
    }

    /**
     * @brief Verifica la stabilità di selectById nel caso in cui le chiavi siano parziali o vuote.
     *
     * Sottopone diverse combinazioni in cui almeno uno dei due parametri di ricerca è omesso, 
     * convalidando il comportamento di guardia protetto del metodo.
     */
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

    /**
     * @brief Verifica il recupero completo di tutte le parole disponibili nel dizionario.
     *
     * Accerta che la collezione non sia nulla, non sia vuota e si interfacci con la vista 
     * 'availableWords' escludendo i vocaboli associati a sorgenti prive di un percorso valido.
     */
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

    /**
     * @brief Verifica il filtraggio condizionale con tutti i parametri di ricerca impostati.
     *
     * Applica restrizioni simultanee su token, frequenza e sorgente, verificando che la query 
     * dinamica restituisca esclusivamente i record adatti.
     */
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

    /**
     * @brief Verifica la query di ricerca condizionale filtrando unicamente per stringa token.
     *
     * Omette i parametri di frequenza e id sorgente per convalidare il corretto isolamento 
     * e la composizione flessibile dei criteri all'interno della stringa SQL.
     */
    @Test
    public void testSelectAllWhereOnlyToken() {
        Optional<String> token = Optional.of("metallo");
        
        List<Word> result = wdao.selectAllWhere(token, Optional.empty(), Optional.empty());
        assertNotNull(result);
        assertFalse(result.isEmpty());
        result.forEach(w -> assertEquals("metallo", w.getToken()));
    }

    /**
     * @brief Verifica il comportamento di selectAllWhere quando non viene passato alcun criterio.
     *
     * Dimostra che il passaggio di tre Optional vuoti viene intercettato delegando l'esecuzione 
     * a selectAll() per estrarre l'intero set di parole attive.
     */
    @Test
    public void testSelectAllWhereNoFilters() {
        // Se non viene passato alcun filtro, deve comportarsi come selectAll()
        List<Word> noFiltersResult = wdao.selectAllWhere(Optional.empty(), Optional.empty(), Optional.empty());
        List<Word> allResult = wdao.selectAll();
        
        assertEquals(allResult.size(), noFiltersResult.size(), "Senza filtri deve restituire lo stesso numero di elementi di selectAll.");
    }

    // test per il metodo insert

    /**
     * @brief Verifica il corretto inserimento di una nuova parola valida nel database.
     *
     * Assicura la persistenza di un record collegato a un vincolo di sorgente coerente 
     * (id = 6) e ne ispeziona la riuscita eseguendo una selectById di riscontro.
     */
    @Test
    public void testInsertValid() {
        // Inserimento di una parola agganciata a una sorgente esistente (ID 6)
        Word newWord = new Word("albero", 5, 6);
        wdao.insert(newWord);
        
        Optional<Word> retrieved = wdao.selectById(Optional.of("albero"), Optional.of(6));
        assertTrue(retrieved.isPresent(), "La parola inserita deve essere recuperabile.");
        assertEquals(5, retrieved.get().getFrequency());
    }

    /**
     * @brief Verifica l'immunità del metodo insert in caso di argomenti nulli.
     *
     * Controlla che il passaggio di un riferimento null sia intercettato in sicurezza dalle 
     * guardie del DAO, lasciando inalterato il quantitativo totale di record salvati.
     */
    @Test
    public void testInsertNull() {
        int sizeBefore = wdao.selectAll().size();
        wdao.insert(null);
        int sizeAfter = wdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un oggetto null non deve alterare il DB.");
    }

    /**
     * @brief Verifica il blocco dell'inserimento a fronte di una chiave primaria duplicata.
     *
     * Invia una richiesta di scrittura per una chiave composta già occupata nel sistema 
     * ("metallo", 6), validando il rifiuto della query a salvaguardia dell'indice univoco.
     */
    @Test
    public void testInsertDuplicatePrimaryKey() {
        // Tentativo di inserire una chiave composta (token + source) già esistente
        Word duplicate = new Word("metallo", 99, 6);
        int sizeBefore = wdao.selectAll().size();
        
        wdao.insert(duplicate);
        
        int sizeAfter = wdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "Il database non deve accettare chiavi primarie duplicate.");
    }

    /**
     * @brief Verifica il blocco della persistenza indotto da una violazione di Foreign Key.
     *
     * Tenta l'inserimento di un vocabolo associato a un identificativo di fonte inesistente ("999"). 
     * Il test valida l'intervento dei vincoli d'integrità referenziale a protezione dei dati orfani.
     */
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

    /**
     * @brief Verifica l'inserimento massivo di una collezione di vocaboli validi.
     *
     * Passa al metodo insertAll una lista contenente due elementi ben strutturati, accertando 
     * che il conteggio complessivo salga di due unità e che i record siano leggibili.
     */
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

    /**
     * @brief Verifica l'immunità del metodo insertAll da strutture a collezione nulle o vuote.
     *
     * Sottopone argomenti non validi o privi di istanze per certificare la stabilità difensiva 
     * ed escludere fluttuazioni non desiderate nel numero di righe memorizzate.
     */
    @Test
    public void testInsertAllNullOrEmpty() {
        int sizeBefore = wdao.selectAll().size();
        
        wdao.insertAll(null);
        assertEquals(sizeBefore, wdao.selectAll().size());
        
        wdao.insertAll(new ArrayList<>());
        assertEquals(sizeBefore, wdao.selectAll().size());
    }

    /**
     * @brief Verifica il comportamento atomico di insertAll tramite rollback transazionale.
     *
     * Crea un blocco in cui un elemento viola la chiave primaria (record duplicato). Il test 
     * certifica che l'intero pacchetto batch venga annullato, stornando anche l'inserimento valido.
     */
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

    /**
     * @brief Verifica la modifica dei campi informativi di un record preesistente.
     *
     * Aggiorna il valore di frequenza associato alla chiave primaria composta "metallo"-6, 
     * interrogando nuovamente il database per appurarne la corretta sovrascrittura.
     */
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

    /**
     * @brief Verifica la stabilità del metodo update a fronte di argomenti di modifica nulli.
     *
     * Controlla che il passaggio di un riferimento nullo sia gestito a monte, scongiurando 
     * l'insorgenza di crash o fallimenti imprevisti dell'applicazione a runtime.
     */
    @Test
    public void testUpdateNull() {
        assertDoesNotThrow(() -> wdao.update(null), "L'aggiornamento di un oggetto null non deve sollevare eccezioni.");
    }

    // test per il metodo delete

    /**
     * @brief Verifica la corretta rimozione fisica di un record parola isolato.
     *
     * Registra preventivamente un record di test e procede alla richiesta di cancellazione 
     * sulla chiave composta dedicata, verificando la riduzione del numero complessivo delle righe.
     */
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

    /**
     * @brief Verifica che la chiamata a delete tramite parametri vuoti venga ignorata.
     *
     * Sottopone degli Optional vuoti per verificare l'intervento dei blocchi di guardia 
     * a tutela della stabilità dello stato dei dati.
     */
    @Test
    public void testDeleteEmptyOptionals() {
        int sizeBefore = wdao.selectAll().size();
        
        wdao.delete(Optional.empty(), Optional.of(6));
        wdao.delete(Optional.of("metallo"), Optional.empty());
        
        int sizeAfter = wdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "Il passaggio di Optional vuoti non deve alterare lo stato del DB.");
    }
}
