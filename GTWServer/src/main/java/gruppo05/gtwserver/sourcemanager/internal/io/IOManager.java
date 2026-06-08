/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.internal.io;

/**
 *
 * @author Hermann
 */
import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.model.Word;
import gruppo05.gtwserver.db.DAO;
import gruppo05.gtwserver.model.SourceId;
import gruppo05.gtwserver.model.WordId;
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
    private final DAO<Source, SourceId> sourceDao;

    /**
     * @brief Oggetto DAO per l'accesso e la persistenza delle entità Word.
     */
    private final DAO<Word, WordId> wordDao;

    /**
     * @brief Costruttore del gestore di IO dell'applicazione.
     * @param[in] sourceDao Oggetto DAO per la persistenza dei sorgenti.
     * @param[in] wordDao Oggetto DAO per la persistenza delle parole.
     * @pre
     * Entrambi i parametri di input sourceDao e wordDao devono essere diversi da null.
     * @post
     * L'istanza di IOManager viene creata con i relativi DAO correttamente configurati.
     */
    public IOManager(DAO<Source, SourceId> sourceDao, DAO<Word, WordId> wordDao) {
        this.sourceDao = sourceDao;
        this.wordDao = wordDao;
    }

    /**
     * @brief Legge il contenuto del file associato ad un sorgente restituendo uno stream pigro di parole e punti.
     * @param[in] source L'oggetto sorgente contenente il path del file da leggere.
     * @return Uno Stream di stringhe in cui compaiono esclusivamente parole purificate e punti singoli.
     * @pre
     * Il sorgente non deve essere null e il percorso del file associato deve essere valido ed esistente.
     * @post
     * Viene restituito un flusso aperto per la lettura sequenziale del file. La chiusura dello stream è delegata al chiamante.
     */
    public Stream<String> readSourceWordsAndPeriods(Source source) throws SourceNotFoundException {
        if (source == null || source.getPath() == null) {
            throw new SourceNotFoundException();
        }

        Path path = source.getPath();
        if (!Files.exists(path)) {
            throw new SourceNotFoundException();
        }

        try {
            // Sfruttiamo Files.lines per una lettura lazy guidata dalla StreamAPI, riducendo l'impatto sulla memoria
            return Files.lines(path)
                    .flatMap(this::extractWordsAndPeriodsFromLine);
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
            List<Word> words = wordDao.selectAllWhere(w -> w.getId().getSource() == source.getId().getId());
            
            if (words == null || words.isEmpty()) {
                throw new FrequencyMapNotFoundException();
            }

            // Trasformiamo la lista in mappa aggregando per Token e Frequenza tramite StreamAPI
            return words.stream().collect(Collectors.toMap(word -> word.getId().getToken(), Word::getFrequency));
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
                    .map(entry -> new Word(entry.getKey(), entry.getValue(), source.getId().getId()))
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
            sourceDao.delete(source.getId());
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
     * @brief Algoritmo interno di tokenizzazione di una singola riga di testo per estrarre parole e punti singoli.
     * @param[in] line La stringa testuale grezza estratta dal file sorgente.
     * @return Uno stream di token contenente parole pulite e punti singoli estratti dalla riga corrente.
     * @pre
     * La stringa passata in input non deve essere nulla.
     * @post
     * Tutti i caratteri speciali non ammessi vengono eliminati, e i punti consecutivi vengono unificati in un unico punto indipendente.
     */
    private Stream<String> extractWordsAndPeriodsFromLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return Stream.empty();
        }

        // 1. Pulizia caratteri: manteniamo lettere (comprese le accentate italiane), numeri e punti, sostituendo il resto con uno spazio
        String cleaned = line.replaceAll("[^a-zA-Z0-9àèìòùáéíóúÀÈÌÒÙÁÉÍÓÚ.]", " ");

        // 2. Unificazione dei punti: qualsiasi sequenza di più punti consecutivi (es. "...") viene ridotta a un singolo punto isolato da spazi
        cleaned = cleaned.replaceAll("\\.+", " . ");

        // 3. Tokenizzazione per spazi bianchi multipli
        String[] tokens = cleaned.trim().split("\\s+");

        // Ritorna lo stream filtrando eventuali residui vuoti dovuti alla formattazione regex
        return Arrays.stream(tokens).filter(token -> !token.isEmpty());
    }
}