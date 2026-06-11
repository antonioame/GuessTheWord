package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Player;
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
 * @author francesco-vecchione
 * @brief Classe di test per la verifica del DAO concreto ConcretePlayerDAO.
 *
 * Interfaccia i blocchi di test con lo stato controllato generato da DebugDB per 
 * validare il ciclo di vita dell'entità Player. Verifica la precisione del recupero dati, 
 * il comportamento delle transazioni in caso di violazione della chiave primaria e la 
 * stabilità difensiva dell'integrità referenziale.
 */
public class ConcretePlayerDAOTest {
    
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
    private final PlayerDAO pdao;
    
    /**
     * @brief Costruttore predefinito.
     *
     * Inizializza l'utility di debug del database ed istanzia il DAO concreto dedicato ai giocatori.
     */
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
    
    /**
     * @brief Configura lo stato della base dati prima dell'esecuzione di ogni test case.
     *
     * Ricostruisce lo schema relazionale delle tabelle e inserisce i record iniziali di 
     * debug per assicurare l'isolamento degli ambienti d'esecuzione.
     *
     * @throws SQLException In caso di anomalie nella connessione JDBC o nell'esecuzione delle query.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        DatabaseManager.initDB();
        ddb.initDebugDBWithoutDAO();
    }
    
    /**
     * @brief Pulisce le risorse del file system al completamento di ogni test case.
     *
     * Rimuove fisicamente il file del database locale in modo da scongiurare la 
     * persistenza di dati residui o sporchi fra i test.
     */
    @AfterEach
    public void tearDown() {
        // Cancella il database alla fine di ogni operazione
        File db = new File(DB_NAME);
        if(db.exists()) db.delete();
    }
    
    // test per il metodo selectById

    /**
     * @brief Verifica il recupero di un giocatore registrato tramite la sua chiave primaria.
     *
     * Controlla che il record associato a "RobertoViola" sia presente e che i relativi 
     * contatori e contatori cumulativi statistici vengano mappati in modo coerente.
     */
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

    /**
     * @brief Verifica che la ricerca di uno username non registrato restituisca un Optional vuoto.
     *
     * Fornisce una chiave di ricerca inesistente ("UtenteInesistente") per appurare che 
     * il metodo selectById non generi eccezioni ma segnali correttamente l'assenza del record.
     */
    @Test
    public void testSelectByIdNotExisting() {
        Optional<String> username = Optional.of("UtenteInesistente");
        Optional<Player> result = pdao.selectById(username);
        
        assertFalse(result.isPresent(), "Il giocatore non dovrebbe essere trovato.");
    }

    /**
     * @brief Verifica il comportamento di selectById quando viene fornito un parametro vuoto.
     *
     * Valuta l'efficacia del controllo di guardia interno del DAO a fronte del passaggio 
     * di un Optional.empty(), che deve tradursi in un esito controllato vuoto.
     */
    @Test
    public void testSelectByIdEmptyOptional() {
        Optional<Player> result = pdao.selectById(Optional.empty());
        
        assertFalse(result.isPresent(), "Il passaggio di un Optional vuoto deve restituire un Optional vuoto.");
    }

    // test per il metodo selectAll

    /**
     * @brief Verifica il recupero massivo di tutti i giocatori registrati nel sistema.
     *
     * Si accerta che l'elenco restituito non sia nullo, non sia vuoto e contenga i profili 
     * storici di partenza come ad esempio l'utente "RobertoViola".
     */
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

    /**
     * @brief Verifica il corretto inserimento di un nuovo giocatore valido.
     *
     * Persiste un'istanza di Player inizializzata con valori statistici azzerati 
     * e ne constata l'avvenuto salvataggio tramite una successiva query di verifica.
     */
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

    /**
     * @brief Verifica l'immunità del metodo insert a fronte di argomenti nulli.
     *
     * Controlla che il passaggio di un riferimento null venga scartato dalla guardia del 
     * metodo senza alterare lo stato o il numero di righe salvate nella tabella.
     */
    @Test
    public void testInsertNull() {
        int sizeBefore = pdao.selectAll().size();
        pdao.insert(null);
        int sizeAfter = pdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un oggetto null non deve alterare il DB.");
    }

    /**
     * @brief Verifica l'inibizione dell'inserimento in presenza di una chiave primaria duplicata.
     *
     * Tenta l'inserimento di un account avente uno username identico a un record esistente 
     * ("RobertoViola"), accertando che il vincolo UNIQUE/PK blocchi l'operazione preservando i dati.
     */
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

    /**
     * @brief Verifica l'inserimento massivo di una collezione di giocatori validi.
     *
     * Passa al metodo insertAll una lista contenente due istanze corrette, certificando 
     * che la dimensione totale aumenti di due unità e che i profili siano individualmente accessibili.
     */
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

    /**
     * @brief Verifica che insertAll gestisca in modo sicuro collezioni vuote o nulle.
     *
     * Sottopone al metodo degli input non validi o privi di elementi per validare l'assenza 
     * di eccezioni impreviste a runtime (NullPointerException) o modifiche indebite al DB.
     */
    @Test
    public void testInsertAllNullOrEmpty() {
        int sizeBefore = pdao.selectAll().size();
        
        pdao.insertAll(null);
        assertEquals(sizeBefore, pdao.selectAll().size(), "Una lista null non deve modificare il DB.");
        
        pdao.insertAll(new ArrayList<>());
        assertEquals(sizeBefore, pdao.selectAll().size(), "Una lista vuota non deve modificare il DB.");
    }

    /**
     * @brief Verifica l'atomicità della transazione massiva mediante rollback in caso di errore.
     *
     * Predispone una lista in cui il secondo elemento viola la chiave primaria ("RobertoViola"). 
     * Il test si assicura che venga eseguito un rollback completo, annullando anche il salvataggio 
     * del primo record ("SoniaGrigi") per garantire la consistenza di tipo tutto-o-niente.
     */
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

    /**
     * @brief Verifica la modifica corretta dei dati associati ad un giocatore esistente.
     *
     * Modifica la password del profilo "RobertoViola" e procede ad una rilettura per 
     * constatare che la modifica sia stata registrata stabilmente sul database.
     */
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

    /**
     * @brief Verifica la tolleranza del metodo update a parametri in ingresso nulli.
     *
     * Accerta che l'applicazione intercetti preventivamente i riferimenti nulli prima di 
     * formulare o eseguire query SQL, azzerando il rischio di crash imprevisti.
     */
    @Test
    public void testUpdateNull() {
        assertDoesNotThrow(() -> pdao.update(null), "L'aggiornamento di un oggetto null non deve sollevare eccezioni.");
    }

    // test per il metodo delete

    /**
     * @brief Verifica la rimozione fisica di un utente privo di collegamenti esterni.
     *
     * Inserisce un utente fittizio privo di storici di gioco per poi richiederne l'eliminazione, 
     * constatando la riduzione del numero di record complessivi.
     */
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

    /**
     * @brief Verifica l'efficacia del blocco ON DELETE RESTRICT in presenza di record figli.
     *
     * Tenta l'eliminazione dell'utente "RobertoViola" il quale ha all'attivo dei match salvati 
     * nella tabella 'game'. Il test valida il rifiuto della cancellazione da parte del database 
     * in modo da salvaguardare la consistenza e l'integrità dei dati referenziati.
     */
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

    /**
     * @brief Verifica che la richiesta di rimozione tramite Optional vuoto venga ignorata.
     *
     * Sottopone un Optional.empty() al metodo delete per sincerarsi che la query non 
     * venga eseguita e lo stato complessivo dei dati non subisca variazioni.
     */
    @Test
    public void testDeleteEmptyOptional() {
        int sizeBefore = pdao.selectAll().size();
        pdao.delete(Optional.empty());
        int sizeAfter = pdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'eliminazione tramite un Optional vuoto non deve apportare modifiche.");
    }
}
