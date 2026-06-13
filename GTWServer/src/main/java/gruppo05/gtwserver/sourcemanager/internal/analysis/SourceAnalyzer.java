/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.internal.analysis;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @brief Analizzatore di sorgenti testuali per il calcolo delle frequenze delle parole.
 * @invariant
 * L'insieme delle parole di stop (stopWords) non deve mai essere nullo.
 */
public class SourceAnalyzer {

	/**
	 * @brief Insieme delle parole vuote (stop words) da ignorare durante l'analisi.
	 */
	private final Set<String> stopWords;

	/**
	 * @brief Costruttore che inizializza l'analizzatore con un set di parole di stop.
	 * @param[in] stopWords	L'insieme di parole chiave da filtrare ed escludere dal conteggio.
	 * @pre
	 * Il parametro stopWords non deve essere nullo.
	 * @post
	 * L'attributo interno stopWords viene valorizzato con il set fornito in input.
	 */
	public SourceAnalyzer(Set<String> stopWords) {
		if (stopWords == null) {
			throw new IllegalArgumentException("stopWords cannot be null");
		}
		this.stopWords = stopWords;
	}

	/**
	 * @brief Elabora uno stream di token calcolando la frequenza di ciascuna parola valida.
	 * @param[in] sourceWordsAndPeriods	Lo stream di token (parole o punti) provenienti dalla sorgente.
	 * @return Una mappa contenente le parole normalizzate in minuscolo come chiavi e il rispettivo numero di occorrenze come valori.
	 * @pre
	 * Lo stream sourceWordsAndPeriods non deve essere nullo.
	 * @post
	 * La mappa restituita non è nulla, contiene solo chiavi in minuscolo,
	 * non contiene segni di punteggiatura (punti) e non contiene parole presenti nel set delle stopWords.
	 */
	public Map<String, Integer> getSourceMapWordFrequency(Stream<String> sourceWordsAndPeriods) {
		if (sourceWordsAndPeriods == null) {
			throw new IllegalArgumentException("Stream cannot be null");
		}
		return sourceWordsAndPeriods
				.filter(this::isWord)
				.map(String::toLowerCase)
				.filter(word -> !stopWords.contains(word))
				.collect(Collectors.toMap(
						word -> word,
						word -> 1,
						Integer::sum
				));
	}

	/**
	 * @brief Verifica se un determinato token rappresenta una parola valida o un punto di interpunzione.
	 * @param[in] token	Il token stringa da esaminare.
	 * @return Un valore booleano: true se il token è una parola, false se rappresenta un punto, se è vuoto o nullo.
	 * @post
	 * Il valore restituito rispecchia la natura del token distinguendo le parole dalla punteggiatura.
	 */
	private boolean isWord(String token) {
		return token != null && !token.isEmpty() && !".".equals(token);
	}
}