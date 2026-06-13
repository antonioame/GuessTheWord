/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.api;

import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.model.Question;
import gruppo05.gtwserver.sourcemanager.api.config.SourceManagerConfig;
import gruppo05.gtwserver.sourcemanager.api.config.PresetConfig;
import gruppo05.gtwserver.sourcemanager.exception.PresetNotFoundException;
import gruppo05.gtwserver.sourcemanager.exception.QuestionGenerationException;
import gruppo05.gtwserver.sourcemanager.internal.io.IOManager;
import gruppo05.gtwserver.sourcemanager.internal.analysis.SourceAnalyzer;
import gruppo05.gtwserver.sourcemanager.internal.generation.QuestionGenerator;
import gruppo05.gtwserver.sourcemanager.internal.generation.WordExtractor;
import java.util.HashSet;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @brief Componente core per la gestione asincrona del ciclo di vita dei sorgenti e la generazione di domande.
 * @invariant
 * L'executor service interno deve essere inizializzato e attivo fino alla chiamata di spegnimento.
 * @invariant
 * La mappa dei preset configurati non deve essere nulla o contenere chiavi non valide.
 */
public class BasicSourceManager implements SourceManager, AutoCloseable {

    private final IOManager ioManager;
    private final QuestionGenerator questionGenerator;
    private final SourceAnalyzer sourceAnalyzer;
    private final ExecutorService executor;
    private final Map<String, PresetConfig> presets;
    private static final String MODULE_SANITIZATION_REGEX = "[^a-zA-Z0-9àèìòùáéíóúÀÈÌÒÙÁÉÍÓÚ]";

    /**
     * @brief Costruttore principale che configura tutti i sottomoduli interni ed il pool di thread.
     * @param[in] config La configurazione iniziale contenente i DAO, le funzioni di confronto e i preset.
     * @pre
     * Il parametro di configurazione config non deve essere nullo.
     * @post
     * Tutti i componenti interni IOManager, SourceAnalyzer e QuestionGenerator sono pronti all'uso.
     */
    public BasicSourceManager(SourceManagerConfig config) {
        this.ioManager = new IOManager(config.getSourceDao(), config.getWordDao(), MODULE_SANITIZATION_REGEX);
        
        // 2. Logica dei "Sensible Defaults" per le StopWords
        Set<String> finalStopWords = config.getStopWords();
        
        // Se la configurazione non ha stopwords (null), tentiamo di caricare quelle di default
        if (finalStopWords == null || finalStopWords.isEmpty()) {
            try {
                System.out.println("[BasicSourceManager] Nessuna stopword custom fornita. Caricamento default in corso...");
                finalStopWords = ioManager.readDefaultStopWords();
            } catch (Exception e) {
                System.err.println("[BasicSourceManager] ERRORE: Impossibile caricare le stopwords di default. Verrà usato un set vuoto.");
                finalStopWords = new HashSet<>();
            }
        }
        
        this.sourceAnalyzer = new SourceAnalyzer(finalStopWords);
        
        WordExtractor extractor = new WordExtractor(
            config.getSimilarityFunction(), 
            config.getFallbackWordCriterion(), 
            finalStopWords, 
            new Random(),
            MODULE_SANITIZATION_REGEX
        );
        this.questionGenerator = new QuestionGenerator(extractor, new Random());
        this.executor = Executors.newFixedThreadPool(4);
        this.presets = config.getPresets();
    }

    /**
     * @brief Registra e analizza un sorgente estraendone le frequenze dei token in modo asincrono.
     * @param[in] source Il sorgente informativo da persistere e analizzare.
     * @param[in] onSuccess Task di callback da lanciare in caso di successo dell'intera operazione.
     * @param[in] onFailure Consumer di callback per intercettare e gestire eventuali eccezioni sollevate.
     * @pre
     * Tutti i parametri passati in input (source, onSuccess, onFailure) devono essere non nulli.
     * @post
     * L'operazione viene inserita nella coda di esecuzione dell'executor service in background.
     */
    @Override
    public void addSource(Source source, Runnable onSuccess, Consumer<Exception> onFailure) {
        this.executor.submit(() -> {
            try {
                ioManager.writeSource(source);
                try (Stream<String> wordsAndPeriods = ioManager.readSourceWords(source)) {
                    Map<String, Integer> frequencies = sourceAnalyzer.getSourceMapWordFrequency(wordsAndPeriods);
                    ioManager.writeSourceMapFrequency(source, frequencies);
                }
                onSuccess.run();
            } catch (Exception e) {
                onFailure.accept(e);
            }
        });
    }

    /**
     * @brief Rimuove in maniera asincrona un sorgente e tutte le mappe di frequenza associate dal sistema.
     * @param[in] source Il sorgente da eliminare dal sistema di storage.
     * @param[in] onSuccess Task di callback da lanciare ad eliminazione completata.
     * @param[in] onFailure Consumer di callback delegato alla gestione di errori di cancellazione.
     * @pre
     * I parametri in ingresso devono essere validi e non nulli.
     * @post
     * Il task di rimozione viene preso in carico ed eseguito in un thread separato.
     */
    @Override
    public void removeSource(Source source, Runnable onSuccess, Consumer<Exception> onFailure) {
        this.executor.submit(() -> {
            try {
                ioManager.deleteSource(source);
                onSuccess.run();
            } catch (Exception e) {
                onFailure.accept(e);
            }
        });
    }

    /**
     * @brief Genera asincronamente un oggetto Question estraendo dati dal sorgente secondo le regole di un preset.
     * @param[in] source Il sorgente da cui estrapolare il testo e le risposte.
     * @param[in] presetName Il nome univoco della configurazione di generazione da applicare.
     * @param[in] onSuccess Callback invocata passando l'istanza della domanda generata con successo.
     * @param[in] onFailure Callback invocata in caso di fallimento o se il preset specificato non esiste.
     * @pre
     * I parametri passati non devono essere nulli.
     * @post
     * La pipeline di estrazione e cifratura della domanda viene avviata in background.
     */
    @Override
    public void generateQuestion(Source source, String presetName, Consumer<Question> onSuccess, Consumer<Exception> onFailure) {
        executor.submit(() -> {
            try {
                PresetConfig config = presets.get(presetName);
                if (config == null) throw new PresetNotFoundException();

                Map<String, Integer> frequencies = ioManager.readSourceMapFrequency(source);
                long estimatedWordCount = ioManager.getEstimatedWordCount(source);

                Question question;
            
                try {
                    // TENTATIVO 1: Lettura ottimizzata con salto (skip)
                    try (Stream<String> stream = ioManager.readRawLines(source)) {
                        question = questionGenerator.generateQuestion(stream, frequencies, config, estimatedWordCount);
                    }
                } catch (QuestionGenerationException e) {
                    // TENTATIVO 2: Se il salto ha esaurito il file, ripartiamo da 0.
                    // Riapriamo un nuovo Stream fresco e passiamo '0' come stima.
                    // Passando 0, il QuestionGenerator calcolerà uno skip di 0 e leggerà dall'inizio.
                    try (Stream<String> fallbackStream = ioManager.readRawLines(source)) {
                        question = questionGenerator.generateQuestion(fallbackStream, frequencies, config, 0);
                    }
                }

                onSuccess.accept(question);

            } catch (Exception e) {
                onFailure.accept(e);
            }
        });
    }

    /**
     * @brief Disattiva il gestore arrestando l'accettazione di nuovi task asincroni.
     * @post
     * L'executor service interno avvia la sua normale procedura di shutdown.
     */
    @Override
    public void shutdown() {
        this.executor.shutdown();
    }

    /**
     * @brief Metodo di chiusura della risorsa delegato dal contratto dell'interfaccia AutoCloseable.
     * @post
     * Viene invocato internamente il metodo shutdown per terminare in sicurezza i thread attivi.
     */
    @Override
    public void close() {
        shutdown();
    }
}
