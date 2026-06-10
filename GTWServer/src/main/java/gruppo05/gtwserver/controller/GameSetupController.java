package gruppo05.gtwserver.controller;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gruppo05.gtwserver.db.SourceDAO;
import gruppo05.gtwserver.db.ConcreteSourceDAO; 
import gruppo05.gtwserver.db.WordDAO;
import gruppo05.gtwserver.db.ConcreteWordDAO;       
import gruppo05.gtwserver.model.Question;
import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.model.Word;
import gruppo05.gtwserver.sourcemanager.api.config.PresetConfig;
import gruppo05.gtwserver.sourcemanager.internal.generation.QuestionGenerator;
import gruppo05.gtwserver.sourcemanager.internal.generation.WordExtractor;
import gruppo05.gtwshared.utility.Difficulty;
import gruppo05.gtwserver.sourcemanager.internal.similarity.LetterFrequencySimilarity;

/**
 * @class GameSetupController
 * @brief Controller dedicato all'inizializzazione e alla parametrizzazione di una sessione di gioco.
 * @details Questa classe astrae la logica di business dal layer di rete. 
 * Ha la responsabilità esclusiva di mediare i conflitti sulle difficoltà selezionate dai client, 
 * calcolare i vincoli temporali (timer) e orchestrare il motore di generazione testuale 
 * interfacciandosi con il Data Access Object (DAO).
 * @invariant I parametri generati (testo cifrato, parola target, timer) rimangono costanti per l'intero ciclo di vita dell'istanza.
 * @see QuestionGenerator
 * @see PresetConfig
 * @author chiara
 * @version 1.0
 */
public class GameSetupController {

    /** @brief Difficoltà definitiva stabilita per il match corrente. */
    private Difficulty matchDifficulty;
    
    /** @brief Tempo limite in secondi calcolato per la risoluzione della sfida. */
    private int timer;
    
    /** @brief Stralcio di testo offuscato tramite algoritmo di cifratura (Caesar Cipher). */
    private String cipheredText;
    
    /** @brief La soluzione in chiaro attesa dal sistema per la vittoria. */
    private String targetWord;
    
    /** @brief Identificativo della sorgente testuale nel database da cui è stata estratta la parola. */
    private int sourceId;

    /**
     * @brief Costruttore di default per il setup controller.
     * @details Alloca la memoria e imposta i valori di fallback di sicurezza per prevenire 
     * crash di sistema qualora i sottosistemi di generazione o il database non fossero raggiungibili.
     * @post L'oggetto viene istanziato in uno stato di sicurezza valido e utilizzabile.
     */
    public GameSetupController() {
        this.cipheredText = "TESTO***DI***FALLBACK";
        this.targetWord = "DEFAULT";
        this.sourceId = 1;
        this.timer = 60;
        this.matchDifficulty = Difficulty.NORMAL;
    }

    /**
     * @brief Coordina il processo di estrazione e cifratura in base alle preferenze dei giocatori.
     * @details Risolve stocasticamente (50/50) eventuali discrepanze tra le difficoltà richieste 
     * e modella di conseguenza i parametri del PresetConfig (offset, tolleranza, frequenza).
     * @param[in] p1Difficulty Il livello di difficoltà proposto dal Giocatore 1.
     * @param[in] p2Difficulty Il livello di difficoltà proposto dal Giocatore 2.
     * @pre I parametri p1Difficulty e p2Difficulty non devono essere nulli.
     * @post Le variabili di istanza (cipheredText, targetWord, timer) vengono sovrascritte con i dati reali generati.
     */
    public void generateMatchData(Difficulty p1Difficulty, Difficulty p2Difficulty) {
        Random random = new Random();
        
        // Seleziona casualmente la difficoltà se i due giocatori hanno scelto opzioni diverse
        this.matchDifficulty = random.nextBoolean() ? p1Difficulty : p2Difficulty;

        final int offset;
        final int maxFreq;
        
        // Imposta i vincoli di gioco (tempo, chiavi di cifratura e rarità della parola) in base alla difficoltà
        switch (this.matchDifficulty) {
            case EASY:
                this.timer = 90;
                offset = 1;      
                maxFreq = 200;   
                break;
            case HARD:
                this.timer = 30;
                offset = 7;      
                maxFreq = 30;    
                break;
            case NORMAL:
            default:
                this.timer = 60;
                offset = 3;
                maxFreq = 100;
                break;
        }

        try {
            SourceDAO sourceDao = new ConcreteSourceDAO();
            List<Source> sources = sourceDao.selectAll(); // Recupera tutte le fonti di testo disponibili dal database
            
            if (!sources.isEmpty()) {
                // Seleziona casualmente una delle fonti disponibili e ne memorizza l'ID
                Source selectedSource = sources.get(random.nextInt(sources.size()));
                this.sourceId = selectedSource.getId(); 
                
                WordDAO wordDao = new ConcreteWordDAO();
                // Estrae tutte le parole note e crea una mappa {Token -> Frequenza} per la valutazione della difficoltà
                Map<String, Integer> wordFrequencies = wordDao.selectAll().stream()
                        .collect(Collectors.toMap(
                                w -> w.getToken(), 
                                Word::getFrequency, 
                                (v1, v2) -> v1)); // Risolve eventuali collisioni mantenendo il primo valore
                
                // Set di parole non rilevanti che non devono essere scelte come target
                Set<String> stopWords = new HashSet<>(Arrays.asList(
                        "il", "lo", "la", "i", "gli", "le", "di", "a", "da", 
                        "in", "con", "su", "per", "tra", "fra", "un", "una", "uno"
                ));
                
                // Inizializza il motore di estrazione definendo le regole: similarità, priorità di frequenza e stop words
                WordExtractor extractor = new WordExtractor(
                        new LetterFrequencySimilarity(), 
                        (f1, f2) -> f1 < f2,                            
                        stopWords, 
                        random
                );
                
                QuestionGenerator generator = new QuestionGenerator(extractor, random);
                
                // Costruisce la configurazione del generatore utilizzando i parametri calcolati nello switch
                PresetConfig dynamicConfig = new PresetConfig.Builder()
                        .withNumberOfPeriods(2)
                        .withShiftingOffset(offset)
                        .withMaximumWordFrequency(maxFreq)
                        .withMaximumSimilarWordInQuestionText(1)
                        .build();
                
                long estimatedWords = 1000L; // Dimensione stimata per ottimizzare l'elaborazione dello stream
                
                // Legge il file di testo sorgente riga per riga suddividendolo in singole parole
                try (Stream<String> textStream = Files.lines(selectedSource.getPath())
                        .flatMap(line -> Arrays.stream(line.split("\\s+")))) {
                    
                    // Delega al generatore la creazione del quiz effettivo e aggiorna lo stato del Controller
                    Question q = generator.generateQuestion(textStream, wordFrequencies, dynamicConfig, estimatedWords);
                    this.cipheredText = q.getText();   
                    this.targetWord = q.getAnswer();   
                }
            }
        } catch (Exception e) {
            // Logga l'errore; i valori originali impostati nel costruttore (fallback) garantiranno che il gioco non crashi
            System.err.println("Errore critico generazione domanda, utilizzo fallback. Motivo: " + e.getMessage());
        }
    }

    /**
     * @brief Fornisce la difficoltà ufficiale decretata dal sistema per la sessione.
     * @return Enum `Difficulty` rappresentante il livello applicato.
     */
    public Difficulty getMatchDifficulty() { return matchDifficulty; }

    /**
     * @brief Fornisce il limite temporale imposto ai giocatori.
     * @return Intero esprimente i secondi a disposizione.
     */
    public int getTimer() { return timer; }

    /**
     * @brief Fornisce l'elaborato testuale mascherato dal cifrario.
     * @return Stringa contenente il testo cifrato da mostrare nell'interfaccia.
     */
    public String getCipheredText() { return cipheredText; }

    /**
     * @brief Fornisce la chiave di risoluzione in chiaro.
     * @return Stringa della parola originale necessaria per superare la sfida.
     */
    public String getTargetWord() { return targetWord; }

    /**
     * @brief Fornisce la provenienza del materiale testuale.
     * @return Intero rappresentante l'ID univoco della sorgente documentale.
     */
    public int getSourceId() { return sourceId; }
}