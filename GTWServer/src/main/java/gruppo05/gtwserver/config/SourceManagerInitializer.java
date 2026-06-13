/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.config;

import gruppo05.gtwserver.db.SourceDAO;
import gruppo05.gtwserver.db.WordDAO;
import gruppo05.gtwserver.sourcemanager.api.BasicSourceManager;
import gruppo05.gtwserver.sourcemanager.api.SourceManager;
import gruppo05.gtwserver.sourcemanager.api.config.PresetConfig;
import gruppo05.gtwserver.sourcemanager.api.config.SourceManagerConfig;
import gruppo05.gtwserver.utility.LetterFrequencySimilarity;
import gruppo05.gtwshared.utility.Difficulty;
import java.util.function.BiPredicate;

public final class SourceManagerInitializer {

    // Istanza Singleton
    private static SourceManager instance;

    // Costruttore privato per impedire l'istanziamento della classe
    private SourceManagerInitializer() {
        throw new UnsupportedOperationException("Classe di utilità, non può essere istanziata.");
    }

    /**
     * Metodo di inizializzazione da invocare ESCLUSIVAMENTE all'avvio (nel main).
     * Isola tutta la complessa logica di setup del SourceManager.
     * * @param sourceDao Il DAO per le entità Source
     * @param wordDao Il DAO per le entità Word
     */
    public static void init(SourceDAO sourceDao, WordDAO wordDao) {
        if (instance != null) {
            throw new IllegalStateException("SourceManager è già stato inizializzato.");
        }

        // 1. Configurazione dei Preset basati sull'Enum Difficulty
        
        // Livello Facile: testo breve, parole comuni consentite, poca confusione visiva, crittografia leggera
        PresetConfig easyPreset = new PresetConfig.Builder()
                .withNumberOfPeriods(5)
                .withMaximumWordFrequency(100) 
                .withMaximumSimilarWordInQuestionText(5)
                .withShiftingOffset(2)
                .build();

        // Livello Normale: testo medio, parole meno comuni, un po' di confusione visiva, crittografia media
        PresetConfig normalPreset = new PresetConfig.Builder()
                .withNumberOfPeriods(3)
                .withMaximumWordFrequency(50)
                .withMaximumSimilarWordInQuestionText(3)
                .withShiftingOffset(5)
                .build();

        // Livello Difficile: testo lungo, parole rare, alta confusione visiva, crittografia forte
        PresetConfig hardPreset = new PresetConfig.Builder()
                .withNumberOfPeriods(2)
                .withMaximumWordFrequency(20)
                .withMaximumSimilarWordInQuestionText(2)
                .withShiftingOffset(10)
                .build();

        // 2. Definizione delle funzioni strategiche
        // Esempio: due parole sono simili se sono uguali ignorando il case
        BiPredicate<String, String> similarityFunction = new LetterFrequencySimilarity(); 
        
        // Esempio: il criterio di fallback preferisce la parola con la frequenza minore
        BiPredicate<Integer, Integer> fallbackCriterion = (freq1, freq2) -> freq1 < freq2;

        // 3. Costruzione della configurazione principale del SourceManager
        // Usiamo il nome dell'enum (.name()) come chiave per garantire la coerenza
        SourceManagerConfig config = new SourceManagerConfig.Builder(sourceDao, wordDao, similarityFunction, fallbackCriterion)
                .addPreset(Difficulty.EASY.name(), easyPreset)
                .addPreset(Difficulty.NORMAL.name(), normalPreset)
                .addPreset(Difficulty.HARD.name(), hardPreset)
                // .withCustomStopWords(Set.of("il", "lo", "la", "e", "o")) // Opzionale
                .build();

        // 4. Creazione e salvataggio dell'istanza Singleton
        instance = new BasicSourceManager(config);
    }

    /**
     * Restituisce l'istanza di SourceManager già configurata.
     * * @return L'istanza Singleton di SourceManager
     * @throws IllegalStateException se init() non è stato chiamato precedentemente
     */
    public static SourceManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Il SourceManager non è stato inizializzato. Assicurati di chiamare SourceManagerInitializer.init()");
        }
        return instance;
    }
}