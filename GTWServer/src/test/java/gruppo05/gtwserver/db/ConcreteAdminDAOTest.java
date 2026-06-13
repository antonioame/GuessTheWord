package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Admin;
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
 * @brief Classe di test per la verifica del DAO concreto ConcreteAdminDAO.
 * 
 * Sfrutta il dataset controllato fornito da DebugDB per testare l'accuratezza e 
 * l'affidabilità delle operazioni CRUD (Create, Read, Update, Delete) sull'entità Admin.
 */
public class ConcreteAdminDAOTest {
    
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
    private final AdminDAO adao;
    
    /**
     * @brief Costruttore predefinito.
     * 
     * Istanzia l'utility di debug e l'implementazione concreta del DAO per gli amministratori.
     */
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

    /**
     * @brief Configura l'ambiente prima dell'esecuzione di ogni singolo test case.
     * 
     * Ricrea l'intera struttura del database e inserisce i dati di debug di partenza,
     * garantendo l'assoluta indipendenza e riproducibilità di ciascun test.
     * 
     * @throws SQLException In caso di anomalie nella transazione SQL o nella connessione JDBC.
     */    
    @BeforeEach
    public void setUp() throws SQLException {
        DatabaseManager.initDB();
        ddb.initDebugDBWithoutDAO();
    }
    
    /**
     * @brief Esegue la pulizia delle risorse al termine di ciascun test case.
     * 
     * Rimuove il file fisico del database per eliminare gli effetti collaterali 
     * e i dati residui lasciati dai test precedenti.
     */    
    @AfterEach
    public void tearDown() {
        // Cancella il database alla fine di ogni operazione
        File db = new File(DB_NAME);
        if(db.exists()) db.delete();
    }

    // test per il metodo selectById
    
    /**
     * @brief Verifica il recupero di un amministratore esistente tramite la sua chiave primaria.
     * 
     * Controlla che l'Optional restituito sia pieno e che le credenziali dell'oggetto Admin 
     * estratto corrispondano esattamente a quelle caricate in fase di setup ("MarioRossi").
     */
    @Test
    public void testSelectByIdExisting() {
        Optional<String> username = Optional.of("MarioRossi");
        Optional<Admin> result = adao.selectById(username);
        
        assertTrue(result.isPresent(), "L'admin dovrebbe essere presente nel DB.");
        assertEquals("MarioRossi", result.get().getUsername());
        assertEquals("nunciafac@", result.get().getPassword());
    }

    /**
     * @brief Verifica che la ricerca di una chiave non registrata restituisca un esito vuoto.
     * 
     * Passa al DAO un username non presente nel dataset iniziale e si assicura che il 
     * metodo selectById gestisca l'assenza restituendo un Optional vuoto invece di lanciare eccezioni.
     */
    @Test
    public void testSelectByIdNotExisting() {
        Optional<String> username = Optional.of("AdminInesistente");
        Optional<Admin> result = adao.selectById(username);
        
        assertFalse(result.isPresent(), "L'admin non dovrebbe essere trovato.");
    }

    /**
     * @brief Verifica il comportamento di selectById a fronte di un parametro di ricerca vuoto.
     * 
     * Dimostra la robustezza del metodo quando viene invocato passando Optional.empty() come ID, 
     * accertando che restituisca un Optional vuoto.
     */
    @Test
    public void testSelectByIdEmptyOptional() {
        Optional<Admin> result = adao.selectById(Optional.empty());
        
        assertFalse(result.isPresent(), "Il passaggio di un Optional vuoto deve restituire un Optional vuoto.");
    }

    // test per il metodo selectAll
    
    /**
     * @brief Verifica il recupero massivo di tutti gli amministratori presenti nel sistema.
     * 
     * Certifica che la lista non sia nulla, che la dimensione sia coerente con il dataset di debug 
     * (esattamente 2) e che includa gli utenti "MarioRossi" e "GiuseppeVerdi".
     */
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
    
    /**
     * @brief Verifica il corretto inserimento di una nuova istanza Admin valida nel database.
     * 
     * Esegue la persistenza di un nuovo record e ne verifica l'effettivo salvataggio effettuando 
     * una successiva interrogazione mirata tramite la selectById.
     */
    @Test
    public void testInsertValid() {
        Admin newAdmin = new Admin("PaoloBianchi", "passwordSicura123");
        adao.insert(newAdmin);
        
        // Verifica dell'avvenuto inserimento tramite select
        Optional<Admin> retrieved = adao.selectById(Optional.of("PaoloBianchi"));
        assertTrue(retrieved.isPresent(), "Il nuovo admin deve essere recuperabile dal DB.");
        assertEquals("passwordSicura123", retrieved.get().getPassword());
    }

    /**
     * @brief Verifica la gestione difensiva del metodo insert in caso di parametro nullo.
     * 
     * Accerta che il passaggio di un riferimento null venga ignorato internamente dalla guardia 
     * del metodo senza scatenare NullPointerException e lasciando inalterato il numero di record nel DB.
     */
    @Test
    public void testInsertNull() {
        int sizeBefore = adao.selectAll().size();
        
        // Il metodo gestisce internamente il null con una guardia 'if(model == null) return;'
        adao.insert(null);
        
        int sizeAfter = adao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un oggetto null non deve alterare il DB.");
    }

    
    // test per il metod insertAll
    
    /**
     * @brief Verifica la persistenza di massa tramite una struttura a lista.
     * 
     * Fornisce al metodo insertAll una collezione contenente due nuovi amministratori validi, 
     * assicurando che la cardinalità complessiva della tabella salga a 4 e che entrambi i record 
     * siano estratti ed ispezionati con successo.
     */
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

    /**
     * @brief Test della politica di tolleranza ai guasti di insertAll con input anomali.
     * 
     * Controlla che il passaggio di una collezione nulla o di una lista priva di elementi 
     * non comprometta la stabilità dell'applicazione e mantenga intatta la consistenza dei dati.
     */
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
    
    /**
     * @brief Verifica la corretta modifica dei campi di un record preesistente.
     * 
     * Aggiorna la password dell'amministratore "MarioRossi" ed esegue una rilettura sul database 
     * per appurare che la nuova password sia stata sovrascritta con successo.
     */
    @Test
    public void testUpdateExisting() {
        Admin adminToUpdate = new Admin("MarioRossi", "nuovaPassword99!");
        adao.update(adminToUpdate);
        
        Optional<Admin> retrieved = adao.selectById(Optional.of("MarioRossi"));
        assertTrue(retrieved.isPresent());
        assertEquals("nuovaPassword99!", retrieved.get().getPassword(), "La password deve risultare aggiornata.");
    }

    /**
     * @brief Verifica l'immunità del metodo update a parametri in ingresso nulli.
     * 
     * Dimostra che la trasmissione di un riferimento nullo viene intercettata preventivamente 
     * impedendo eccezioni a runtime o fallimenti fatali dello statement SQL.
     */
    @Test
    public void testUpdateNull() {
        // Il metodo gestisce internamente il null senza sollevare eccezioni verso l'esterno
        assertDoesNotThrow(() -> adao.update(null), "L'aggiornamento di un oggetto null deve fallire silenziosamente.");
    }

    // test per il metodo delete
    
    /**
     * @brief Verifica la corretta rimozione logica/fisica di un amministratore dal database.
     *
     * Invia una richiesta di cancellazione per "GiuseppeVerdi" e verifica che il record non 
     * compaia più nelle selezioni puntuali e che il conteggio complessivo decrementi di conseguenza.
     */
    @Test
    public void testDeleteExisting() {
        Optional<String> usernameToDelete = Optional.of("GiuseppeVerdi");
        adao.delete(usernameToDelete);
        
        Optional<Admin> retrieved = adao.selectById(usernameToDelete);
        assertFalse(retrieved.isPresent(), "L'admin eliminato non deve essere più presente.");
        assertEquals(1, adao.selectAll().size(), "Nel DB deve essere rimasto solo 1 admin.");
    }

    /**
     * @brief Verifica la stabilità del metodo delete a fronte di una richiesta di cancellazione vuota.
     * 
     * Garantisce che l'invio di un Optional.empty() non alteri lo stato interno del database, 
     * preservando l'integrità dei dati salvati.
     */
    @Test
    public void testDeleteEmptyOptional() {
        int sizeBefore = adao.selectAll().size();
        
        adao.delete(Optional.empty());
        
        int sizeAfter = adao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "L'eliminazione tramite un Optional vuoto non deve modificare il DB.");
    }
}
