/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.internal.generation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.function.BiPredicate;

import gruppo05.gtwserver.sourcemanager.api.config.PresetConfig;
import gruppo05.gtwserver.sourcemanager.exception.QuestionGenerationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Classe di test JUnit 5 dedicata alla verifica dell'algoritmo iterativo di WordExtractor.
 */
public class WordExtractorTest {

    private WordExtractor extractor;
    private Set<String> stopWords;
    private Map<String, Integer> wordFrequencies;
    private PresetConfig dummyConfig;
    private Random fixedRandom;

    @BeforeEach
    public void setUp() {
        stopWords = new HashSet<>(Arrays.asList("il", "lo", "la", "i", "gli", "le", "di", "a", "da", "in", "con", "su", "per", "tra", "fra", "un", "una"));
        wordFrequencies = new HashMap<>();
        
        fixedRandom = new Random(42);

        // Due parole sono simili se iniziano con la stessa lettera
        BiPredicate<String, String> simFunc = (s1, s2) -> s1.toLowerCase().charAt(0) == s2.toLowerCase().charAt(0);
        
        // Il fallback premia chi ha la frequenza minore
        BiPredicate<Integer, Integer> fallbackCrit = (freqOld, freqNew) -> freqNew < freqOld;

        extractor = new WordExtractor(simFunc, fallbackCrit, stopWords, fixedRandom);

        // Usiamo il Builder reale.
        // Soglia max frequenza = 10, Soglia parole simili < 2
        dummyConfig = new PresetConfig.Builder()
                .withNumberOfPeriods(5)
                .withMaximumWordFrequency(10)
                .withMaximumSimilarWordInQuestionText(2)
                .withShiftingOffset(1)
                .build();
    }

    @Test
    public void testExtractWordFindsGoodWordDirectly() throws QuestionGenerationException {
        String text = "Il tavolo e una sedia.";
        
        wordFrequencies.put("tavolo", 5);
        wordFrequencies.put("sedia", 3);
        wordFrequencies.put("e", 50);

        String result = extractor.extractWord(text, wordFrequencies, dummyConfig);

        assertTrue(result.equals("tavolo") || result.equals("sedia"));
    }

    @Test
    public void testExtractWordDiscardsBadWordAndFindsGoodOne() throws QuestionGenerationException {
        String text = "Il cane vede la casa e una cosa vicino un albero.";
        
        // Parole target con frequenza corretta
        wordFrequencies.put("cane", 2);
        wordFrequencies.put("casa", 2);
        wordFrequencies.put("cosa", 2);
        wordFrequencies.put("albero", 2);

        // FIX DEL TEST: "vede", "e", "vicino" non sono stop-words. 
        // Le impostiamo con una frequenza > 10 in modo che il filtro iniziale le scarti, 
        // altrimenti avrebbero frequenza di default 0 e sarebbero candidabili!
        wordFrequencies.put("vede", 50);
        wordFrequencies.put("e", 50);
        wordFrequencies.put("vicino", 50);

        String result = extractor.extractWord(text, wordFrequencies, dummyConfig);

        assertEquals("albero", result);
    }

    @Test
    public void testExtractWordExhaustsTextAndReturnsFallback() throws QuestionGenerationException {
        // FIX DEL TEST: Creiamo 3 gruppi di parole simili distinti.
        // In questo modo, l'algoritmo non svuoterà il testo al primo giro,
        // ma farà 3 iterazioni separate, permettendo al Fallback di scegliere il "meno peggio".
        String text = "Il mare mela muro pane palla ponte sole sale sasso.";
        
        // Gruppo M (frequenza alta, fallirà per similarità)
        wordFrequencies.put("mare", 8); wordFrequencies.put("mela", 8); wordFrequencies.put("muro", 8);
        
        // Gruppo P (frequenza media, fallirà per similarità)
        wordFrequencies.put("pane", 5); wordFrequencies.put("palla", 5); wordFrequencies.put("ponte", 5);
        
        // Gruppo S (frequenza minima, fallirà per similarità)
        wordFrequencies.put("sole", 2); wordFrequencies.put("sale", 2); wordFrequencies.put("sasso", 2);

        // Quando lo stralcio sarà esaurito, il ripescaggio (che premia freqNew < freqOld) 
        // dovrà obbligatoriamente aver salvato una delle parole del Gruppo S (frequenza 2).
        String result = extractor.extractWord(text, wordFrequencies, dummyConfig);

        assertTrue(result.equals("sole") || result.equals("sale") || result.equals("sasso"));
    }

    @Test
    public void testExtractWordThrowsExceptionWhenNoWordsAvailableAtAll() {
        String text = "Il lo la . un a di .";
        
        assertThrows(QuestionGenerationException.class, () -> {
            extractor.extractWord(text, wordFrequencies, dummyConfig);
        });
    }
}