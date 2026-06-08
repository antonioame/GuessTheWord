/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.api;

/**
 *
 * @author Hermann
 */
import gruppo05.gtwserver.db.DAO;
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
import java.util.function.Function;

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
     * @brief Finto DAO per conservare le entità in memoria durante i test.
     * @param <T> Il tipo dell'entità gestita.
     * @param <K> Il tipo della chiave primaria dell'entità.
     */
    private static class FakeDAO<T> implements DAO<T> {
        private final Map<K, T> storage = new HashMap<>();
        private final Function<T, K> idExtractor;

        public FakeDAO(Function<T, K> idExtractor) {
            this.idExtractor = idExtractor;
        }

        @Override
        public Optional<T> selectById(K modelId) {
            return Optional.ofNullable(storage.get(modelId));
        }

        @Override
        public List<T> selectAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public void insert(T item) {
            storage.put(idExtractor.apply(item), item);
        }

        @Override
        public void insertAll(List<T> items) {
            for (T item : items) {
                insert(item);
            }
        }

        @Override
        public void update(T item) {
            storage.put(idExtractor.apply(item), item);
        }

        @Override
        public void delete(K modelId) {
            storage.remove(modelId);
        }
    }

    private FakeDAO<Source, SourceId> sourceDao;
    private FakeDAO<Word, WordId> wordDao;
    private BasicSourceManager sourceManager;
    private Source dummySource;

    /**
     * @brief Inizializza l'ambiente di test, le configurazioni ed il manager.
     */
    @BeforeEach
    public void setUp() throws IOException {
        // Passiamo i reference ai metodi getter degli ID
        sourceDao = new FakeDAO<>(Source::getId);
        wordDao = new FakeDAO<>(Word::getId);

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
        
        // Verifica tramite selectById anziché selectAllWhere
        assertTrue(sourceDao.selectById(dummySource.getId()).isPresent(), "La sorgente non è stata inserita nel DAO.");
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
                // Delete ora usa l'ID e non l'oggetto
                sourceDao.delete(dummySource.getId());
                latch.countDown();
            },
            e -> {
                errorRef.set(e);
                latch.countDown();
            }
        );

        latch.await(2, TimeUnit.SECONDS);

        assertNull(errorRef.get(), "Si è verificata un'eccezione durante la rimozione della sorgente.");
        
        // Verifica l'assenza tramite selectById
        assertFalse(sourceDao.selectById(dummySource.getId()).isPresent(), "La sorgente non è stata rimossa correttamente dal DAO.");
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