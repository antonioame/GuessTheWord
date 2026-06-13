/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.api;

import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.model.Word;
import gruppo05.gtwserver.model.Question;
import gruppo05.gtwserver.db.DAO;
import gruppo05.gtwserver.db.SourceDAO;
import gruppo05.gtwserver.db.WordDAO;
import gruppo05.gtwserver.sourcemanager.api.config.SourceManagerConfig;
import gruppo05.gtwserver.sourcemanager.api.config.PresetConfig;
import gruppo05.gtwserver.sourcemanager.exception.PresetNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class BasicSourceManagerTest {

    private SourceManagerConfig config;
    private BasicSourceManager manager;

    // Implementazione Manuale Fake di DAO per Source
    private static class FakeSourceDAO implements SourceDAO {
        @Override 
        public Optional<Source> selectById(Optional<Integer> id) { 
            return Optional.empty(); 
        }

        @Override 
        public List<Source> selectAll() { 
            return new ArrayList<>(); 
        }

        @Override 
        public void insert(Source model) {}

        @Override 
        public void insertAll(List<Source> modelList) {}

        @Override 
        public void update(Source model) {}

        @Override 
        public void delete(Optional<Integer> id) {}
    }

    // Implementazione Manuale Fake di DAO per Word
    private static class FakeWordDAO implements WordDAO {
        @Override 
        public Optional<Word> selectById(Optional<String> token, Optional<Integer> source) { 
            return Optional.empty(); 
        }

        @Override 
        public List<Word> selectAll() { 
            return new ArrayList<>(); 
        }

        @Override
        public List<Word> selectAllWhere(Optional<String> token, Optional<Integer> frequenza, Optional<Integer> source) {
            return new ArrayList<>();
        }
        
        @Override 
        public void insert(Word model) {}

        @Override 
        public void insertAll(List<Word> modelList) {}

        @Override 
        public void update(Word model) {}

        @Override 
        public void delete(Optional<String> token, Optional<Integer> source) {}
    }

    @BeforeEach
    public void setUp() {
        SourceDAO sourceDao = new FakeSourceDAO();
        WordDAO wordDao = new FakeWordDAO();
        BiPredicate<String, String> similarityFunction = String::equals;
        BiPredicate<Integer, Integer> fallbackCriterion = (a, b) -> true;

        // Configurazione del builder del Preset
        PresetConfig.Builder presetBuilder = new PresetConfig.Builder();
        PresetConfig testPreset = presetBuilder
                .withNumberOfPeriods(2)
                .withMaximumWordFrequency(5)
                .withMaximumSimilarWordInQuestionText(3)
                .withShiftingOffset(2)
                .build();

        // Configurazione globale del manager
        SourceManagerConfig.Builder configBuilder = new SourceManagerConfig.Builder(
                sourceDao, wordDao, similarityFunction, fallbackCriterion
        );
        configBuilder.addPreset("defaultPreset", testPreset);
        configBuilder.withCustomStopWords(new HashSet<>(Collections.singletonList("e")));

        this.config = configBuilder.build();
        this.manager = new BasicSourceManager(config);
    }

    @Test
    public void testInitializationSuccess() {
        assertNotNull(manager, "Il BasicSourceManager dovrebbe essere istanziato correttamente.");
    }

    @Test
    public void testGenerateQuestionThrowsPresetNotFoundException() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Exception[] capturedException = new Exception[1];
        
        // Utilizziamo un'istanza fittizia di Source
        Path dummyPath = Paths.get("src/test/resources/dummy_source.txt");
        Source dummySource = new Source(1, dummyPath);

        manager.generateQuestion(
                dummySource, 
                "PresetInesistante", 
                question -> latch.countDown(), 
                exception -> {
                    capturedException[0] = exception;
                    latch.countDown();
                }
        );

        // Attendiamo il completamento asincrono sul pool di thread
        boolean completed = latch.await(2, TimeUnit.SECONDS);

        assertTrue(completed, "Il thread asincrono non ha risposto in tempo utile.");
        assertNotNull(capturedException[0], "Dovrebbe essere catturata un'eccezione.");
        assertTrue(capturedException[0] instanceof PresetNotFoundException, 
                "L'eccezione lanciata dovrebbe essere di tipo PresetNotFoundException.");
    }

    @Test
    public void testShutdownAndCloseBehavior() {
        assertDoesNotThrow(() -> {
            manager.shutdown();
            manager.close();
        }, "I metodi di chiusura non dovrebbero sollevare eccezioni spontanee.");
    }
}