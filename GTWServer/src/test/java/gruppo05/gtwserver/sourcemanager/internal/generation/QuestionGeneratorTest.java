/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.internal.generation;

/**
 *
 * @author Hermann
 */
import gruppo05.gtwserver.model.Question;
import gruppo05.gtwserver.sourcemanager.api.config.PresetConfig;
import gruppo05.gtwserver.sourcemanager.exception.QuestionGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief Suite di test unitari JUnit 5 per la classe QuestionGenerator.
 */
public class QuestionGeneratorTest {

    private QuestionGenerator questionGenerator;
    private StubWordExtractor stubWordExtractor;
    private ControlledRandom controlledRandom;
    private Map<String, Integer> dummyFrequencies;

    /**
     * @brief Estensione mock manuale di Random per forzare gli indici desiderati nel test.
     */
    private static class ControlledRandom extends Random {
        private int forcedIndex = 0;

        public void setForcedIndex(int index) {
            this.forcedIndex = index;
        }

        @Override
        public int nextInt(int bound) {
            // Ritorna l'indice forzato a meno che non superi il bound superiore dello stream
            return forcedIndex < bound ? forcedIndex : bound - 1;
        }
    }

    /**
     * @brief Stub manuale di WordExtractor per simulare il comportamento di estrazione senza Mockito.
     */
    private static class StubWordExtractor extends WordExtractor {
        private String fixedWord = "chiave";

        public StubWordExtractor() {
            // Invocazione del costruttore padre con parametri dummy/predicati sempre veri
            super((s1, s2) -> true, (i1, i2) -> true, Collections.emptySet(), new Random());
        }

        public void setFixedWord(String word) {
            this.fixedWord = word;
        }

        @Override
        public String extractWord(String text, Map<String, Integer> wordFrequencies, PresetConfig config) {
            return this.fixedWord;
        }
    }

    @BeforeEach
    public void setUp() {
        controlledRandom = new ControlledRandom();
        stubWordExtractor = new StubWordExtractor();
        questionGenerator = new QuestionGenerator(stubWordExtractor, controlledRandom);
        dummyFrequencies = new HashMap<>();
    }

    /**
     * @brief Verifica il successo della generazione della domanda con corretta estrazione e cifratura.
     */
    @Test
    public void testGenerateQuestionSuccess() throws QuestionGenerationException {
        // Configurazione: 1 periodo richiesto, offset di cifratura pari a 3
        PresetConfig config = new PresetConfig.Builder()
                .withNumberOfPeriods(1)
                .withShiftingOffset(3)
                .build();

        // Stream strutturato: Frase 1. Frase 2 contenente la parola target.
        Stream<String> sourceStream = Stream.of("Prima", "frase", ".", "La", "chiave", "funziona", ".");
        
        // Forziamo il random ad atterrare sull'indice 0; l'algoritmo cercherà il primo punto 
        // all'indice 2, facendo partire il testo estratto dall'indice 3 ("La chiave funziona .")
        controlledRandom.setForcedIndex(0);
        stubWordExtractor.setFixedWord("chiave");

        Question result = questionGenerator.generateQuestion(sourceStream, dummyFrequencies, config);

        assertNotNull(result);
        assertEquals("chiave", result.getAnswer());
        
        // Verifica cifratura della parola "chiave" con offset 3:
        // c->f, h->k, i->l, a->d, v->y, e->h => "fkldyh"
        assertTrue(result.getText().contains("fkldyh"), "Il testo dovrebbe contenere la parola cifrata 'fkldyh'");
        assertFalse(result.getText().contains("chiave"), "Il testo non dovrebbe più contenere la parola in chiaro");
        assertEquals("La fkldyh funziona.", result.getText());
    }

    /**
     * @brief Verifica il corretto lancio dell'eccezione in caso di stream sorgente vuoto.
     */
    @Test
    public void testGenerateQuestionWithEmptySource() {
        PresetConfig config = new PresetConfig.Builder()
                .withNumberOfPeriods(1)
                .withShiftingOffset(1)
                .build();

        Stream<String> emptyStream = Stream.empty();

        assertThrows(QuestionGenerationException.class, () -> {
            questionGenerator.generateQuestion(emptyStream, dummyFrequencies, config);
        }, "Dovrebbe lanciare QuestionGenerationException per uno stream vuoto");
    }

    /**
     * @brief Verifica che la cifratura rispetti la sensibilità alle maiuscole/minuscole (Case Sensitive).
     */
    @Test
    public void testGenerateQuestionCaseSensitiveEncryption() throws QuestionGenerationException {
        PresetConfig config = new PresetConfig.Builder()
                .withNumberOfPeriods(1)
                .withShiftingOffset(1) // Shift di 1: 'Test' -> 'Uftu', 'test' -> 'uftu'
                .build();

        Stream<String> sourceStream = Stream.of("Un", "test", "chiamato", "Test", ".");
        controlledRandom.setForcedIndex(0);
        
        // Chiediamo allo stub di estrarre la versione minuscola "test"
        stubWordExtractor.setFixedWord("test");

        Question result = questionGenerator.generateQuestion(sourceStream, dummyFrequencies, config);

        // Dovrebbe cifrare solo "test" in "uftu", lasciando invariato "Test"
        assertTrue(result.getText().contains("uftu"));
        assertTrue(result.getText().contains("Test"));
        assertFalse(result.getText().contains("Uftu"));
    }
}