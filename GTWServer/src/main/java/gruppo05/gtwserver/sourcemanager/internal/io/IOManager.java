package gruppo05.gtwserver.sourcemanager.internal.io;

import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.model.Word;
import gruppo05.gtwserver.db.SourceDAO;
import gruppo05.gtwserver.db.WordDAO;
import gruppo05.gtwserver.sourcemanager.exception.SourceNotFoundException;
import gruppo05.gtwserver.sourcemanager.exception.FrequencyMapNotFoundException;
import gruppo05.gtwserver.sourcemanager.exception.StorageException;
import gruppo05.gtwserver.sourcemanager.exception.StopWordsLoadException;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @brief Componente per la gestione delle operazioni di Input/Output del sistema.
 * @invariant
 * Il database access object per i sorgenti (sourceDao) non deve essere nullo.
 * @invariant
 * Il database access object per le parole (wordDao) non deve essere nullo.
 */
public class IOManager {

    /**
     * @brief Oggetto DAO per l'accesso e la persistenza delle entità Source.
     */
    private final SourceDAO sourceDao;

    /**
     * @brief Oggetto DAO per l'accesso e la persistenza delle entità Word.
     */
    private final WordDAO wordDao;
    
    /**
     * @brief Regola di sanificazione globale del modulo testuale iniettata dall'orchestratore.
     * Definisce l'espressione regolare utilizzata per rimuovere la punteggiatura e mantenere
     * coerente la scomposizione delle parole con le fasi successive (es. estrazione e cifratura).
     */
    private final String sanitizationRegex;

    /**
     * @brief Costruttore del gestore di IO dell'applicazione.
     * @param[in] sourceDao Oggetto DAO per la persistenza dei sorgenti.
     * @param[in] wordDao Oggetto DAO per la persistenza delle parole.
     * @param[in] sanitizationRegex L'espressione regolare per pulire il testo da caratteri non validi.
     * @pre
     * Tutti i parametri di input devono essere diversi da null.
     * @post
     * L'istanza di IOManager viene creata con i relativi DAO correttamente configurati.
     */
    public IOManager(SourceDAO sourceDao, WordDAO wordDao, String sanitizationRegex) {
        this.sourceDao = sourceDao;
        this.wordDao = wordDao;
        this.sanitizationRegex = sanitizationRegex;
    }
    
    /**
     * @brief Legge il contenuto grezzo (raw) del file sorgente per preservarne l'estetica.
     * Utilizzato dal generatore di domande per mantenere intatta la formattazione, 
     * la punteggiatura e le maiuscole originali dell'autore.
     * @param[in] source L'oggetto sorgente testuale da cui leggere.
     * @return Uno Stream di stringhe, dove ogni elemento rappresenta una riga intatta del file originale.
     * @throws SourceNotFoundException Se il file non esiste, è inaccessibile o il percorso non è valido.
     * @post Lo stream restituito deve essere chiuso dal chiamante (es. tramite try-with-resources) per liberare il file lock.
     */
    public Stream<String> readRawLines(Source source) throws SourceNotFoundException {
        if (source == null || source.getPath() == null || !Files.exists(source.getPath())) {
            throw new SourceNotFoundException();
        }
        try {
            return Files.lines(source.getPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SourceNotFoundException();
        }
    }

    /**
     * @brief Legge il file sorgente estraendone un flusso di sole parole pure.
     * Utilizzato esclusivamente dall'analizzatore per il calcolo statistico delle frequenze, 
     * elimina completamente qualsiasi segno di interpunzione.
     * @param[in] source L'oggetto sorgente testuale da elaborare.
     * @return Uno Stream di token rappresentanti le singole parole, privi di punteggiatura.
     * @throws SourceNotFoundException Se il file non esiste o il percorso non è valido.
     * @post Lo stream restituito non contiene stringhe vuote o segni di punteggiatura isolati.
     */
    public Stream<String> readSourceWords(Source source) throws SourceNotFoundException {
        if (source == null || source.getPath() == null || !Files.exists(source.getPath())) {
            throw new SourceNotFoundException();
        }
        try {
            return Files.lines(source.getPath(), StandardCharsets.UTF_8)
                    .flatMap(this::extractWordsFromLine);
        } catch (IOException e) {
            throw new SourceNotFoundException();
        }
    }

    /**
     * @brief Recupera dal database la mappa delle frequenze delle parole relative ad un determinato sorgente.
     * @param[in] source L'oggetto sorgente di cui si vogliono ottenere le frequenze memorizzate.
     * @return Una mappa avente come chiave il token della parola e come valore la sua frequenza interna.
     * @pre
     * Il sorgente passato in input non deve essere null.
     * @post
     * La mappa ritornata rispecchia fedelmente lo stato memorizzato nel wordDao per l'id del sorgente fornito.
     */
    public Map<String, Integer> readSourceMapFrequency(Source source) throws FrequencyMapNotFoundException {
        if (source == null) {
            throw new FrequencyMapNotFoundException();
        }

        try {
            // Interroghiamo il DAO filtrando per l'identificativo univoco del sorgente
            List<Word> words = wordDao.selectAllWhere(Optional.empty(), Optional.empty(), Optional.of(source.getId()));
            
            if (words == null || words.isEmpty()) {
                throw new FrequencyMapNotFoundException();
            }

            // Trasformiamo la lista in mappa aggregando per Token e Frequenza tramite StreamAPI
            return words.stream().collect(Collectors.toMap(word -> word.getToken(), Word::getFrequency));
        } catch (Exception e) {
            throw new FrequencyMapNotFoundException();
        }
    }

    /**
     * @brief Effettua il salvataggio persistente di un'entità Source nel database.
     * @param[in] source L'oggetto sorgente da registrare all'interno del sistema.
     * @pre
     * L'oggetto sorgente non deve essere nullo.
     * @post
     * Il sorgente viene inserito in modo sicuro all'interno della base dati sfruttando il DAO dedicato.
     */
    public void writeSource(Source source) throws StorageException {
        if (source == null) {
            throw new StorageException();
        }
        try {
            sourceDao.insert(source);
        } catch (Exception e) {
            throw new StorageException();
        }
    }
    
    /**
     * @brief Stima il numero di parole contenute nel file sorgente in base alla sua dimensione.
     * @param[in] source L'oggetto sorgente.
     * @return Il numero stimato di token (parole).
     */
    public long getEstimatedWordCount(Source source) throws SourceNotFoundException {
        if (source == null || source.getPath() == null || !Files.exists(source.getPath())) {
            throw new SourceNotFoundException();
        }
        try {
            // Ottiene la dimensione del file in byte direttamente dal File System
            long bytes = Files.size(source.getPath());
            
            // Euristica: una parola media in italiano (inclusa spaziatura/punteggiatura) occupa circa 7 byte
            // Restituisce almeno 1 per evitare errori matematici
            return Math.max(1, bytes / 7);
            
        } catch (IOException e) {
            throw new SourceNotFoundException();
        }
    }

    /**
     * @brief Memorizza massivamente le frequenze calcolate per le parole di un determinato sorgente.
     * @param[in] source L'oggetto sorgente a cui agganciare le frequenze delle parole.
     * @param[in] frequencies Mappa contenente i token e il rispettivo numero di occorrenze rilevato.
     * @pre
     * Sia il sorgente sia la mappa delle frequenze non devono essere nulli.
     * @post
     * Tutti gli elementi inseriti nella mappa vengono convertiti in entità Word e storicizzati tramite wordDao.
     */
    public void writeSourceMapFrequency(Source source, Map<String, Integer> frequencies) throws StorageException {
        if (source == null || frequencies == null) {
            throw new StorageException();
        }
        try {
            // Convertiamo la mappa di frequenze in una lista di entità strutturate Word
            List<Word> wordsToInsert = frequencies.entrySet().stream()
                    .map(entry -> new Word(entry.getKey(), entry.getValue(), source.getId()))
                    .collect(Collectors.toList());

            wordDao.insertAll(wordsToInsert);
        } catch (Exception e) {
            throw new StorageException();
        }
    }

    /**
     * @brief Cancella in maniera definitiva un sorgente e tutte le sue parole collegate dal sistema.
     * @param[in] source Il sorgente da rimuovere completamente.
     * @pre
     * Il sorgente da eliminare non deve essere nullo.
     * @post
     * Vengono eliminate prima tutte le parole figlie associate tramite wordDao e successivamente l'entità Source tramite sourceDao.
     */
    public void deleteSource(Source source) throws StorageException {
        if (source == null) {
            throw new StorageException();
        }
        try {
            // Per garantire l'integrità logica dei dati elimineremmo prima le parole collegate al sorgente
            // wordDao.deleteAllWhere(w -> w.getSource() == source.getId());
            // Ciò non è però necessario grazie a ON DELETE CASCADE del database tra Word e Source
            sourceDao.delete(Optional.of(source.getId()));
        } catch (Exception e) {
            throw new StorageException();
        }
    }

    /**
     * @brief Carica l'insieme delle parole vuote (stop-words) di default da un file di configurazione predefinito.
     * @return Un Set immutabile o precompilato contenente le stop-words unificate.
     * @pre
     * Il file o la risorsa delle stop-words posizionata a livello di sistema deve risultare accessibile.
     * @post
     * Viene ritornato un insieme contenente le parole di stop estratte e isolate riga per riga senza spazi bianchi.
     */
    public Set<String> readDefaultStopWords() throws StopWordsLoadException {
        // Il file deve chiamarsi esattamente così nella cartella resources
        String resourcePath = "/stopwords-it.txt"; 
        
        // Carica il file dalle risorse interne del progetto
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                // Il file non è stato trovato in resources
                throw new StopWordsLoadException(); 
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.toSet());
            }
        } catch (IOException e) {
            throw new StopWordsLoadException();
        }
    }

    /**
     * @brief Esegue una tokenizzazione pura su una riga di testo, estraendo solo parole valide.
     * Poiché l'estetica è gestita altrove, elimina apostrofi e punteggiatura trasformandoli in spazi.
     * @param[in] line La riga testuale da pulire.
     * @return Uno Stream contenente i singoli token puliti.
     * @post Parole composte per elisione (es. "dell'animo") vengono divise in due token puri ("dell", "animo").
     */
    private Stream<String> extractWordsFromLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return Stream.empty();
        }

        // Applicazione rigorosa della regola di business globale del modulo
        String cleaned = line.replaceAll(this.sanitizationRegex, " ");

        // Tokenizzazione per spazi
        String[] tokens = cleaned.trim().split("\\s+");

        return Arrays.stream(tokens).filter(token -> !token.isEmpty());
    }
}