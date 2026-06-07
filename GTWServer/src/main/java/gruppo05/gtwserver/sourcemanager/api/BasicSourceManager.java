/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.api;

/**
 *
 * @author Hermann
 */
import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.model.Question;
import gruppo05.gtwserver.sourcemanager.api.config.SourceManagerConfig;
import gruppo05.gtwserver.sourcemanager.api.config.PresetConfig;
import gruppo05.gtwserver.sourcemanager.exception.PresetNotFoundException;
import gruppo05.gtwserver.sourcemanager.internal.io.IOManager;
import gruppo05.gtwserver.sourcemanager.internal.analysis.SourceAnalyzer;
import gruppo05.gtwserver.sourcemanager.internal.generation.QuestionGenerator;
import gruppo05.gtwserver.sourcemanager.internal.generation.WordExtractor;

import java.util.Map;
import java.util.Random;
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

    /**
     * @brief Costruttore principale che configura tutti i sottomoduli interni ed il pool di thread.
     * @param[in] config La configurazione iniziale contenente i DAO, le funzioni di confronto e i preset.
     * @pre
     * Il parametro di configurazione config non deve essere nullo.
     * @post
     * Tutti i componenti interni IOManager, SourceAnalyzer e QuestionGenerator sono pronti all'uso.
     */
    public BasicSourceManager(SourceManagerConfig config) {
        this.ioManager = new IOManager(config.getSourceDao(), config.getWordDao());
        this.sourceAnalyzer = new SourceAnalyzer(config.getStopWords());
        
        WordExtractor extractor = new WordExtractor(
            config.getSimilarityFunction(), 
            config.getFallbackWordCriterion(), 
            config.getStopWords(), 
            new Random()
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
                try (Stream<String> wordsAndPeriods = ioManager.readSourceWordsAndPeriods(source)) {
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
        this.executor.submit(() -> {
            try {
                PresetConfig config = presets.get(presetName);
                if (config == null) {
                    throw new PresetNotFoundException();
                }
                try (Stream<String> sourceWords = ioManager.readSourceWordsAndPeriods(source)) {
                    Map<String, Integer> wordFrequencies = ioManager.readSourceMapFrequency(source);
                    Question question = questionGenerator.generateQuestion(sourceWords, wordFrequencies, config);
                    onSuccess.accept(question);
                }
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
