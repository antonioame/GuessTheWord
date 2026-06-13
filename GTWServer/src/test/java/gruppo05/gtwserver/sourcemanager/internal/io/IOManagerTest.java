/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.internal.io;

import gruppo05.gtwserver.db.SourceDAO;
import gruppo05.gtwserver.db.WordDAO;
import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.model.Word;
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
 * istanziando direttamente i modelli Core aggiornati.
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

    /**
     * @brief Verifica la lettura delle righe grezze per l'estetica del generatore.
     */
    @Test
    void testReadRawLines_Success(@TempDir Path tempDir) throws Exception {
        Path textFile = tempDir.resolve("raw_source.txt");
        List<String> lines = Arrays.asList(
                "L'inizio.",
                "La fine!"
        );
        Files.write(textFile, lines);
        Source source = new Source(1, textFile);

        Stream<String> lineStream = ioManager.readRawLines(source);
        List<String> resultLines = lineStream.collect(Collectors.toList());

        assertEquals(2, resultLines.size());
        assertEquals("L'inizio.", resultLines.get(0));
        assertEquals("La fine!", resultLines.get(1)); // Punteggiatura intatta!
    }

    /**
     * @brief Verifica l'estrazione pura delle parole per il database statistico.
     */
    @Test
    void testReadSourceWords_Success(@TempDir Path tempDir) throws Exception {
        Path textFile = tempDir.resolve("test_source.txt");
        List<String> lines = Arrays.asList(
                "Ciao mondo... Questo e' un test.",
                "Java 8 funziona dell'animo!"
        );
        Files.write(textFile, lines);

        Source source = new Source(1, textFile);

        Stream<String> wordStream = ioManager.readSourceWords(source);
        List<String> resultTokens = wordStream.collect(Collectors.toList());

        assertNotNull(resultTokens);
        assertTrue(resultTokens.contains("Ciao"));
        assertTrue(resultTokens.contains("mondo"));
        assertTrue(resultTokens.contains("Questo"));
        assertTrue(resultTokens.contains("test"));
        assertTrue(resultTokens.contains("Java"));
        assertTrue(resultTokens.contains("8"));
        
        // Verifica che la punteggiatura forte sia SPARITA
        assertFalse(resultTokens.contains("."));
        assertFalse(resultTokens.contains("..."));
        
        // Verifica la pulizia dell'apostrofo (e' diventa e, dell'animo diventa dell e animo)
        assertTrue(resultTokens.contains("e"));
        assertFalse(resultTokens.contains("e'"));
        assertTrue(resultTokens.contains("dell"));
        assertTrue(resultTokens.contains("animo"));
    }

    @Test
    void testReadSourceWords_SourceNotFoundException() {
        Source source = new Source(1, Paths.get("percorso_fittizio_inesistente.txt"));

        assertThrows(SourceNotFoundException.class, () -> {
            ioManager.readSourceWords(source);
        });
    }

    @Test
    void testReadSourceMapFrequency_Success() throws Exception {
        Source source = new Source(42, Paths.get("dummy.txt"));
        fakeWordDao.insert(new Word("casa", 10, 42));
        fakeWordDao.insert(new Word("albero", 5, 42));
        fakeWordDao.insert(new Word("intruso", 1, 99));

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
        assertThrows(FrequencyMapNotFoundException.class, () -> {
            ioManager.readSourceMapFrequency(source);
        });
    }

    @Test
    void testWriteSource_Success() throws Exception {
        Source source = new Source(10, Paths.get("dummy.txt"));
        ioManager.writeSource(source);
        assertEquals(1, fakeSourceDao.database.size());
        assertTrue(fakeSourceDao.database.containsValue(source));
    }

    @Test
    void testWriteSource_StorageException() {
        Source source = new Source(10, Paths.get("dummy.txt"));
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

        assertEquals(2, fakeWordDao.database.size());
        assertTrue(fakeWordDao.database.values().stream().anyMatch(w -> w.getToken().equals("test") && w.getFrequency() == 3));
        assertTrue(fakeWordDao.database.values().stream().anyMatch(w -> w.getToken().equals("junit") && w.getFrequency() == 1));
    }

    @Test
    void testDeleteSource_Success() throws Exception {
        Source source = new Source(100, Paths.get("dummy.txt"));
        fakeSourceDao.insert(source);
        ioManager.deleteSource(source);
        assertTrue(fakeSourceDao.database.isEmpty());
    }

    @Test
    void testGetEstimatedWordCount_Success(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("stima.txt");
        String content = "1234567 8901234 56789"; 
        Files.write(testFile, content.getBytes());
        Source source = new Source(10, testFile);
        long estimated = ioManager.getEstimatedWordCount(source);
        assertEquals(3, estimated);
    }

    @Test
    void testGetEstimatedWordCount_EmptyFile(@TempDir Path tempDir) throws Exception {
        Path emptyFile = tempDir.resolve("vuoto.txt");
        Files.createFile(emptyFile);
        Source source = new Source(11, emptyFile);
        long estimated = ioManager.getEstimatedWordCount(source);
        assertEquals(1, estimated);
    }

    @Test
    void testGetEstimatedWordCount_SourceNotFoundExceptions() {
        assertThrows(SourceNotFoundException.class, () -> { ioManager.getEstimatedWordCount(null); });
        Source sourceNullPath = new Source(12, null);
        assertThrows(SourceNotFoundException.class, () -> { ioManager.getEstimatedWordCount(sourceNullPath); });
        Source sourceNotExists = new Source(13, Paths.get("ghost.txt"));
        assertThrows(SourceNotFoundException.class, () -> { ioManager.getEstimatedWordCount(sourceNotExists); });
    }

    // --- CLASSI FAKE INTERNE AL TEST ---

    private static class FakeSourceDAO implements SourceDAO {
        public final Map<Integer, Source> database = new HashMap<>();
        public boolean shouldFailOnInsert = false;
        
        @Override 
        public Optional<Source> selectById(Optional<Integer> id) { 
            return id.flatMap(i -> Optional.ofNullable(database.get(i))); 
        }
        
        @Override 
        public List<Source> selectAll() { 
            return new ArrayList<>(database.values()); 
        }
        
        @Override 
        public void insert(Source item) { 
            if (shouldFailOnInsert) throw new RuntimeException("DB Error"); 
            database.put(item.getId(), item); 
        }
        
        @Override 
        public void insertAll(List<Source> items) { 
            items.forEach(this::insert); 
        }
        
        @Override 
        public void update(Source model) { 
            database.put(model.getId(), model); 
        }
        
        @Override 
        public void delete(Optional<Integer> id) { 
            id.ifPresent(database::remove); 
        }
    }

    private static class FakeWordDAO implements WordDAO {
        private static class WordId {
            private final String token; 
            private final int source;
            
            public WordId(String token, int source) { 
                this.token = token; 
                this.source = source; 
            }
            
            @Override 
            public int hashCode() { 
                return Objects.hash(token, source); 
            }
            
            @Override 
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (obj == null || getClass() != obj.getClass()) return false;
                WordId other = (WordId) obj;
                return source == other.source && Objects.equals(token, other.token);
            }
        }
        
        public final Map<WordId, Word> database = new HashMap<>();

        @Override 
        public Optional<Word> selectById(Optional<String> token, Optional<Integer> source) {
            if (token.isPresent() && source.isPresent()) {
                return Optional.ofNullable(database.get(new WordId(token.get(), source.get())));
            }
            return Optional.empty();
        }
        
        @Override 
        public List<Word> selectAll() { 
            return new ArrayList<>(database.values()); 
        }
        
        @Override 
        public List<Word> selectAllWhere(Optional<String> token, Optional<Integer> frequenza, Optional<Integer> source) {
            return database.values().stream()
                    .filter(w -> token.map(t -> t.equals(w.getToken())).orElse(true))
                    .filter(w -> frequenza.map(f -> f.equals(w.getFrequency())).orElse(true))
                    .filter(w -> source.map(s -> s.equals(w.getSource())).orElse(true))
                    .collect(Collectors.toList());
        }
        
        @Override 
        public void insert(Word item) { 
            database.put(new WordId(item.getToken(), item.getSource()), item); 
        }
        
        @Override 
        public void insertAll(List<Word> items) { 
            items.forEach(this::insert); 
        }
        
        @Override 
        public void update(Word model) { 
            database.put(new WordId(model.getToken(), model.getSource()), model); 
        }
        
        @Override 
        public void delete(Optional<String> token, Optional<Integer> source) {
            if (token.isPresent() && source.isPresent()) {
                database.remove(new WordId(token.get(), source.get()));
            }
        }
    }
}