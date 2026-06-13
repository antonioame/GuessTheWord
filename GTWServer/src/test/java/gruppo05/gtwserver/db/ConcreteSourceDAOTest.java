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
 * @brief Classe di test per la verifica del DAO concreto ConcreteSourceDAO.
 *
 * Utilizza lo stato controllato generato da DebugDB per convalidare il ciclo di vita 
 * dell'entità Source. Verifica in particolare i meccanismi di cancellazione logica 
 * (soft delete), accertandosi che i record con percorso nullo vengano ignorati e 
 * che le transazioni in blocco rispettino l'atomicità.
 */
public class ConcreteSourceDAOTest {
    
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
    private final SourceDAO sdao;
    
    /**
     * @brief Costruttore predefinito.
     *
     * Inizializza l'utility di debug del database e assegna il riferimento all'istanza 
     * concreta del DAO delle sorgenti.
     */
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
    
    /**
     * @brief Configura l'ambiente prima dell'esecuzione di ogni singolo test case.
     *
     * Ricostruisce lo schema relazionale delle tabelle e inserisce i record iniziali di 
     * debug per assicurare la consistenza e l'isolamento degli ambienti d'esecuzione.
     *
     * @throws SQLException In caso di anomalie nella connessione JDBC o nell'esecuzione delle query.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        DatabaseManager.initDB();
        ddb.initDebugDBWithoutDAO();
    }
    
    /**
     * @brief Esegue la rimozione del file di database alla conclusione di ciascun test.
     *
     * Garantisce il completo isolamento tra i casi di test eliminando il database SQLite locale, 
     * impedendo la persistenza di record sporchi o alterati.
     */
    @AfterEach
    public void tearDown() {
        // Cancella il database alla fine di ogni operazione
        File db = new File(DB_NAME);
        if(db.exists()) db.delete();
    }
    
    // test per il metodo selectById

    /**
     * @brief Verifica il corretto recupero di una sorgente valida tramite il suo codice identificativo.
     *
     * Controlla che il record 6, pre-caricato dallo script di debug, sia rilevato come presente 
     * e contenga un percorso file non nullo.
     */
    @Test
    public void testSelectByIdExisting() {
        // Presuppone l'esistenza della sorgente valida con ID 6 nel database di debug
        Optional<Integer> id = Optional.of(6);
        Optional<Source> result = sdao.selectById(id);
        
        assertTrue(result.isPresent(), "La sorgente con ID 6 dovrebbe essere presente.");
        assertEquals(6, result.get().getId());
        assertNotNull(result.get().getPath(), "Il path associato alla sorgente non deve essere null.");
    }

    /**
     * @brief Verifica che la ricerca di una sorgente non registrata produca un esito vuoto.
     *
     * Interroga il DAO impostando un ID inesistente ("999"), appurando che il sistema ritorni 
     * un Optional vuoto anziché lanciare eccezioni a runtime.
     */
    @Test
    public void testSelectByIdNotExisting() {
        Optional<Integer> id = Optional.of(999);
        Optional<Source> result = sdao.selectById(id);
        
        assertFalse(result.isPresent(), "La sorgente con ID inesistente non deve essere trovata.");
    }

    /**
     * @brief Verifica la gestione protetta di selectById in caso di parametro vuoto.
     *
     * Assicura che l'invio di un Optional.empty() non avvii interrogazioni ma venga 
     * intercettato restituendo un esito controllato vuoto.
     */
    @Test
    public void testSelectByIdEmptyOptional() {
        Optional<Source> result = sdao.selectById(Optional.empty());
        
        assertFalse(result.isPresent(), "L'invio di un Optional vuoto deve ritornare un Optional vuoto.");
    }

    /**
     * @brief Verifica che selectById escluda i record con percorso nullo (cancellati logicamente).
     *
     * Inserisce direttamente via SQL un record anomalo avente PATH NULL. Il test accerta che la 
     * clausola restrittiva del DAO escluda l'elemento, considerandolo rimosso dal sistema.
     */
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

    /**
     * @brief Verifica il recupero complessivo di tutte le sorgenti attive.
     *
     * Valuta la consistenza dell'elenco estratto, verificando che sia non nullo e contenga 
     * le fonti correttamente inizializzate dal dataset di debug.
     */
    @Test
    public void testSelectAll() {
        List<Source> result = sdao.selectAll();
        
        assertNotNull(result, "La lista restituita non deve essere null.");
        assertFalse(result.isEmpty(), "La lista delle sorgenti valide non dovrebbe essere vuota.");
    }

    /**
     * @brief Verifica che selectAll filtri ed escluda i record con percorso nullo.
     *
     * Introduce forzatamente una sorgente con PATH NULL. Il test certifica che il metodo 
     * selectAll non incrementi il proprio conteggio, escludendo la riga orfana mediante i filtri SQL.
     */
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

    /**
     * @brief Verifica l'inserimento corretto di una nuova sorgente valida.
     *
     * Memorizza un oggetto Source ben formato e ne constata la stabilità effettuando 
     * una successiva selectById di riscontro sul codice identificativo dedicato.
     */
    @Test
    public void testInsertValid() {
        Source newSource = new Source(10, Paths.get("/fake/path/source10.txt"));
        sdao.insert(newSource);
        
        Optional<Source> retrieved = sdao.selectById(Optional.of(10));
        assertTrue(retrieved.isPresent(), "La nuova sorgente deve essere presente nel DB.");
        assertEquals(Paths.get("/fake/path/source10.txt"), retrieved.get().getPath());
    }

    /**
     * @brief Verifica l'immunità del metodo insert a fronte di parametri in ingresso nulli.
     *
     * Controlla che il passaggio di un riferimento null venga scartato dalle clausole di guardia 
     * interne al DAO senza alterare il quantitativo totale di righe memorizzate nella tabella.
     */
    @Test
    public void testInsertNull() {
        int sizeBefore = sdao.selectAll().size();
        sdao.insert(null);
        int sizeAfter = sdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un oggetto null non deve alterare lo stato del DB.");
    }

    /**
     * @brief Verifica l'inibizione dell'inserimento in caso di chiave primaria duplicata.
     *
     * Tenta di registrare una sorgente fornendo un ID già occupato nel sistema (id = 6). 
     * Il test valida il blocco dell'operazione volto a preservare la consistenza dell'indice.
     */
    @Test
    public void testInsertDuplicateId() {
        Source duplicateSource = new Source(6, Paths.get("/fake/path/duplicate.txt"));
        
        int sizeBefore = sdao.selectAll().size();
        sdao.insert(duplicateSource);
        int sizeAfter = sdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un ID duplicato deve fallire senza aggiungere record.");
    }

    // test per il metodo insertAll

    /**
     * @brief Verifica la persistenza di massa di una collezione di sorgenti valide.
     *
     * Invia al metodo insertAll un elenco contenente due istanze configurate correttamente, 
     * certificando che la tabella incrementi di due unità e che i record siano leggibili.
     */
    @Test
    public void testInsertAllValid() {
        List<Source> list = new ArrayList<>();
        list.add(new Source(20, Paths.get("/fake/path/source20.txt")));
        list.add(new Source(21, Paths.get("/fake/path/source21.txt")));
        
        int sizeBefore = sdao.selectAll().size();
        sdao.insertAll(list);
        int sizeAfter = sdao.selectAll().size();
        
        assertEquals(sizeBefore + 2, sizeAfter, "Il database deve contenere le 2 nuove sorgenti inserite.");
        assertTrue(sdao.selectById(Optional.of(20)).isPresent());
        assertTrue(sdao.selectById(Optional.of(21)).isPresent());
    }

    /**
     * @brief Verifica la tolleranza di insertAll verso collezioni vuote o nulle.
     *
     * Fornisce input vuoti o privi di riferimenti al metodo massivo per convalidare la stabilità 
     * difensiva ed escludere l'insorgenza di eccezioni impreviste a runtime.
     */
    @Test
    public void testInsertAllNullOrEmpty() {
        int sizeBefore = sdao.selectAll().size();
        
        sdao.insertAll(null);
        assertEquals(sizeBefore, sdao.selectAll().size());
        
        sdao.insertAll(new ArrayList<>());
        assertEquals(sizeBefore, sdao.selectAll().size());
    }

    /**
     * @brief Verifica l'atomicità di insertAll mediante rollback transazionale in caso di errore.
     *
     * Crea un elenco miscellaneo in cui il secondo record viola la chiave primaria (ID 6 esistente). 
     * Il test dimostra che l'intero blocco viene respinto, annullando anche l'inserimento della prima fonte valida.
     */
    @Test
    public void testInsertAllWithRollback() {
        int sizeBefore = sdao.selectAll().size();
        List<Source> list = new ArrayList<>();
        list.add(new Source(30, Paths.get("/fake/path/source30.txt")));
        // Il secondo elemento fallisce per violazione di chiave primaria (ID 6 già esistente)
        list.add(new Source(6, Paths.get("/fake/path/source6_fail.txt")));
        
        sdao.insertAll(list);
        
        int sizeAfter = sdao.selectAll().size();
        assertEquals(sizeBefore, sizeAfter, "La transazione deve eseguire il rollback: nessuna sorgente deve essere aggiunta.");
        assertFalse(sdao.selectById(Optional.of(30)).isPresent(), "La sorgente transitoria non deve essere stata salvata.");
    }

    // test per il metodo update

    /**
     * @brief Verifica la modifica dei campi informativi di una sorgente registrata.
     *
     * Modifica il percorso del file associato alla sorgente 6 e interroga nuovamente il 
     * database per verificare la persistenza del nuovo tracciato.
     */
    @Test
    public void testUpdateExisting() {
        Optional<Integer> id = Optional.of(6);
        Optional<Source> original = sdao.selectById(id);
        assertTrue(original.isPresent());
        
        Source updatedSource = new Source(6, Paths.get("/fake/path/new_path_2026.txt"));
        sdao.update(updatedSource);
        
        Optional<Source> retrieved = sdao.selectById(id);
        assertTrue(retrieved.isPresent());
        assertEquals(Paths.get("/fake/path/new_path_2026.txt"), retrieved.get().getPath(), "Il percorso (path) deve risultare aggiornato.");
    }

    /**
     * @brief Verifica che il metodo update gestisca in sicurezza argomenti nulli.
     *
     * Assicura l'assenza di crash dell'applicazione nel caso in cui venga passato un 
     * riferimento nullo, interrompendo anzitempo l'invocazione delle routine SQL.
     */
    @Test
    public void testUpdateNull() {
        assertDoesNotThrow(() -> sdao.update(null), "L'aggiornamento di un oggetto null non deve sollevare eccezioni.");
    }

    // test per il metodo delete

    /**
     * @brief Verifica l'eliminazione di una sorgente priva di legami referenziali esterni.
     *
     * Inserisce un record di test isolato (id = 40) e procede alla sua rimozione, verificando 
     * il corretto decremento della cardinalità complessiva dei dati attivi.
     */
    @Test
    public void testDeleteNoReferences() {
        Source tempSource = new Source(40, Paths.get("/fake/path/temp.txt"));
        sdao.insert(tempSource);
        
        int sizeAfterInsert = sdao.selectAll().size();
        sdao.delete(Optional.of(40));
        int sizeAfterDelete = sdao.selectAll().size();
        
        assertEquals(sizeAfterInsert - 1, sizeAfterDelete, "La sorgente isolata deve essere eliminata correttamente.");
        assertFalse(sdao.selectById(Optional.of(40)).isPresent());
    }

    /**
     * @brief Verifica il meccanismo di soft delete (cancellazione logica) su sorgenti referenziate.
     *
     * Tenta l'eliminazione della sorgente 6, la quale è vincolata a righe dipendenti nelle tabelle 
     * word e challenge. Il test dimostra che, per preservare l'integrità referenziale, il record non 
     * viene rimosso fisicamente, ma il suo campo 'path' viene impostato a NULL, escludendolo 
     * dalle normali operazioni del DAO pur mantenendo intatta la struttura relazionale.
     *
     * @throws SQLException In caso di anomalie di interrogazione diretta a basso livello tramite JDBC.
     */
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
    
    /**
     * @brief Verifica che la chiamata a delete con un Optional vuoto non muti lo stato del DB.
     *
     * Sottopone un Optional.empty() al metodo di rimozione, accertando che l'azione venga 
     * ignorata senza applicare modifiche alle tabelle.
     */
    @Test
    public void testDeleteEmptyOptional() {
        int sizeBefore = sdao.selectAll().size();
        sdao.delete(Optional.empty());
        int sizeAfter = sdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'invio di un Optional vuoto non deve modificare lo stato del database.");
    }
}
