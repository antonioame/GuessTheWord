/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.internal.generation;

/**
 *
 * @author Hermann
 */
import gruppo05.gtwserver.model.Question;
import gruppo05.gtwserver.sourcemanager.api.config.PresetConfig;
import gruppo05.gtwserver.sourcemanager.exception.QuestionGenerationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @brief Classe responsabile della generazione di quesiti a partire da uno stream di testo.
 * @invariant
 * L'estrattore di parole (wordExtractor) non deve essere nullo.
 * @invariant
 * Il generatore di numeri casuali (random) non deve essere nullo.
 */
public class QuestionGenerator {

    /**
     * @brief Estrattore interno per la selezione della parola chiave.
     */
    private final WordExtractor wordExtractor;

    /**
     * @brief Generatore di numeri casuali per l'indicizzazione dello stralcio di testo.
     */
    private final Random random;

    /**
     * @brief Costruttore del generatore di domande.
     * @param[in] wordExtractor Componente per estrarre la parola target dal testo.
     * @param[in] random        Generatore di numeri casuali per la selezione dell'indice.
     * @pre
     * Entrambe le dipendenze fornite in input (wordExtractor e random) devono essere diverse da null.
     * @post
     * L'istanza di QuestionGenerator viene creata con i relativi motori di estrazione e casualità pronti.
     */
    public QuestionGenerator(WordExtractor wordExtractor, Random random) {
        this.wordExtractor = wordExtractor;
        this.random = random;
    }

    /**
     * @brief Genera una domanda estraendo un testo casuale, individuando una parola e cifrandola.
     * @param[in] source             Stream di stringhe contenente solo parole e punti.
     * @param[in] wordFrequencies    Mappa delle frequenze assolute delle parole.
     * @param[in] config             Configurazione del preset per i vincoli di generazione.
     * @param[in] estimatedWordCount Numero di parole stimate presenti nella fonte.
     * @return La domanda generata contenente il testo con la parola cifrata e la risposta in chiaro.
     * @pre
     * Lo stream source non deve essere nullo e deve contenere elementi validi.
     * @pre
     * La mappa wordFrequencies e l'oggetto config non devono essere nulli.
     * @post
     * Viene restituita un'istanza valida di Question avente la risposta esatta in chiaro
     * ed il testo della domanda opportunamente modificato tramite cifratura della parola chiave.
     */
    public Question generateQuestion(Stream<String> source, Map<String, Integer> wordFrequencies, PresetConfig config, long estimatedWordCount) throws QuestionGenerationException {
        // Estrazione dello stralcio di testo basato sul numero di periodi configurato
        String questionText = extractQuestionText(source, config.getNumberOfPeriods(), estimatedWordCount);
        
        // Individuazione della parola chiave all'interno del testo estratto
        String targetWord = wordExtractor.extractWord(questionText, wordFrequencies, config);
        
        // Cifratura del testo sostituendo le occorrenze esatte della parola chiave
        String encryptedText = encryptQuestionText(questionText, targetWord, config.getShiftingOffset());
        
        return new Question(encryptedText, targetWord);
    }

    /**
     * @brief Estrae uno stralcio di testo partendo da un indice casuale fino a completare i periodi richiesti.
     * @param[in] source             Stream sorgente di token (parole e punti).
     * @param[in] numberOfPeriods    Numero di frasi (terminate da '.') da includere nello stralcio.
     * @param[in] estimatedWordCount Numero di parole stimate presenti nella fonte.
     * @return Lo stralcio di testo estratto sotto forma di stringa unica.
     * @pre
     * Lo stream dei token deve essere popolato e numberOfPeriods deve essere maggiore di zero.
     * @post
     * Viene restituita una stringa non vuota formata concatenando i token isolati,
     * garantendo la corretta spaziatura tra le parole.
     */
    private String extractQuestionText(Stream<String> source, int numberOfPeriods, long estimatedWordCount) throws QuestionGenerationException {
        
        // 1. Calcolo del salto casuale. 
        // Riduciamo la stima del 15% per evitare di saltare troppo vicini alla fine del file
        long maxSkip = Math.max(0, (long)(estimatedWordCount * 0.85));
        long randomSkip = (maxSkip > 0) ? (long)(random.nextDouble() * maxSkip) : 0;

        // 2. Saltiamo 'randomSkip' token nello stream e otteniamo un Iterator
        Iterator<String> iterator = source.skip(randomSkip).iterator();
        
        // 3. Poiché abbiamo saltato a caso, potremmo essere a metà frase. 
        // Scartiamo tutto finché non troviamo il primo punto '.', così iniziamo da una frase pulita.
        if (randomSkip > 0) {
            while (iterator.hasNext()) {
                if (".".equals(iterator.next())) {
                    break;
                }
            }
        }

        // 4. Inizia l'estrazione manuale ("takeWhile")
        StringBuilder sb = new StringBuilder();
        int periodCount = 0;
        
        while (iterator.hasNext()) {
            String token = iterator.next();
            
            if (sb.length() > 0 && !".".equals(token)) {
                sb.append(" ");
            }
            sb.append(token);

            if (".".equals(token)) {
                periodCount++;
                if (periodCount == numberOfPeriods) {
                    break; // Esci e ferma il caricamento in memoria!
                }
            }
        }

        String result = sb.toString().trim();
        if (result.isEmpty()) {
            throw new QuestionGenerationException("Impossibile formare un periodo valido. Riprovare.");
        }
        
        return result;
    }

    /**
     * @brief Cifra le occorrenze esatte (Case Sensitive) della parola target nel testo applicando uno shifting.
     * @param[in] text           Il testo originale dello stralcio.
     * @param[in] word           La parola chiave da individuare e cifrare.
     * @param[in] shiftingOffset Il valore numerico di spostamento dei caratteri per la cifratura.
     * @return Il testo finale modificato con la parola cifrata.
     * @pre
     * Il testo e la parola non devono essere nulli o vuoti.
     * @post
     * Tutte le occorrenze esatte della parola vengono rimpiazzate dalla sua versione cifrata.
     */
    private String encryptQuestionText(String text, String word, int shiftingOffset) {
        if (word == null || word.isEmpty()) {
            return text;
        }

        // Cifratura preventiva della singola parola chiave
        String encryptedWord = shiftString(word, shiftingOffset);
        
        // Costruiamo la Regex con i Word Boundaries (\b).
        // Usiamo Pattern.quote per evitare che eventuali caratteri speciali nella parola rompano la Regex.
        String regex = "\\b" + java.util.regex.Pattern.quote(word) + "\\b";

        // Sostituzione globale sicura.
        // Matcher.quoteReplacement previene errori se la parola cifrata contiene simboli interpretati da replaceAll come i dollari ($)
        return text.replaceAll(regex, java.util.regex.Matcher.quoteReplacement(encryptedWord));
    }

    /**
     * @brief Esegue lo shifting alfabetico dei caratteri di una stringa preservando il case (Caesar cipher).
     * @param[in] input  La stringa alfabetica da cifrare.
     * @param[in] offset Lo scostamento applicato ad ogni carattere.
     * @return La stringa risultante dallo shifting.
     * @pre
     * La stringa di input non deve essere nulla.
     * @post
     * I caratteri alfabetici vengono traslati nell'alfabeto di riferimento, i restanti restano invariati.
     */
    private String shiftString(String input, int offset) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                // Calcolo dello shift circolare all'interno dell'alfabeto a 26 lettere
                int shifted = (c - base + offset) % 26;
                if (shifted < 0) {
                    shifted += 26; // Gestione di eventuali offset negativi
                }
                sb.append((char) (base + shifted));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}