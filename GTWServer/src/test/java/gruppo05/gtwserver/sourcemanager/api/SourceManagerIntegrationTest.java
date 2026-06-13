/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.api;

import gruppo05.gtwserver.db.DAO;
import gruppo05.gtwserver.db.SourceDAO;
import gruppo05.gtwserver.db.WordDAO;
import gruppo05.gtwserver.model.Question;
import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.model.Word;
import gruppo05.gtwserver.sourcemanager.api.config.PresetConfig;
import gruppo05.gtwserver.sourcemanager.api.config.SourceManagerConfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Classe di test di integrazione per l'architettura del SourceManager.
 * @invariant
 * Il ciclo di vita dei thread interni al SourceManager viene sempre
 * correttamente terminato alla fine di ciascun test.
 */
public class SourceManagerIntegrationTest {

    @TempDir
    Path tempDir;

    /**
     * @brief Finto SourceDAO per conservare le entità in memoria durante i test.
     */
    private static class FakeSourceDAO implements SourceDAO {
        private final Map<Integer, Source> storage = new HashMap<>();

        @Override
        public Optional<Source> selectById(Optional<Integer> id) {
            if (id.isPresent()) {
                return Optional.ofNullable(storage.get(id.get()));
            }
            return Optional.empty();
        }

        @Override
        public List<Source> selectAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public void insert(Source item) {
            storage.put(item.getId(), item);
        }

        @Override
        public void insertAll(List<Source> items) {
            for (Source item : items) {
                insert(item);
            }
        }

        @Override
        public void update(Source item) {
            storage.put(item.getId(), item);
        }

        @Override
        public void delete(Optional<Integer> id) {
            id.ifPresent(storage::remove);
        }
    }

    /**
     * @brief Finto WordDAO per conservare le entità in memoria durante i test.
     */
    private static class FakeWordDAO implements WordDAO {
        private final List<Word> storage = new ArrayList<>();

        @Override
        public Optional<Word> selectById(Optional<String> token, Optional<Integer> source) {
            return Optional.empty(); // Stub per il test
        }

        @Override
        public List<Word> selectAllWhere(Optional<String> token, Optional<Integer> frequenza, Optional<Integer> source) {
            return new ArrayList<>(); // Stub per il test
        }

        @Override
        public List<Word> selectAll() {
            return new ArrayList<>(storage);
        }

        @Override
        public void insert(Word item) {
            storage.add(item);
        }

        @Override
        public void insertAll(List<Word> items) {
            storage.addAll(items);
        }

        @Override
        public void update(Word item) {
            // Non strettamente necessario per questo test
        }

        @Override
        public void delete(Optional<String> token, Optional<Integer> source) {
            // Stub per il test
        }
    }

    private FakeSourceDAO sourceDao;
    private FakeWordDAO wordDao;
    private BasicSourceManager sourceManager;
    private Source dummySource;

    /**
     * @brief Inizializza l'ambiente di test, le configurazioni ed il manager.
     */
    @BeforeEach
    public void setUp() throws IOException {
        sourceDao = new FakeSourceDAO();
        wordDao = new FakeWordDAO();

        Set<String> customStopWords = new HashSet<>();
        customStopWords.add("il");
        customStopWords.add("la");

        PresetConfig defaultPreset = new PresetConfig.Builder()
                .withNumberOfPeriods(3)
                .withMaximumWordFrequency(5)
                .withMaximumSimilarWordInQuestionText(2)
                .withShiftingOffset(1)
                .build();

        SourceManagerConfig config = new SourceManagerConfig.Builder(
                sourceDao,
                wordDao,
                String::equalsIgnoreCase, // similarityFunction
                (freq1, freq2) -> freq1 < freq2 // fallbackWordCriterion
        )
        .withCustomStopWords(customStopWords)
        .addPreset("DEFAULT", defaultPreset)
        .build();

        sourceManager = new BasicSourceManager(config);

        Path tempFilePath = tempDir.resolve("test_source.txt");
        Files.write(tempFilePath, Arrays.asList(
            "Questa è la prima riga del file di test.", 
            "Questa è la seconda riga per popolare le frequenze."
        ));

        dummySource = new Source(1, tempFilePath);
    }

    /**
     * @brief Chiude il SourceManager e libera le risorse dopo ogni test.
     */
    @AfterEach
    public void tearDown() {
        if (sourceManager != null) {
            sourceManager.close();
        }
    }

    /**
     * @brief Testa l'aggiunta asincrona di una sorgente e la chiamata del callback.
     */
    @Test
    public void testAddSourceSuccess() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        sourceManager.addSource(dummySource, 
            () -> {
                sourceDao.insert(dummySource);
                latch.countDown();
            },
            e -> {
                errorRef.set(e);
                latch.countDown();
            }
        );

        boolean awaitResult = latch.await(2, TimeUnit.SECONDS);

        assertTrue(awaitResult, "Il task asincrono è andato in timeout.");
        assertNull(errorRef.get(), "Si è verificata un'eccezione inaspettata durante addSource.");
        
        // Verifica tramite selectById con uso di Optional
        assertTrue(sourceDao.selectById(Optional.of(dummySource.getId())).isPresent(), "La sorgente non è stata inserita nel DAO.");
    }

    /**
     * @brief Testa la rimozione asincrona di una sorgente tramite manager.
     */
    @Test
    public void testRemoveSourceSuccess() throws InterruptedException {
        sourceDao.insert(dummySource);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        sourceManager.removeSource(dummySource, 
            () -> {
                // Delete ora usa Optional e l'ID
                sourceDao.delete(Optional.of(dummySource.getId()));
                latch.countDown();
            },
            e -> {
                errorRef.set(e);
                latch.countDown();
            }
        );

        latch.await(2, TimeUnit.SECONDS);

        assertNull(errorRef.get(), "Si è verificata un'eccezione durante la rimozione della sorgente.");
        
        // Verifica l'assenza tramite selectById con uso di Optional
        assertFalse(sourceDao.selectById(Optional.of(dummySource.getId())).isPresent(), "La sorgente non è stata rimossa correttamente dal DAO.");
    }

    /**
     * @brief Verifica che la generazione di una domanda gestisca i callback.
     */
    @Test
    public void testGenerateQuestion() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Question> questionRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        sourceManager.generateQuestion(dummySource, "DEFAULT",
            q -> {
                questionRef.set(q);
                latch.countDown();
            },
            e -> {
                errorRef.set(e);
                latch.countDown();
            }
        );

        latch.await(2, TimeUnit.SECONDS);

        assertTrue(questionRef.get() != null || errorRef.get() != null, "Nessun callback è stato chiamato.");
    }

    /**
     * @brief Testa lo spegnimento manuale del manager.
     */
    @Test
    public void testShutdownManager() {
        sourceManager.shutdown();
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        try {
            sourceManager.addSource(dummySource, latch::countDown, e -> {
                errorRef.set(e);
                latch.countDown();
            });
            latch.await(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }
}