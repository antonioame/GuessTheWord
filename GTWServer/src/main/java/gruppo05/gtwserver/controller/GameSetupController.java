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
import gruppo05.gtwserver.sourcemanager.api.BasicSourceManager;
import gruppo05.gtwserver.sourcemanager.api.config.PresetConfig;
import gruppo05.gtwserver.sourcemanager.internal.generation.QuestionGenerator;
import gruppo05.gtwserver.sourcemanager.internal.generation.WordExtractor;
import gruppo05.gtwshared.utility.Difficulty;
import gruppo05.gtwserver.sourcemanager.internal.similarity.LetterFrequencySimilarity;

/**
 * @class GameSetupController
 * @brief Controller dedicato all'inizializzazione e alla parametrizzazione di una sessione di gioco.
 * @details Gestisce la preparazione logica della partita: seleziona la difficoltà finale, 
 * calcola il tempo a disposizione ed estrae i dati di gioco dal database. Riceve l'istanza 
 * globale di BasicSourceManager e la utilizza per interrogare i preset già configurati in App.java.
 * @version 1.0
 */
public class GameSetupController {

    /** @brief Gestore centralizzato per la generazione dei testi cifrati e delle domande. */
    private final BasicSourceManager sourceManager;
    
    /** @brief Livello di difficoltà stabilito per la partita corrente. */
    private Difficulty matchDifficulty;
    
    /** @brief Tempo in secondi a disposizione dei giocatori. */
    private int timer;
    
    /** @brief Testo cifrato che verrà mostrato ai giocatori. */
    private String cipheredText;
    
    /** @brief Soluzione in chiaro che i giocatori dovranno indovinare. */
    private String targetWord;
    
    /** @brief Identificativo univoco della Source prelevata dal database. */
    private int sourceId;

    /**
     * @brief Costruttore con Dependency Injection per il SourceManager.
     * @details Inizializza il controller assegnando dei valori di default (fallback).
     * Questo garantisce che l'oggetto abbia uno stato coerente anche qualora 
     * la generazione dei dati non vada a buon fine.
     * @param sourceManager L'istanza centralizzata creata all'avvio dell'applicazione.
     */
    public GameSetupController(BasicSourceManager sourceManager) {
        this.sourceManager = sourceManager;
        
        // Inizializzazione dei valori di default per prevenire stati nulli
        this.cipheredText = "TESTO***DI***FALLBACK";
        this.targetWord = "DEFAULT";
        this.sourceId = 1;
        this.timer = 60;
        this.matchDifficulty = Difficulty.NORMAL;
    }

    /**
     * @brief Genera e imposta i dati fondamentali per la partita.
     * @details Seleziona casualmente la difficoltà tra quelle proposte dai due giocatori,
     * imposta il timer corrispondente, preleva una Source casuale dal DB e genera il
     * contenuto di gioco tramite il manager globale.
     * 
     * @param p1Difficulty Difficoltà proposta dal Giocatore 1.
     * @param p2Difficulty Difficoltà proposta dal Giocatore 2.
     */
    public void generateMatchData(Difficulty p1Difficulty, Difficulty p2Difficulty) {
        Random random = new Random();
        
        // 1. Scelta della difficoltà: con probabilità del 50% vince la scelta del P1, altrimenti quella del P2
        this.matchDifficulty = random.nextBoolean() ? p1Difficulty : p2Difficulty;

        // 2. Assegnazione del tempo in base alla difficoltà estratta
        switch (this.matchDifficulty) {
            case EASY:
                this.timer = 90; // Più tempo per la modalità facile
                break;
            case HARD:
                this.timer = 30; // Tempo ridotto per la modalità difficile
                break;
            case NORMAL:
            default:
                this.timer = 60; // Tempo standard di default
                break;
        }

        try {
            // 3. Inizializzazione dell'accesso al Database
            SourceDAO sourceDao = new ConcreteSourceDAO();
            
            // 4. Recupero di tutte le sorgenti di testo disponibili
            List<Source> sources = sourceDao.selectAll();
            
            // Procediamo solo se il database ha restituito dei risultati
            if (!sources.isEmpty()) {
                
                // 5. Estrazione di un elemento casuale dalla lista delle sorgenti
                Source selectedSource = sources.get(random.nextInt(sources.size()));
                
                // Memorizziamo l'ID della sorgente selezionata per statistiche/controlli futuri
                this.sourceId = selectedSource.getId(); 
                
                // 6. Generazione della domanda tramite il manager
                // Si richiama il preset passando il nome della difficoltà (es. "EASY", "NORMAL") come stringa
                this.sourceManager.generateQuestion(
                        selectedSource, 
                        this.matchDifficulty.name(), 
                        
                        // Callback di Successo: viene eseguita se il testo è generato correttamente
                        (question) -> {
                            this.cipheredText = question.getText(); // Salva il testo cifrato
                            this.targetWord = question.getAnswer(); // Salv la asoluzione reale
                        },
                        
                        // Callback di Errore: viene eseguita in caso di problemi interni al manager
                        (exception) -> {
                            System.err.println("Errore nella generazione del testo: " + exception.getMessage());
                        }
                );
            }
        } catch (Exception e) {
            // Cattura eventuali eccezioni relative all'accesso al database
            System.err.println("Errore critico: " + e.getMessage());
        }
    }

    /**
     * @brief Restituisce la difficoltà definitiva della partita.
     * @return Difficoltà della partita (Difficulty).
     */
    public Difficulty getMatchDifficulty() { return matchDifficulty; }

    /**
     * @brief Restituisce la durata della partita.
     * @return Tempo a disposizione in secondi.
     */
    public int getTimer() { return timer; }

    /**
     * @brief Restituisce il testo di gioco (es. il testo da decifrare).
     * @return Il testo cifrato elaborato dal manager.
     */
    public String getCipheredText() { return cipheredText; }

    /**
     * @brief Restituisce la parola o il testo che rappresenta la soluzione.
     * @return La soluzione in chiaro attesa dal sistema.
     */
    public String getTargetWord() { return targetWord; }

    /**
     * @brief Restituisce l'ID della Source utilizzata per questa partita.
     * @return Identificativo intero della Source nel database.
     */
    public int getSourceId() { return sourceId; }
    
}