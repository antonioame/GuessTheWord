/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.internal.analysis;

/**
 *
 * @author Hermann
 */
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SourceAnalyzerTest {

	private Set<String> stopWords;
	private SourceAnalyzer analyzer;

	@BeforeEach
	void setUp() {
		stopWords = new HashSet<>();
		stopWords.add("il");
		stopWords.add("di");
		stopWords.add("e");
		analyzer = new SourceAnalyzer(stopWords);
	}

	@Test
	void testConstructorThrowsExceptionWithNullStopWords() {
		assertThrows(IllegalArgumentException.class, () -> new SourceAnalyzer(null));
	}

	@Test
	void testGetSourceMapWordFrequencyNormalFlow() {
		Stream<String> tokenStream = Stream.of("Java", "software", "Java", "sviluppo");
		
		Map<String, Integer> frequencies = analyzer.getSourceMapWordFrequency(tokenStream);
		
		assertNotNull(frequencies);
		assertEquals(3, frequencies.size());
		assertEquals(2, frequencies.get("java"));
		assertEquals(1, frequencies.get("software"));
		assertEquals(1, frequencies.get("sviluppo"));
	}

        @Test
	void testGetSourceMapWordFrequencyIgnoresEmptyOrNullStrings() {
		Stream<String> tokenStream = Stream.of("Inizio", "", null, "Fine");
		
		Map<String, Integer> frequencies = analyzer.getSourceMapWordFrequency(tokenStream);
		
		assertNotNull(frequencies);
		assertEquals(2, frequencies.size());
		assertTrue(frequencies.containsKey("inizio"));
		assertTrue(frequencies.containsKey("fine"));
		assertFalse(frequencies.containsKey(""));
	}

	@Test
	void testGetSourceMapWordFrequencyFiltersOutStopWords() {
		Stream<String> tokenStream = Stream.of("il", "codice", "di", "qualità", "e", "pulito");
		
		Map<String, Integer> frequencies = analyzer.getSourceMapWordFrequency(tokenStream);
		
		assertNotNull(frequencies);
		assertEquals(3, frequencies.size());
		assertTrue(frequencies.containsKey("codice"));
		assertTrue(frequencies.containsKey("qualità"));
		assertTrue(frequencies.containsKey("pulito"));
		assertFalse(frequencies.containsKey("il"));
		assertFalse(frequencies.containsKey("di"));
		assertFalse(frequencies.containsKey("e"));
	}

	@Test
	void testGetSourceMapWordFrequencyCaseInsensitivity() {
		Stream<String> tokenStream = Stream.of("Test", "test", "TEST");
		
		Map<String, Integer> frequencies = analyzer.getSourceMapWordFrequency(tokenStream);
		
		assertNotNull(frequencies);
		assertEquals(1, frequencies.size());
		assertEquals(3, frequencies.get("test"));
	}

	@Test
	void testGetSourceMapWordFrequencyWithEmptyStream() {
		Stream<String> tokenStream = Stream.empty();
		
		Map<String, Integer> frequencies = analyzer.getSourceMapWordFrequency(tokenStream);
		
		assertNotNull(frequencies);
		assertTrue(frequencies.isEmpty());
	}

	@Test
	void testGetSourceMapWordFrequencyThrowsExceptionWithNullStream() {
		assertThrows(IllegalArgumentException.class, () -> analyzer.getSourceMapWordFrequency(null));
	}
}