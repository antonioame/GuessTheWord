/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.api.config;

/**
 *
 * @author Hermann
 */
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * @brief Classe di test JUnit5 per verificare la corretta implementazione di PresetConfig e del suo Builder.
 */
public class PresetConfigTest {

	/**
	 * @brief Verifica che il costruttore di default del Builder configuri correttamente i valori predefiniti.
	 * @post
	 * I valori di default devono corrispondere a quelli attesi e soddisfare le invarianti.
	 */
	@Test
	public void testDefaultBuilderValues() {
		PresetConfig config = new PresetConfig.Builder().build();
		
		assertEquals(1, config.getNumberOfPeriods());
		assertEquals(100, config.getMaximumWordFrequency());
		assertEquals(5, config.getMaximumSimilarWordInQuestionText());
		assertEquals(3, config.getShiftingOffset());
	}

	/**
	 * @brief Verifica la corretta configurazione di valori personalizzati e validi tramite il Builder.
	 * @post
	 * L'oggetto PresetConfig creato deve contenere esattamente i valori forniti tramite i metodi del Builder.
	 */
	@Test
	public void testCustomValidValues() {
		PresetConfig config = new PresetConfig.Builder()
				.withNumberOfPeriods(5)
				.withMaximumWordFrequency(250)
				.withMaximumSimilarWordInQuestionText(10)
				.withShiftingOffset(7)
				.build();
				
		assertEquals(5, config.getNumberOfPeriods());
		assertEquals(250, config.getMaximumWordFrequency());
		assertEquals(10, config.getMaximumSimilarWordInQuestionText());
		assertEquals(7, config.getShiftingOffset());
	}

	/**
	 * @brief Verifica che l'impostazione di un numero di periodi non valido sollevi un'eccezione.
	 * @post
	 * Viene lanciata un'eccezione di tipo IllegalArgumentException per valori minori o uguali a zero.
	 */
	@Test
	public void testInvalidNumberOfPeriods() {
		PresetConfig.Builder builder = new PresetConfig.Builder();
		
		assertThrows(IllegalArgumentException.class, () -> builder.withNumberOfPeriods(0));
		assertThrows(IllegalArgumentException.class, () -> builder.withNumberOfPeriods(-1));
	}

	/**
	 * @brief Verifica che l'impostazione di una frequenza massima di parole non valida sollevi un'eccezione.
	 * @post
	 * Viene lanciata un'eccezione di tipo IllegalArgumentException per valori minori o uguali a zero.
	 */
	@Test
	public void testInvalidMaximumWordFrequency() {
		PresetConfig.Builder builder = new PresetConfig.Builder();
		
		assertThrows(IllegalArgumentException.class, () -> builder.withMaximumWordFrequency(0));
		assertThrows(IllegalArgumentException.class, () -> builder.withMaximumWordFrequency(-5));
	}

	/**
	 * @brief Verifica che l'impostazione di un numero massimo di parole simili negativo sollevi un'eccezione.
	 * @post
	 * Viene lanciata un'eccezione di tipo IllegalArgumentException per valori negativi.
	 */
	@Test
	public void testInvalidMaximumSimilarWordInQuestionText() {
		PresetConfig.Builder builder = new PresetConfig.Builder();
		
		assertThrows(IllegalArgumentException.class, () -> builder.withMaximumSimilarWordInQuestionText(-1));
	}

	/**
	 * @brief Verifica che l'impostazione di un offset di shifting negativo sollevi un'eccezione.
	 * @post
	 * Viene lanciata un'eccezione di tipo IllegalArgumentException per valori negativi.
	 */
	@Test
	public void testInvalidShiftingOffset() {
		PresetConfig.Builder builder = new PresetConfig.Builder();
		
		assertThrows(IllegalArgumentException.class, () -> builder.withShiftingOffset(-1));
	}
}