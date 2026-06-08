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
        assertTrue(fakeWordDao.database.values().stream().anyMatch(w -> w.getToken().equals("test") && w.getFrequency() == 3));
        assertTrue(fakeWordDao.database.values().stream().anyMatch(w -> w.getToken().equals("junit") && w.getFrequency() == 1));
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

    @Test
    void testGetEstimatedWordCount_Success(@TempDir Path tempDir) throws Exception {
        // Prepariamo un file con esattamente 21 byte (3 parole stimate: 21 / 7 = 3)
        Path testFile = tempDir.resolve("stima.txt");
        String content = "1234567 8901234 56789"; // 21 caratteri ASCII = 21 byte
        Files.write(testFile, content.getBytes());

        Source source = new Source(10, testFile);

        long estimated = ioManager.getEstimatedWordCount(source);

        // Verifichiamo che il calcolo rispetti l'euristica (byte / 7)
        assertEquals(3, estimated);
    }

    @Test
    void testGetEstimatedWordCount_EmptyFile(@TempDir Path tempDir) throws Exception {
        // Prepariamo un file completamente vuoto (0 byte)
        Path emptyFile = tempDir.resolve("vuoto.txt");
        Files.createFile(emptyFile);

        Source source = new Source(11, emptyFile);

        long estimated = ioManager.getEstimatedWordCount(source);

        // Verifichiamo che il limite inferiore di sicurezza (Math.max(1, ...)) funzioni
        assertEquals(1, estimated);
    }

    @Test
    void testGetEstimatedWordCount_SourceNotFoundExceptions() {
        // Test 1: Source null
        assertThrows(SourceNotFoundException.class, () -> {
            ioManager.getEstimatedWordCount(null);
        });

        // Test 2: Source con Path null
        Source sourceNullPath = new Source(12, null);
        assertThrows(SourceNotFoundException.class, () -> {
            ioManager.getEstimatedWordCount(sourceNullPath);
        });

        // Test 3: Source con Path inesistente
        Source sourceNotExists = new Source(13, Paths.get("file_inesistente_ghost.txt"));
        assertThrows(SourceNotFoundException.class, () -> {
            ioManager.getEstimatedWordCount(sourceNotExists);
        });
    }

    // --- CLASSI FAKE INTERNE AL TEST ---

    /**
     * Implementazione In-Memory del database per i Source.
     * La chiave primaria (K) è il tipo custom SourceId.
     */
    private static class FakeSourceDAO implements SourceDAO {
        // Usiamo una Map per rendere coerente e sicura la ricerca per ID
        public final Map<Integer, Source> database = new HashMap<>();
        public boolean shouldFailOnInsert = false;

        @Override
        public Optional<Source> selectById(Optional<Integer> id) {
            return Optional.ofNullable(database.get(id));
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
        public void delete(Optional<Integer> id) {
            database.remove(id);
        }
    }

    /**
     * Implementazione In-Memory del database per le Word.
     * La chiave primaria (K) è il tipo custom WordId.
     */
    private static class FakeWordDAO implements WordDAO {
        
        private static class WordId {
            private String token;
            private int source;

            public WordId(String token, int source) {
                this.token = token;
                this.source = source;
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 71 * hash + Objects.hashCode(this.token);
                hash = 71 * hash + this.source;
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final WordId other = (WordId) obj;
                if (this.source != other.source) {
                    return false;
                }
                if (!Objects.equals(this.token, other.token)) {
                    return false;
                }
                return true;
            }
            
            
        }
        
        
        public final Map<WordId, Word> database = new HashMap<>();

        @Override
        public Optional<Word> selectById(Optional<String> token, Optional<Integer> source) {
            if(token.isPresent() && source.isPresent())
                return Optional.ofNullable(database.get(new WordId(token.get(), source.get())));
            return Optional.empty();
        }

        @Override
        public List<Word> selectAll() {
            return new ArrayList<>(database.values());
        }

        @Override
        public List<Word> selectAllWhere(Optional<String> token, Optional<Integer> frequenza, Optional<Integer> source) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void insert(Word item) {
            database.put(new WordId(item.getToken(), item.getSource()), item);
        }

        @Override
        public void insertAll(List<Word> items) {
            for (Word item : items) {
                insert(item);
            }
        }

        @Override
        public void update(Word model) {
            database.put(new WordId(model.getToken(), model.getSource()), model);
        }

        @Override
        public void delete(Optional<String> token, Optional<Integer> source) {
            if(token.isPresent() && source.isPresent())
                database.remove(new WordId(token.get(), source.get()));
        }
    }
}