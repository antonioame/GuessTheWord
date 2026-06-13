/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.api.config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.model.Word;
import gruppo05.gtwserver.db.DAO;
import gruppo05.gtwserver.db.SourceDAO;
import gruppo05.gtwserver.db.WordDAO;
import java.util.Optional;

public class SourceManagerConfigTest {

    private SourceDAO mockSourceDao;
    private WordDAO mockWordDao;
    private BiPredicate<String, String> dummySimFunc;
    private BiPredicate<Integer, Integer> dummyFallbackCrit;
    private PresetConfig dummyPreset;

    @BeforeEach
    public void setUp() {
        // Creazione di implementazioni stub minimali per i DAO compatibili con la nuova interfaccia
        mockSourceDao = new SourceDAO() {
            @Override 
            public Optional<Source> selectById(Optional<Integer> id) { 
                return Optional.empty(); 
            }
            
            @Override 
            public List<Source> selectAll() { 
                return Collections.emptyList(); 
            }
            
            @Override 
            public void insert(Source model) {}
            
            @Override 
            public void insertAll(List<Source> modelList) {}
            
            @Override 
            public void update(Source model) {}
            
            @Override 
            public void delete(Optional<Integer> id) {}
        };

        mockWordDao = new WordDAO() {
            @Override 
            public Optional<Word> selectById(Optional<String> token, Optional<Integer> source) { 
                return Optional.empty(); 
            }
            
            @Override 
            public List<Word> selectAll() { 
                return Collections.emptyList(); 
            }

            @Override
            public List<Word> selectAllWhere(Optional<String> token, Optional<Integer> frequenza, Optional<Integer> source) {
                return Collections.emptyList();
            }
            
            @Override 
            public void insert(Word model) {}
            
            @Override 
            public void insertAll(List<Word> modelList) {}
            
            @Override 
            public void update(Word model) {}
            
            @Override 
            public void delete(Optional<String> token, Optional<Integer> source) {}
        };

        dummySimFunc = (s1, s2) -> s1.equals(s2);
        dummyFallbackCrit = (i1, i2) -> i1 > i2;
        
        // Mocking minimale o instanziazione di un PresetConfig fittizio
        dummyPreset = new PresetConfig.Builder()
                .withNumberOfPeriods(3)
                .withMaximumWordFrequency(10)
                .withMaximumSimilarWordInQuestionText(2)
                .withShiftingOffset(1)
                .build();
    }

    @Test
    public void testBuilderSuccessAndGetters() {
        // Inizializzazione Set compatibile con Java 8
        Set<String> customStops = new HashSet<>(Arrays.asList("il", "la", "di"));

        SourceManagerConfig config = new SourceManagerConfig.Builder(mockSourceDao, mockWordDao, dummySimFunc, dummyFallbackCrit)
                .addPreset("default_preset", dummyPreset)
                .withCustomStopWords(customStops)
                .build();

        assertNotNull(config);
        assertEquals(mockSourceDao, config.getSourceDao());
        assertEquals(mockWordDao, config.getWordDao());
        assertEquals(dummySimFunc, config.getSimilarityFunction());
        assertEquals(dummyFallbackCrit, config.getFallbackWordCriterion());
        
        Map<String, PresetConfig> presets = config.getPresets();
        assertTrue(presets.containsKey("default_preset"));
        assertEquals(dummyPreset, presets.get("default_preset"));

        Set<String> stopWords = config.getStopWords();
        assertEquals(3, stopWords.size());
        assertTrue(stopWords.contains("il"));
    }

    @Test
    public void testBuilderConstructorThrowsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SourceManagerConfig.Builder(null, mockWordDao, dummySimFunc, dummyFallbackCrit);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new SourceManagerConfig.Builder(mockSourceDao, null, dummySimFunc, dummyFallbackCrit);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new SourceManagerConfig.Builder(mockSourceDao, mockWordDao, null, dummyFallbackCrit);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new SourceManagerConfig.Builder(mockSourceDao, mockWordDao, dummySimFunc, null);
        });
    }

    @Test
    public void testAddPresetThrowsOnNullParameters() {
        SourceManagerConfig.Builder builder = new SourceManagerConfig.Builder(mockSourceDao, mockWordDao, dummySimFunc, dummyFallbackCrit);

        assertThrows(IllegalArgumentException.class, () -> {
            builder.addPreset(null, dummyPreset);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            builder.addPreset("test", null);
        });
    }

    @Test
    public void testWithCustomStopWordsThrowsOnNull() {
        SourceManagerConfig.Builder builder = new SourceManagerConfig.Builder(mockSourceDao, mockWordDao, dummySimFunc, dummyFallbackCrit);

        assertThrows(IllegalArgumentException.class, () -> {
            builder.withCustomStopWords(null);
        });
    }

    @Test
    public void testCollectionsImmutability() {
        // Inizializzazione Set compatibile con Java 8
        Set<String> stopWordsSet = new HashSet<>(Arrays.asList("stop"));
        
        SourceManagerConfig config = new SourceManagerConfig.Builder(mockSourceDao, mockWordDao, dummySimFunc, dummyFallbackCrit)
                .addPreset("p1", dummyPreset)
                .withCustomStopWords(stopWordsSet)
                .build();

        // Verifica che la mappa dei preset non sia modificabile dall'esterno
        assertThrows(UnsupportedOperationException.class, () -> {
            config.getPresets().put("p2", dummyPreset);
        });

        // Verifica che il set delle stop words non sia modificabile dall'esterno
        assertThrows(UnsupportedOperationException.class, () -> {
            config.getStopWords().add("new_stop");
        });
    }
}