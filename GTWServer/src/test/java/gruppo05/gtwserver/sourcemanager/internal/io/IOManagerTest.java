/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.internal.io;

/**
 *
 * @author Hermann
 */
import gruppo05.gtwserver.db.DAO;
import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.model.SourceId;
import gruppo05.gtwserver.model.Word;
import gruppo05.gtwserver.model.WordId;
import gruppo05.gtwserver.sourcemanager.exception.FrequencyMapNotFoundException;
import gruppo05.gtwserver.sourcemanager.exception.SourceNotFoundException;
import gruppo05.gtwserver.sourcemanager.exception.StorageException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Classe di test d'unità per verificare i comportamenti del componente IOManager,
 * utilizzando implementazioni Fake in-memory al posto di librerie di mocking e 
 * istanziando direttamente i modelli Core.
 */
public class IOManagerTest {

    private FakeSourceDAO fakeSourceDao;
    private FakeWordDAO fakeWordDao;
    private IOManager ioManager;

    @BeforeEach
    void setUp() {
        fakeSourceDao = new FakeSourceDAO();
        fakeWordDao = new FakeWordDAO();
        ioManager = new IOManager(fakeSourceDao, fakeWordDao);
    }

    @Test
    void testReadSourceWordsAndPeriods_Success(@TempDir Path tempDir) throws Exception {
        // Prepariamo un file di prova temporaneo
        Path textFile = tempDir.resolve("test_source.txt");
        List<String> lines = Arrays.asList(
                "Ciao mondo... Questo e' un test.",
                "Java 8 funziona molto bene!"
        );
        Files.write(textFile, lines);

        // Creiamo la sorgente reale usando il costruttore del model
        Source source = new Source(1, textFile);

        // Esecuzione dell'algoritmo di lettura
        Stream<String> wordStream = ioManager.readSourceWordsAndPeriods(source);
        List<String> resultTokens = wordStream.collect(Collectors.toList());

        // Verifiche
        assertNotNull(resultTokens);
        assertTrue(resultTokens.contains("Ciao"));
        assertTrue(resultTokens.contains("mondo"));
        assertTrue(resultTokens.contains(".")); // Il blocco "..." deve diventare un singolo punto
        assertTrue(resultTokens.contains("Questo"));
        assertTrue(resultTokens.contains("test"));
        assertTrue(resultTokens.contains("Java"));
        assertTrue(resultTokens.contains("8"));
        
        assertFalse(resultTokens.contains("e'"));
        assertFalse(resultTokens.contains("bene!"));
    }

    @Test
    void testReadSourceWordsAndPeriods_SourceNotFoundException() {
        // Usiamo un path inesistente
        Source source = new Source(1, Paths.get("percorso_fittizio_inesistente.txt"));

        assertThrows(SourceNotFoundException.class, () -> {
            ioManager.readSourceWordsAndPeriods(source);
        });
    }

    @Test
    void testReadSourceMapFrequency_Success() throws Exception {
        Source source = new Source(42, Paths.get("dummy.txt"));

        // Popoliamo il database fake con dati prestabiliti usando il costruttore di Word
        fakeWordDao.insert(new Word("casa", 10, 42));
        fakeWordDao.insert(new Word("albero", 5, 42));
        fakeWordDao.insert(new Word("intruso", 1, 99)); // Appartiene a un'altra sorgente

        Map<String, Integer> frequencyMap = ioManager.readSourceMapFrequency(source);

        assertNotNull(frequencyMap);
        assertEquals(2, frequencyMap.size());
        assertEquals(10, frequencyMap.get("casa"));
        assertEquals(5, frequencyMap.get("albero"));
        assertNull(frequencyMap.get("intruso"));
    }

    @Test
    void testReadSourceMapFrequency_EmptyOrNotFound() {
        Source source = new Source(99, Paths.get("dummy.txt"));
        // Il database è vuoto per questa sorgente

        assertThrows(FrequencyMapNotFoundException.class, () -> {
            ioManager.readSourceMapFrequency(source);
        });
    }

    @Test
    void testWriteSource_Success() throws Exception {
        Source source = new Source(10, Paths.get("dummy.txt"));
        
        ioManager.writeSource(source);

        // Verifichiamo che l'elemento sia stato effettivamente aggiunto alla mappa interna del DAO
        assertEquals(1, fakeSourceDao.database.size());
        assertTrue(fakeSourceDao.database.containsValue(source));
    }

    @Test
    void testWriteSource_StorageException() {
        Source source = new Source(10, Paths.get("dummy.txt"));
        // Configuriamo il finto DAO per fallire di proposito
        fakeSourceDao.shouldFailOnInsert = true;

        assertThrows(StorageException.class, () -> {
            ioManager.writeSource(source);
        });
    }

    @Test
    void testWriteSourceMapFrequency_Success() throws Exception {
        Source source = new Source(1, Paths.get("dummy.txt"));

        Map<String, Integer> frequencies = new HashMap<>();
        frequencies.put("test", 3);
        frequencies.put("junit", 1);

        ioManager.writeSourceMapFrequency(source, frequencies);

        // Verifichiamo il salvataggio massivo controllando i valori nella mappa
        assertEquals(2, fakeWordDao.database.size());
        assertTrue(fakeWordDao.database.values().stream().anyMatch(w -> w.getId().getToken().equals("test") && w.getFrequency() == 3));
        assertTrue(fakeWordDao.database.values().stream().anyMatch(w -> w.getId().getToken().equals("junit") && w.getFrequency() == 1));
    }

    @Test
    void testDeleteSource_Success() throws Exception {
        Source source = new Source(100, Paths.get("dummy.txt"));
        
        // Popoliamo il database fake solo con il sorgente
        fakeSourceDao.insert(source);

        // Eseguiamo la cancellazione
        ioManager.deleteSource(source);

        // Assicuriamoci che la sorgente sia rimossa.
        assertTrue(fakeSourceDao.database.isEmpty());
    }

    // --- CLASSI FAKE INTERNE AL TEST ---

    /**
     * Implementazione In-Memory del database per i Source.
     * La chiave primaria (K) è il tipo custom SourceId.
     */
    private static class FakeSourceDAO implements DAO<Source, SourceId> {
        // Usiamo una Map per rendere coerente e sicura la ricerca per ID
        public final Map<SourceId, Source> database = new HashMap<>();
        public boolean shouldFailOnInsert = false;

        @Override
        public Optional<Source> selectById(SourceId modelId) {
            return Optional.ofNullable(database.get(modelId));
        }

        @Override
        public List<Source> selectAll() {
            return new ArrayList<>(database.values());
        }

        @Override
        public void insert(Source item) {
            if (shouldFailOnInsert) throw new RuntimeException("Simulated Database Error");
            database.put(item.getId(), item);
        }

        @Override
        public void insertAll(List<Source> items) {
            for (Source item : items) {
                insert(item);
            }
        }

        @Override
        public void update(Source model) {
            database.put(model.getId(), model);
        }

        @Override
        public void delete(SourceId modelId) {
            database.remove(modelId);
        }
    }

    /**
     * Implementazione In-Memory del database per le Word.
     * La chiave primaria (K) è il tipo custom WordId.
     */
    private static class FakeWordDAO implements DAO<Word, WordId> {
        public final Map<WordId, Word> database = new HashMap<>();

        @Override
        public Optional<Word> selectById(WordId modelId) {
            return Optional.ofNullable(database.get(modelId));
        }

        @Override
        public List<Word> selectAll() {
            return new ArrayList<>(database.values());
        }

        @Override
        public void insert(Word item) {
            database.put(item.getId(), item);
        }

        @Override
        public void insertAll(List<Word> items) {
            for (Word item : items) {
                insert(item);
            }
        }

        @Override
        public void update(Word model) {
            database.put(model.getId(), model);
        }

        @Override
        public void delete(WordId modelId) {
            database.remove(modelId);
        }
    }
}