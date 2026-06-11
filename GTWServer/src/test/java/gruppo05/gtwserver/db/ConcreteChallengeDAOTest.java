package gruppo05.gtwserver.db;

import gruppo05.gtwserver.model.Challenge;
import gruppo05.gtwshared.utility.Difficulty;
import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
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
 * @author francesco-vecchione
 * @brief Classe di test per la verifica del DAO concreto ConcreteChallengeDAO.
 *
 * Sfrutta il dataset deterministico fornito da DebugDB per testare la correttezza 
 * delle operazioni CRUD sull'entità Challenge, ponendo particolare enfasi sulla 
 * verifica dei vincoli di integrità referenziale (Foreign Key ed effetti ON DELETE).
 */
public class ConcreteChallengeDAOTest {
    
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
    private final ChallengeDAO cdao;
    
    /**
     * @brief Costruttore predefinito.
     *
     * Inizializza l'istanza dell'utility di debug e l'implementazione del DAO per le sfide.
     */
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
    
    /**
     * @brief Configura l'ambiente prima dell'esecuzione di ogni singolo test case.
     *
     * Inizializza la struttura del database e inserisce i dati di debug tramite query 
     * dirette per garantire uno stato noto ed evitare dipendenze esterne.
     *
     * @throws SQLException In caso di anomalie nella transazione SQL o nella connessione JDBC.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        DatabaseManager.initDB();
        ddb.initDebugDBWithoutDAO();
    }
    
    /**
     * @brief Esegue la pulizia del database al termine di ogni singolo test case.
     *
     * Rimuove il file del database SQLite locale per assicurare il completo isolamento 
     * tra i test consecutivi ed evitare la persistenza di dati sporchi.
     */
    @AfterEach
    public void tearDown() {
        // Cancella il database alla fine di ogni operazione
        File db = new File(DB_NAME);
        if(db.exists()) db.delete();
    }
    
    // test per il metodo selectById

    /**
     * @brief Verifica il recupero di una sfida esistente tramite il suo codice identificativo.
     *
     * Controlla che l'Optional restituito dal DAO contenga l'oggetto richiesto e che 
     * tutti i relativi attributi (parola, sorgente, difficoltà) corrispondano al record 1.
     */
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

    /**
     * @brief Verifica che la ricerca di un codice non registrato restituisca un esito vuoto.
     *
     * Passa un ID non esistente nel dataset iniziale ("999") accertando che il metodo 
     * gestisca correttamente l'assenza restituendo un Optional vuoto senza lanciare eccezioni.
     */
    @Test
    public void testSelectByIdNotExisting() {
        Optional<Integer> code = Optional.of(999);
        Optional<Challenge> result = cdao.selectById(code);
        
        assertFalse(result.isPresent(), "La sfida con codice inesistente non deve essere trovata.");
    }

    /**
     * @brief Verifica il comportamento defensivo di selectById con un parametro vuoto.
     *
     * Garantisce la stabilità del metodo restituendo un Optional vuoto nel caso in cui 
     * l'argomento in ingresso sia un Optional.empty().
     */
    @Test
    public void testSelectByIdEmptyOptional() {
        Optional<Challenge> result = cdao.selectById(Optional.empty());
        
        assertFalse(result.isPresent(), "L'invio di un Optional vuoto deve ritornare un Optional vuoto.");
    }

    // test per il metodo selectAll

    /**
     * @brief Verifica il recupero massivo di tutte le sfide registrate.
     *
     * Ispeziona la dimensione della collezione restituita, certificando che sia non nulla 
     * e che corrisponda esattamente alle 5 sfide introdotte dallo stato iniziale di debug.
     */
    @Test
    public void testSelectAll() {
        List<Challenge> result = cdao.selectAll();
        
        assertNotNull(result);
        assertEquals(5, result.size(), "Il database di debug contiene inizialmente 5 sfide.");
    }

    // test per il metodo insert

    /**
     * @brief Verifica il corretto inserimento di una nuova sfida valida nel database.
     *
     * Assicura la persistenza di un oggetto Challenge associato a una combinazione coerente 
     * di parola e sorgente ("metallo", 6) già esistente nella tabella 'word'.
     */
    @Test
    public void testInsertValid() {
        // "metallo" associato alla sorgente 6 esiste nella tabella word del DebugDB
        Challenge newChallenge = new Challenge(10, Date.valueOf(LocalDate.of(2026, 6, 10)), Difficulty.NORMAL, "metallo", 6);
        cdao.insert(newChallenge);
        
        Optional<Challenge> retrieved = cdao.selectById(Optional.of(10));
        assertTrue(retrieved.isPresent(), "La nuova sfida deve essere presente nel DB.");
        assertEquals("metallo", retrieved.get().getWord());
    }

    /**
     * @brief Verifica la stabilità del metodo insert a fronte di un inserimento null.
     *
     * Controlla che il passaggio di un riferimento nullo venga intercettato dalla guardia 
     * interna del DAO, senza alterare il conteggio complessivo dei record presenti.
     */
    @Test
    public void testInsertNull() {
        int sizeBefore = cdao.selectAll().size();
        cdao.insert(null);
        int sizeAfter = cdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'inserimento di un oggetto null non deve alterare il DB.");
    }

    /**
     * @brief Verifica il blocco dell'inserimento in caso di violazione della Foreign Key.
     *
     * Tenta l'inserimento di una sfida con una parola non censita all'interno della tabella del dizionario. 
     * Il test certifica che il vincolo d'integrità referenziale blocchi l'operazione mantenendo la consistenza.
     */
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

    /**
     * @brief Verifica la persistenza di massa di una lista di sfide valide.
     *
     * Passa al metodo insertAll una collezione con due sfide ben formate, accertando 
     * che la dimensione totale passi da 5 a 7 elementi e che entrambi i record siano recuperabili.
     */
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

    /**
     * @brief Verifica che l'invio di liste vuote o nulle non alteri lo stato del sistema.
     *
     * Certifica l'immunità del metodo insertAll da eccezioni di tipo NullPointerException o 
     * esecuzioni SQL vuote superflue.
     */
    @Test
    public void testInsertAllNullOrEmpty() {
        int sizeBefore = cdao.selectAll().size();
        
        cdao.insertAll(null);
        assertEquals(sizeBefore, cdao.selectAll().size());
        
        cdao.insertAll(new ArrayList<>());
        assertEquals(sizeBefore, cdao.selectAll().size());
    }

    /**
     * @brief Verifica il comportamento atomico della transazione con rollback parziale.
     *
     * Compone una lista contenente una sfida valida e una non valida (violazione FK sulla sorgente 99). 
     * Il test verifica che l'intera transazione fallisca, ripristinando il database allo stato iniziale 
     * senza salvare nemmeno l'elemento parzialmente corretto.
     */
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

    /**
     * @brief Verifica la corretta modifica dei campi di una sfida preesistente.
     *
     * Aggiorna i campi di data e difficoltà per la sfida con id = 1 e riesegue una lettura 
     * per sincerarsi della corretta sovrascrittura delle informazioni sul database.
     */
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

    /**
     * @brief Verifica la gestione difensiva del metodo update in caso di input nullo.
     *
     * Assicura che la chiamata al metodo passando un riferimento null non provochi crash dell'applicazione 
     * e venga intercettata silenziosamente a monte delle query SQL.
     */
    @Test
    public void testUpdateNull() {
        assertDoesNotThrow(() -> cdao.update(null));
    }

    // test per il metodo delete

    /**
     * @brief Verifica la cancellazione di una sfida non referenziata da record figli.
     *
     * Inserisce una nuova sfida fittizia (id = 40) priva di storici di gioco associati, procedendo 
     * alla sua rimozione immediata per verificare il corretto decremento dei record.
     */
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

    /**
     * @brief Verifica l'intervento del vincolo ON DELETE RESTRICT su sfide referenziate.
     *
     * Tenta la rimozione della sfida 1 (la quale possiede partite storiche collegate all'interno della tabella 'game'). 
     * Il test accerta che l'eliminazione venga bloccata dal DBMS e che il record rimanga integro nel DB.
     */
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

    /**
     * @brief Verifica che l'invio di un Optional di rimozione vuoto non provochi modifiche.
     *
     * Sottopone al metodo delete un Optional.empty(), validando la stabilità del sistema 
     * e la totale assenza di fluttuazioni nel numero di record presenti.
     */
    @Test
    public void testDeleteEmptyOptional() {
        int sizeBefore = cdao.selectAll().size();
        cdao.delete(Optional.empty());
        int sizeAfter = cdao.selectAll().size();
        
        assertEquals(sizeBefore, sizeAfter, "L'invio di un Optional vuoto non deve alterare lo stato del database.");
    }
}
