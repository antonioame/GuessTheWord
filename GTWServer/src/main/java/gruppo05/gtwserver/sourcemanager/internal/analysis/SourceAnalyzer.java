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
     * @brief Elabora uno stream di token puri calcolando la frequenza di ciascuna parola valida.
     * @param[in] sourceWords Lo stream di token testuali (solo parole) estratti dalla sorgente.
     * @return Una mappa contenente le parole normalizzate in minuscolo come chiavi e il rispettivo numero di occorrenze.
     * @pre Lo stream sourceWords non deve essere nullo e deve contenere stringhe alfanumeriche.
     * @post La mappa restituita contiene solo chiavi in minuscolo e ignora i termini presenti nel set delle stopWords.
     */
    public Map<String, Integer> getSourceMapWordFrequency(Stream<String> sourceWords) {
        if (sourceWords == null) {
            throw new IllegalArgumentException("Stream cannot be null");
        }
        return sourceWords
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
     * @brief Verifica l'integrità strutturale di un token testuale.
     * Poiché la punteggiatura viene filtrata a monte, si limita a controllare l'assenza di stringhe vuote.
     * @param[in] token Il token stringa da esaminare.
     * @return Un valore booleano: true se il token è valido e contiene caratteri, false se è nullo o vuoto.
     */
    private boolean isWord(String token) {
        return token != null && !token.isEmpty();
    }
}