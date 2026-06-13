package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Game;
import gruppo05.gtwshared.utility.Result;
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
 * @brief Classe di test per la verifica del DAO concreto ConcreteGameDAO.
 *
 * Interroga lo stato controllato del database generato da DebugDB per testare la 
 * correttezza delle operazioni CRUD sull'entità Game. I test verificano l'accuratezza 
 * delle query basate sulla chiave primaria composta e il comportamento transazionale dei vincoli.
 */
public class ConcreteGameDAOTest {
    
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
    private final GameDAO gdao;
    
    /**
     * @brief Costruttore predefinito.
     *
     * Configura i riferimenti di supporto e inizializza il DAO concreto delle partite.
     */
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
    
    /**
     * @brief Configura l'ambiente prima dell'esecuzione di ogni singolo test case.
     *
     * Rigenera l'intera alberatura del database e scrive i record di debug deterministici 
     * per far partire ciascun blocco di test da una base dati solida e pulita.
     *
     * @throws SQLException In caso di anomalie di connessione JDBC o di scrittura SQL.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        DatabaseManager.initDB();
        ddb.initDebugDBWithoutDAO();
    }
    
    /**
     * @brief Ripristina lo stato del file system alla fine di ogni operazione di test.
     *
     * Cancella il file fisico del database locale per prevenire interferenze e 
     * contaminazioni di dati tra test eseguiti in sequenza.
     */
    @AfterEach
    public void tearDown() {
        // Cancella il database alla fine di ogni operazione
        File db = new File(DB_NAME);
        if(db.exists()) db.delete();
    }
    
    // test per il metodo selectById

    /**
     * @brief Verifica l'estrazione di un match esistente tramite chiave primaria composta.
     *
     * Passa una coppia valida di identificativi (giocatore e codice sfida) accertando 
     * che il record venga estratto correttamente e che contenga parametri di gioco coerenti.
     */
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

    /**
     * @brief Verifica che la ricerca di una chiave parzialmente inesistente fallisca.
     *
     * Sottopone al metodo un account giocatore non censito nel sistema ("PlayerInesistente"), 
     * appurando che selectById produca un Optional vuoto senza sollevare eccezioni.
     */
    @Test
    public void testSelectByIdNotExisting() {
        Optional<String> player = Optional.of("PlayerInesistente");
        Optional<Integer> challenge = Optional.of(1);
        
        Optional<Game> result = gdao.selectById(player, challenge);
        
        assertFalse(result.isPresent(), "La partita non dovrebbe essere trovata.");
    }

    /**
     * @brief Verifica la gestione robusta di selectById nel caso in cui un solo Optional sia vuoto.
     *
     * Valuta l'attivazione delle clausole di controllo a monte del metodo quando viene omesso 
     * alternativamente il nome del giocatore o il codice della sfida.
     */
    @Test
    public void testSelectByIdOneOptionalEmpty() {
        // Test con il giocatore assente
        Optional<Game> result1 = gdao.selectById(Optional.empty(), Optional.of(1));
        assertFalse(result1.isPresent(), "Se un elemento della chiave composta è vuoto, deve tornare Optional.empty().");
        
        // Test con la sfida assente
        Optional<Game> result2 = gdao.selectById(Optional.of("RobertoViola"), Optional.empty());
        assertFalse(result2.isPresent(), "Se un elemento della chiave composta è vuoto, deve tornare Optional.empty().");
    }

    /**
     * @brief Verifica il comportamento di selectById a fronte di entrambi i parametri vuoti.
     *
     * Accerta che il passaggio simultaneo di due Optional.empty() sia gestito in sicurezza 
     * restituendo un esito nullo controllato.
     */
    @Test
    public void testSelectByIdBothOptionalsEmpty() {
        Optional<Game> result = gdao.selectById(Optional.empty(), Optional.empty());
        assertFalse(result.isPresent(), "Se entrambi gli Optional sono vuoti, deve tornare Optional.empty().");
    }

    // test per il metodo selectAll

    /**
     * @brief Verifica il recupero massivo dello storico delle partite giocate.
     *
     * Controlla che l'elenco complessivo estratto non sia nullo e contenga i record di log 
     * inseriti originariamente dallo script di debug.
     */
    @Test
    public void testSelectAll() {
        List<Game> result = gdao.selectAll();
        
        assertNotNull(result, "La lista restituita non deve essere null.");
        // Verifica che ci siano i record inizializzati dal DebugDB
        assertFalse(result.isEmpty(), "La lista delle partite non dovrebbe essere vuota.");
    }

    // test per il metodo insert

    /**
     * @brief Verifica la corretta persistenza di una nuova istanza di partita valida.
     *
     * Effettua l'inserimento di un match agganciato a vincoli di integrità referenziale esistenti 
     * ("CarmineMagenta" e sfida 2), verificandone il successo con una selectById di riscontro.
     */
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

    /**
     * @brief Verifica l'immunità del metodo insert all'inserimento di riferimenti nulli.
     *
     * Dimostra che il passaggio di un parametro null non genera eccezioni impreviste 
     * e non muta la cardinalità delle righe memorizzate nella tabella.
     */
    @Test
    public void testInsertNull() {
        int sizeBefore = gdao.selectAll().size();
        gdao.insert(null);
        int sizeAfter = gdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un oggetto null non deve variare il DB.");
    }

    /**
     * @brief Verifica il blocco dell'inserimento dovuto a violazione di una Foreign Key.
     *
     * Tenta di registrare una partita associandola a una sfida inesistente (id = 999). 
     * Il test accerta che la query fallisca a causa dei vincoli referenziali attivi.
     */
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

    /**
     * @brief Verifica la persistenza di massa di una lista di record partita validi.
     *
     * Fornisce al metodo insertAll una collezione ben formata contenente due partite distinte, 
     * certificando che il numero totale di elementi cresca esattamente di 2 unità.
     */
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

    /**
     * @brief Verifica che l'invio di liste vuote o nulle venga ignorato in sicurezza.
     *
     * Assicura l'assenza di eccezioni o modifiche errate allo stato dei dati in caso di 
     * collezioni di input non popolate o nulle.
     */
    @Test
    public void testInsertAllNullOrEmpty() {
        int sizeBefore = gdao.selectAll().size();
        
        gdao.insertAll(null);
        assertEquals(sizeBefore, gdao.selectAll().size());
        
        gdao.insertAll(new ArrayList<>());
        assertEquals(sizeBefore, gdao.selectAll().size());
    }

    /**
     * @brief Verifica l'atomicità di insertAll tramite l'esecuzione di un rollback transazionale.
     *
     * Inserisce una lista composta da una partita valida e una contenente un id sfida errato (999). 
     * Il test verifica che l'intera transazione venga annullata, impedendo la scrittura di qualsiasi record.
     */
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

    /**
     * @brief Verifica la modifica dei campi non chiave di una partita preesistente.
     *
     * Recupera una partita nota, altera il suo stato di vittoria e il tempo di risposta 
     * mantenendo invariata la chiave composta originaria, verificandone il successo.
     */
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

    /**
     * @brief Verifica che il metodo update intercetti un argomento nullo in modo sicuro.
     *
     * Garantisce che la trasmissione di un riferimento null non sollevi eccezioni a runtime, 
     * interrompendo il processo in anticipo.
     */
    @Test
    public void testUpdateNull() {
        assertDoesNotThrow(() -> gdao.update(null), "L'aggiornamento di un oggetto null non deve sollevare eccezioni.");
    }

    // test per il metodo delete

    /**
     * @brief Verifica l'eliminazione mirata di un record partita.
     *
     * Esegue la rimozione del match identificato da "RobertoViola" e sfida 1, accertando 
     * il corretto decremento della tabella e l'impossibilità di recuperare il record rimosso.
     */
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

    /**
     * @brief Verifica il comportamento di blocco preventivo di delete se la chiave è parziale.
     *
     * Fornisce un Optional vuoto per la componente giocatore. Il test dimostra che la guardia 
     * interna del DAO interrompe l'esecuzione evitando di corrompere o cancellare record non voluti.
     */
    @Test
    public void testDeleteOneOptionalEmpty() {
        int sizeBefore = gdao.selectAll().size();
        
        // Passaggio di un solo optional vuoto (la guardia interna 'if(!player.isPresent() || !challenge.isPresent()) return;' deve bloccarsi)
        gdao.delete(Optional.empty(), Optional.of(1));
        
        int sizeAfter = gdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "Il database non deve subire modifiche se manca un componente della chiave.");
    }
}
