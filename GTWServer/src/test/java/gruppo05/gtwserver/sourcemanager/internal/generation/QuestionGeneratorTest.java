/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.internal.generation;

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
    
    // Regola di sanificazione per il test
    private static final String TEST_REGEX = "[^a-zA-Z0-9àèìòùáéíóúÀÈÌÒÙÁÉÍÓÚ]";

    /**
     * @brief Estensione mock manuale di Random per forzare i risultati desiderati nel test.
     */
    private static class ControlledRandom extends Random {
        // Usiamo un double perché la nuova logica di skip usa random.nextDouble()
        private double forcedDouble = 0.0;

        public void setForcedDouble(double value) {
            this.forcedDouble = value;
        }

        @Override
        public double nextDouble() {
            return forcedDouble;
        }
    }

    /**
     * @brief Stub manuale di WordExtractor per simulare il comportamento di estrazione.
     */
    private static class StubWordExtractor extends WordExtractor {
        private String fixedWord = "chiave";

        public StubWordExtractor() {
            super((s1, s2) -> true, (i1, i2) -> true, Collections.emptySet(), new Random(), TEST_REGEX);
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
        PresetConfig config = new PresetConfig.Builder()
                .withNumberOfPeriods(1)
                .withShiftingOffset(3)
                .build();

        // FIX: Mettiamo la parola bersaglio nella prima frase
        Stream<String> sourceStream = Stream.of(
                "La chiave funziona perfettamente.", 
                "Questa è una frase in più che verrà ignorata."
        );
        
        controlledRandom.setForcedDouble(0.0);
        stubWordExtractor.setFixedWord("chiave");

        Question result = questionGenerator.generateQuestion(sourceStream, dummyFrequencies, config, 10L);

        assertNotNull(result);
        assertEquals("chiave", result.getAnswer());
        
        assertTrue(result.getText().contains("fkldyh"), "Il testo dovrebbe contenere la parola cifrata 'fkldyh'");
        
        // FIX: Ora ci aspettiamo solo la prima riga cifrata!
        assertEquals("La fkldyh funziona perfettamente.", result.getText(), "Avendo chiesto 1 solo periodo, si ferma al primo punto.");
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
            // Passiamo 0 come stima
            questionGenerator.generateQuestion(emptyStream, dummyFrequencies, config, 0L);
        }, "Dovrebbe lanciare QuestionGenerationException per uno stream vuoto");
    }

    /**
     * @brief Verifica che la cifratura rispetti la sensibilità alle maiuscole/minuscole (Case Sensitive).
     */
    @Test
    public void testGenerateQuestionCaseSensitiveEncryption() throws QuestionGenerationException {
        PresetConfig config = new PresetConfig.Builder()
                .withNumberOfPeriods(1)
                .withShiftingOffset(1) // Shift di 1
                .build();

        // FIX: Passiamo la riga intera
        Stream<String> sourceStream = Stream.of("Un test chiamato Test.");
        
        controlledRandom.setForcedDouble(0.0);
        stubWordExtractor.setFixedWord("test");

        Question result = questionGenerator.generateQuestion(sourceStream, dummyFrequencies, config, 5L);

        // Dovrebbe cifrare solo "test" (minuscolo) in "uftu", lasciando invariato "Test" (maiuscolo)
        assertTrue(result.getText().contains("uftu"));
        assertTrue(result.getText().contains("Test"));
        assertFalse(result.getText().contains("Uftu"));
    }
}