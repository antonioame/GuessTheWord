/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.internal.generation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;
import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import gruppo05.gtwserver.sourcemanager.api.config.PresetConfig;
import gruppo05.gtwserver.sourcemanager.exception.QuestionGenerationException;

/**
 * @brief Componente interno deputato all'estrazione di parole chiave da un testo sorgente per la creazione di quesiti.
 * @invariant
 * Le funzioni di similarità e i criteri di fallback forniti in fase di costruzione non devono essere nulli.
 * @invariant
 * L'insieme delle stop words e l'istanza del generatore di numeri casuali devono essere validi e configurati.
 */
public class WordExtractor {
/**
 * @brief Funzione per determinare se due stringhe sono semanticamente o strutturalmente simili.
 */
    private final BiPredicate<String, String> similarityFunction;
/**
 * @brief Criterio di confronto tra le frequenze di due parole usato per determinare la migliore parola di fallback.
 */
    private final BiPredicate<Integer, Integer> fallbackWordCriterion;
/**
 * @brief Insieme di parole non rilevanti (stop words) da escludere durante l'estrazione.
 */
    private final Set<String> stopWords;
/**
 * @brief Generatore di numeri pseudocasuali utilizzato per selezionare una parola tra quelle idonee.
 */
    private final Random random;
    /**
     * @brief Espressione regolare iniettata dall'orchestratore per uniformare la pulizia del testo.
     * Garantisce che l'estrattore veda le parole esattamente come le vede il database.
     */
    private final String sanitizationRegex;


/**
 * @brief Costruttore completo per l'inizializzazione del WordExtractor.
 * @param[in] similarityFunction     Il predicato logico per calcolare la similarità tra stringhe.
 * @param[in] fallbackWordCriterion Il criterio basato sulle frequenze per selezionare parole alternative.
 * @param[in] stopWords             L'insieme di parole da ignorare nell'estrazione.
 * @param[in] random                La sorgente di casualità per la selezione delle parole.
 * @param[in] sanitizationRegex     La regola Regex globale per ripulire la punteggiatura e gli apostrofi.
 * @pre
 * Tutti i parametri passati al costruttore devono essere non nulli.
 * @post
 * Lo stato interno del componente viene configurato stabilmente con le dipendenze fornite.
 */
    public WordExtractor(BiPredicate<String, String> similarityFunction, BiPredicate<Integer, Integer> fallbackWordCriterion, Set<String> stopWords, Random random, String sanitizationRegex) {
        this.similarityFunction = similarityFunction;
        this.fallbackWordCriterion = fallbackWordCriterion;
        this.stopWords = stopWords;
        this.random = random;
        this.sanitizationRegex = sanitizationRegex;
    }

/**
 * @brief Estrae una parola chiave valida dal testo fornito tramite approccio iterativo e tollerante ai fallimenti.
 * @param[in] text            Il testo dal quale estrarre la parola chiave (composto da parole e punti).
 * @param[in] wordFrequencies La mappa contenente la frequenza globale di ciascuna parola nella sorgente.
 * @param[in] config          La configurazione del preset contenente le soglie e i limiti correnti.
 * @return La parola estratta idonea alla generazione della domanda.
 * @pre
 * Il testo di input, la mappa delle frequenze e la configurazione del preset non devono essere nulli.
 * @post
 * La parola restituita rispetta tutti i vincoli di difficoltà o, in caso di esaurimento delle opzioni, rappresenta il miglior compromesso (fallback).
 */
    public String extractWord(String text, Map<String, Integer> wordFrequencies, PresetConfig config) throws QuestionGenerationException {
        if (text == null || wordFrequencies == null || config == null) {
            throw new QuestionGenerationException("I parametri di input non possono essere nulli.");
        }

        // 1. Filtraggio Iniziale: Creazione dello stralcio pulito da stop-words e parole fuori frequenza
        String eligibleText = filterByStopWordsAndMaximumFrequency(text, wordFrequencies, config.getMaximumWordFrequency());
        String fallbackCandidate = null;

        // 2. Ciclo di ricerca: Manteniamo un approccio iterativo classico poiché lo stato 
        // di "eligibleText" e "fallbackCandidate" muta ad ogni giro, rendendo le Stream inadatte qui.
        while (!eligibleText.trim().isEmpty()) {
            
            // 2.1 Estrazione casuale
            String candidate = extractRandomWord(eligibleText);

            // 2.2 Ricerca parole simili sul testo originale
            List<String> similarWords = extractSimilarWords(text, candidate);

            // 2.3 Valutazione
            if (similarWords.size() < config.getMaximumSimilarWordInQuestionText()) {
                return candidate; // Parola trovata, usciamo con successo
            } 
            else {
                // 2.4 Scarto e aggiornamento Fallback
                if (fallbackCandidate == null) {
                    fallbackCandidate = candidate;
                } else {
                    fallbackCandidate = updateFallbackWord(fallbackCandidate, candidate, wordFrequencies);
                }

                // Riduzione dello stralcio: rimuoviamo il candidato e tutte le sue simili
                eligibleText = filterSimilarWords(eligibleText, similarWords, candidate);
            }
        }

        // Restituzione Fallback se lo stralcio si è esaurito
        if (fallbackCandidate != null) {
            return fallbackCandidate;
        }

        throw new QuestionGenerationException("Impossibile estrarre una parola valida: il testo non contiene candidati idonei.");
    }
    
    /**
     * @brief Applica la regola di pulizia globale a una stringa di testo.
     * Rimuove punteggiatura, apostrofi e caratteri speciali, sostituendoli con spazi,
     * per evitare anomalie durante la scomposizione in token e la ricerca nel database.
     * @param[in] text Il testo originale "sporco" proveniente dal file.
     * @return Una nuova stringa normalizzata contenente solo i caratteri consentiti.
     */
    private String sanitizeText(String text) {
        return text.replaceAll(this.sanitizationRegex, " ");
    }

/**
 * @brief Isola le parole del testo originale che superano i controlli di stop-words e frequenza massima.
 * Prima dell'analisi, il testo viene sanificato per rimuovere apostrofi (es. "l'efficienza" diventa "l efficienza")
 * in modo da interrogare la mappa delle frequenze con token puri.
 * @param[in] text                 Il testo sorgente da scansionare.
 * @param[in] wordFrequencies     La mappa delle frequenze delle parole.
 * @param[in] maximumWordFrequency La soglia massima di frequenza ammessa.
 * @return Una stringa (stralcio) contenente le sole parole idonee separate da spazi.
 */
    private String filterByStopWordsAndMaximumFrequency(String text, Map<String, Integer> wordFrequencies, int maximumWordFrequency) {
        String cleanedText = sanitizeText(text);
        // Uso della Stream API per filtrare in modo dichiarativo e lineare
        return Arrays.stream(cleanedText.split("\\s+"))
                // Scarta stringhe vuote
                .filter(w -> !w.isEmpty())
                // Scarta le stop-words
                .filter(w -> !stopWords.contains(w.toLowerCase()))
                // Scarta le parole che superano la frequenza massima tollerata (se non presenti, assumo frequenza 0)
                .filter(w -> wordFrequencies.getOrDefault(w, 0) <= maximumWordFrequency)
                // Riconcatena tutto con spazi singoli
                .collect(Collectors.joining(" "));
    }

/**
 * @brief Seleziona in modo casuale una parola dall'attuale stralcio di testo idoneo.
 * @param[in] text Lo stralcio di testo (parole separate da spazi).
 * @return Una parola estratta casualmente tra quelle disponibili.
 */
    private String extractRandomWord(String text) {
        // Per l'accesso indicizzato casuale, mantenere l'array è più performante rispetto a uno Stream
        String[] eligibleWords = text.trim().split("\\s+");
        return eligibleWords[random.nextInt(eligibleWords.length)];
    }

/**
 * @brief Estrae dal testo originale tutte le parole che risultano simili al candidato correntemente in analisi.
 * Applica la sanificazione a monte per impedire che segni di punteggiatura falsino la funzione di similarità.
 * @param[in] text       Il testo completo di partenza.
 * @param[in] targetWord La parola target che ha fallito il test di similarità
 * @return La lista univoca delle parole simili individuate.
 */
    private List<String> extractSimilarWords(String text, String targetWord) {
        String cleanedText = sanitizeText(text);
        
        return Arrays.stream(cleanedText.split("\\s+"))
                .map(w -> w.replace(".", "").trim())
                .filter(w -> !w.isEmpty())
                // Una parola non va considerata come "simile" a se stessa
                .filter(w -> !w.equalsIgnoreCase(targetWord))
                // Filtriamo usando il predicato di similarità iniettato dal config
                .filter(w -> similarityFunction.test(targetWord, w))
                // Rimuove eventuali doppioni per avere il conteggio esatto delle parole simili uniche
                .distinct()
                .collect(Collectors.toList());
    }

/**
 * @brief Rimuove dallo stralcio di testo corrente la parola appena scartata e tutte le sue simili.
 * @param[in] text         Lo stralcio di testo attualmente esplorabile.
 * @param[in] similarWords La lista delle parole simili da escludere, unitamente alla parola target corrente.
 * @param[in] targetWord   La parola target che ha fallito il test di similarità.
 * @return Il nuovo stralcio di testo ridotto, pronto per la successiva iterazione.
 */
    private String filterSimilarWords(String text, List<String> similarWords, String targetWord) {
        return Arrays.stream(text.split("\\s+"))
                // Teniamo la parola solo se NON è il target che ha appena fallito
                .filter(w -> !w.equalsIgnoreCase(targetWord))
                // E la teniamo solo se NON fa parte della lista delle parole simili
                .filter(w -> !similarWords.contains(w))
                // Ricongiungiamo il testo sopravvissuto
                .collect(Collectors.joining(" "));
    }

/**
 * @brief Confronta il vecchio e il nuovo candidato scartato per determinare chi conservare come fallback.
 * @param[in] oldWord         Il candidato di fallback eletto nei cicli precedenti.
 * @param[in] newWord         Il nuovo candidato appena scartato.
 * @param[in] wordFrequencies La mappa con le frequenze delle parole nella fonte.
 * @return La parola considerata "meno peggio" in base al criterio di frequenza.
 */
    private String updateFallbackWord(String oldWord, String newWord, Map<String, Integer> wordFrequencies) {
        int freqOld = wordFrequencies.getOrDefault(oldWord, 0);
        int freqNew = wordFrequencies.getOrDefault(newWord, 0);

        return fallbackWordCriterion.test(freqOld, freqNew) ? newWord : oldWord;
    }
}