package gruppo05.gtwserver.sourcemanager.api.config;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.function.BiPredicate;
import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.model.Word;
import gruppo05.gtwserver.db.SourceDAO;
import gruppo05.gtwserver.db.WordDAO;

/**
 * @brief Classe di configurazione immutabile per la gestione delle sorgenti.
 * @invariant
 * I riferimenti a sourceDao, wordDao, similarityFunction e fallbackWordCriterion non devono mai essere null.
 * @invariant
 * Le collezioni dei preset e delle stop words restitute non devono mai essere nulle e devono essere strutturalmente immutabili.
 */
public class SourceManagerConfig {
    /**
     * @brief Il Data Access Object per la persistenza delle entità Source.
     */
    private final SourceDAO sourceDao;
    /**
     * @brief Il Data Access Object per la persistenza delle entità Word.
     */
    private final WordDAO wordDao;
    /**
     * @brief Funzione biocriterio per determinare la similarità tra due stringhe tokenizzate.
     */
    private final BiPredicate<String, String> similarityFunction;
    /**
     * @brief Criterio di ripiego (fallback) basato sulla frequenza o su metriche intere delle parole.
     */
    private final BiPredicate<Integer, Integer> fallbackWordCriterion;
    /**
     * @brief Mappa che associa chiavi identificative testuali a specifiche configurazioni di preset.
     */
    private final Map<String, PresetConfig> presets;
    /**
     * @brief Insieme di parole non rilevanti (stop words) da escludere durante le analisi.
     */
    private final Set<String> stopWords;
    /**
     * @brief Costruttore privato che copia i valori accumulati all'interno del Builder.
     * @param[in] builder   Il builder contenente i parametri di configurazione validati.
     * @pre
     * Il parametro builder non deve essere null.
     * @post
     * L'oggetto viene istanziato salvando i parametri in modo protetto (deep copy o wrapper immutabili).
     */
    private SourceManagerConfig(Builder builder) {
        this.sourceDao = builder.sourceDao;
        this.wordDao = builder.wordDao;
        this.similarityFunction = builder.similarityFunction;
        this.fallbackWordCriterion = builder.fallbackWordCriterion;
        this.presets = Collections.unmodifiableMap(new HashMap<>(builder.presets));
        this.stopWords = Collections.unmodifiableSet(new HashSet<>(builder.stopWords));
    }
    /**
     * @brief Recupera il DAO relativo alle sorgenti di dati.
     * @return Il DAO delle entità Source.
     */
    public SourceDAO getSourceDao() {
        return sourceDao;
    }
    /**
     * @brief Recupera il DAO relativo ai vocaboli estratti.
     * @return Il DAO delle entità Word.
     */
    public WordDAO getWordDao() {
        return wordDao;
    }
    /**
     * @brief Recupera la funzione biocriterio di similarità testuale.
     * @return L'interfaccia funzionale BiPredicate per il confronto tra stringhe.
     */
    public BiPredicate<String, String> getSimilarityFunction() {
        return similarityFunction;
    }
    /**
     * @brief Recupera il criterio numerico di fallback per la selezione delle parole.
     * @return L'interfaccia funzionale BiPredicate per i controlli sui valori interi.
     */
    public BiPredicate<Integer, Integer> getFallbackWordCriterion() {
        return fallbackWordCriterion;
    }
    /**
     * @brief Fornisce una vista non modificabile dei preset inseriti nella configurazione.
     * @return Una mappa immutabile contenente le accoppiate nome-preset.
     */
    public Map<String, PresetConfig> getPresets() {
        return presets;
    }
    /**
     * @brief Fornisce una vista non modificabile dell'insieme delle stop words configurate.
     * @return Un set immutabile contenente le parole da ignorare.
     */
    public Set<String> getStopWords() {
        return stopWords;
    }
    /**
     * @brief Classe Builder interna preposta alla costruzione passo-passo di oggetti SourceManagerConfig.
     * @invariant
     * I campi obbligatori inizializzati nel costruttore rimangono non nulli per tutto il ciclo di vita del builder.
     */
    public static class Builder {
        /**
         * @brief Riferimento al DAO delle sorgenti.
         */
        private final SourceDAO sourceDao;
        /**
         * @brief Riferimento al DAO dei vocaboli.
         */
        private final WordDAO wordDao;
        /**
         * @brief Riferimento alla funzione di confronto per similarità.
         */
        private final BiPredicate<String, String> similarityFunction;
        /**
         * @brief Riferimento alla funzione criterio per la gestione del fallback.
         */
        private final BiPredicate<Integer, Integer> fallbackWordCriterion;
        /**
         * @brief Mappa interna cumulativa per memorizzare i preset in fase di build.
         */
        private final Map<String, PresetConfig> presets = new HashMap<>();
        /**
         * @brief Insieme interno cumulativo per memorizzare le parole da escludere.
         */
        private Set<String> stopWords = new HashSet<>();
        /**
         * @brief Costruttore del Builder che accetta e valida tutti i parametri obbligatori del sistema.
         * @param[in] sourceDao      Il data access object per le sorgenti.
         * @param[in] wordDao        Il data access object per i vocaboli.
         * @param[in] simFunc        La logica di similarità testuale personalizzata o di default.
         * @param[in] fallbackCrit   La logica numerica di sbarramento per il fallback.
         * @pre
         * Tutti i parametri passati in input (sourceDao, wordDao, simFunc, fallbackCrit) non devono essere null.
         * @post
         * Viene creata un'istanza operativa del Builder configurata con i componenti core del motore.
         */
        public Builder(SourceDAO sourceDao, WordDAO wordDao, BiPredicate<String, String> simFunc, BiPredicate<Integer, Integer> fallbackCrit) {
            if (sourceDao == null || wordDao == null || simFunc == null || fallbackCrit == null) {
                throw new IllegalArgumentException("I parametri obbligatori del costruttore del Builder non possono essere null.");
            }
            this.sourceDao = sourceDao;
            this.wordDao = wordDao;
            this.similarityFunction = simFunc;
            this.fallbackWordCriterion = fallbackCrit;
        }
        /**
         * @brief Registra o sovrascrive un preset di configurazione associandolo a un nome mnemonico.
         * @param[in] name     La stringa che fa da chiave univoca per identificare il preset.
         * @param[in] preset   L'oggetto di configurazione PresetConfig effettivo.
         * @return Il riferimento all'istanza corrente del Builder per consentire il chaining.
         * @pre
         * Sia il nome identificativo che il preset passato non devono essere null.
         * @post
         * La mappa interna del builder viene aggiornata includendo il nuovo preset configurato.
         */
        public Builder addPreset(String name, PresetConfig preset) {
            if (name == null || preset == null) {
                throw new IllegalArgumentException("Il nome del preset e il preset stesso non possono essere null.");
            }
            this.presets.put(name, preset);
            return this;
        }
        /**
         * @brief Sovrascrive l'insieme predefinito delle stop words con un insieme personalizzato.
         * @param[in] customStopWords   L'insieme contenente i token testuali da ignorare nell'estrazione.
         * @return Il riferimento all'istanza corrente del Builder per consentire il chaining.
         * @pre
         * L'insieme fornito in input non deve essere null.
         * @post
         * Le stop words correnti del builder vengono rimpiazzate da una copia locale dell'insieme passato.
         */
        public Builder withCustomStopWords(Set<String> customStopWords) {
            if (customStopWords == null) {
                throw new IllegalArgumentException("L'insieme di stop words fornito non può essere null.");
            }
            this.stopWords = new HashSet<>(customStopWords);
            return this;
        }
        /**
         * @brief Istanzia definitivamente l'oggetto di configurazione configurato.
         * @return Un'istanza pronta all'uso e immutabile di SourceManagerConfig.
         * @post
         * Restituisce un oggetto SourceManagerConfig i cui dati interni rispecchiano fedelmente lo stato del builder.
         */
        public SourceManagerConfig build() {
            return new SourceManagerConfig(this);
        }
    }
}