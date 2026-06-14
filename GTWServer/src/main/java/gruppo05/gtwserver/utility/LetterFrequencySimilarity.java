package gruppo05.gtwserver.utility;

import java.util.function.BiPredicate;
import java.util.stream.IntStream;

/**
 * @brief Predicato di similarità tra due parole basato sul confronto delle distribuzioni
 *        di frequenza dei caratteri (tramite cosine similarity).
 *
 * @details L'algoritmo opera in 3 fasi per ogni coppia di parole:
 *          <ol>
 *            <li>Per ciascuna parola viene costruito un vettore di 26 locazioni,
 *                rappresentante le frequenze (valori interi) dei 26 caratteri dell'alfabeto.</li>
 *            <li>I due vettori vengono confrontati calcolando il <em>cosine similarity</em>:
 *                {@code sim = (A · B) / (|A| * |B|)}, dove {@code A · B} è il prodotto scalare
 *                delle frequenze dei caratteri e {@code |A|}, {@code |B|} sono le norme
 *                euclidee dei due vettori.</li>
 *            <li>Il metodo {@link #test(String, String)} restituisce {@code true} se il valore
 *                calcolato supera o eguaglia la soglia {@link #SIMILARITY_THRESHOLD}, costante hardcoded.</li>
 *          </ol>
 *
 *          Questa soluzione è particolarmente efficace per individuare parole che differiscono
 *          solo per prefisso o suffisso (es. "giocatore" / "giocatori"), in quanto condividono
 *          quasi tutti i caratteri con frequenze simili.
 *
 * @invariant
 *          {@link #SIMILARITY_THRESHOLD} deve essere compreso nell'intervallo [0.0, 1.0].
 *
 * @implements BiPredicate<String, String>
 *
 */
public class LetterFrequencySimilarity implements BiPredicate<String, String> {

    // === DEFINIZIONE DELLE COSTANTI HARDCODED ===

    /**
     * @brief   Soglia minima di cosine similarity affinché due parole siano considerate simili.
     * @details Abbassare questa soglia rende il predicato più permissivo (cattura anche parole
     *          meno correlate); alzarla lo rende più severo.
     */
    private static final double SIMILARITY_THRESHOLD = 0.80;

    /**
     * @brief Numero di lettere dell'alfabeto latino. Dimensione fissa del vettore rappresentante le frequenze.
     */
    private static final int ALPHABET_SIZE = 26;

    /**
     * @brief   Valore ASCII del carattere 'a' minuscolo.
     * @details Usato come offset per convertire la rappresentazione 
     *          intera di un carattere nell'indice corrispondente del
     *          vettore di frequenza (0 = 'a', 1 = 'b', ..., 25 = 'z').
     */
    private static final int ASCII_LOWERCASE_OFFSET = 97;

    // === IMPLEMENTAZIONE DELL'INTERFACCIA FUNZIONALE ===

    /**
     * @brief   Valuta se le due parole fornite sono sufficientemente simili, secondo la metrica
     *          di cosine similarity applicata ai vettori di frequenza dei caratteri.
     *
     * @param[in] word1 Prima parola da confrontare.
     * @param[in] word2 Seconda parola da confrontare.
     * @return {@code true} se il cosine similarity tra i due vettori di frequenza è maggiore
     *         o uguale a {@link #SIMILARITY_THRESHOLD} (costante hardcoded), {@code false} altrimenti.
     *
     * @pre     I parametri word1 e word2 non devono essere null.
     */
    @Override
    public boolean test(String word1, String word2) {
        // Normalizzazione: confronto case-insensitive
        int[] freq1 = buildLetterFrequencyVector(word1.toLowerCase());
        int[] freq2 = buildLetterFrequencyVector(word2.toLowerCase());

        double similarity = computeCosineSimilarity(freq1, freq2);
        return similarity >= SIMILARITY_THRESHOLD;
    }

    // === METODI DI SUPPORTO PRIVATI ===

    /**
     * @brief   Costruisce il vettore di frequenza dei caratteri per una singola parola.
     * @details Il vettore ha dimensione fissa {@link #ALPHABET_SIZE}.
     *          La locazione {@code i} contiene il numero di occorrenze della lettera
     *          corrispondente all'i-esima lettera dell'alfabeto ('a'=0, 'b'=1, …, 'z'=25).
     *          L'indice è calcolato come {@code (int) c - ASCII_LOWERCASE_OFFSET}.
     *          I caratteri il cui indice risultante è fuori dall'intervallo [0, 25]
     *          (es. lettere accentate, simboli) vengono silenziosamente ignorati.
     *
     * @param[in] word Parola (già normalizzata in minuscolo) da cui costruire il vettore.
     * @return Vettore {@code int[26]} rappresentante le frequenze dei caratteri.
     *
     * @pre  Il parametro word non deve essere null.
     * @post Il vettore restituito non è mai null; se word è vuota tutti gli elementi sono 0.
     */
    private int[] buildLetterFrequencyVector(String word) {
        int[] vector = new int[ALPHABET_SIZE];
        
        word.chars()                                                // Converte la stringa in flusso di interi (IntStream)
                                                                    // Dalla documentazione: "Returns a stream of int zero-extending the char values from this sequence."
            .map(c -> c - ASCII_LOWERCASE_OFFSET)                   // Calcola indice, cioé posizione del contatore da incrementare all'interno dell'array
            .filter(index -> index >= 0 && index < ALPHABET_SIZE)   // Ignora caratteri non validi (fuori dal range considerato per gli indici dell'array) come lettere accentate
            .forEach(index -> vector[index]++);                     // Aggiorna lo specifico contatore delle occorrenze per la lettera riscontrata
            
        return vector;
    }

    /**
     * @brief   Calcola il cosine similarity tra due vettori di frequenza dei caratteri.
     * @details Il cosine similarity è definito come:
     *          <pre>
     *          sim(A, B) = (A · B) / (||A|| * ||B||)
     *          </pre>
     *          dove {@code A · B} è il prodotto scalare (somma dei prodotti elemento per
     *          elemento) e {@code ||·||} è la norma euclidea del vettore.
     *          Il risultato è compreso in [0.0, 1.0]:
     *          1.0 indica vettori identici, 0.0 indica nessuna lettera in comune.
     *
     * @param[in] freq1 Vettore di frequenza della prima parola (dimensione {@link #ALPHABET_SIZE}).
     * @param[in] freq2 Vettore di frequenza della seconda parola (dimensione {@link #ALPHABET_SIZE}).
     * @return Valore in [0.0, 1.0] rappresentante il cosine similarity tra i due vettori.
     *
     * @pre  Entrambi i parametri non devono essere null e devono avere lunghezza {@link #ALPHABET_SIZE}.
     * @post Se almeno uno dei due vettori ha norma zero (parola vuota o solo caratteri ignorati),
     *       viene restituito 0.0.
     */
    private double computeCosineSimilarity(int[] freq1, int[] freq2) {
        double dotProduct = IntStream.range(0, ALPHABET_SIZE)
                .mapToDouble(i -> (double) freq1[i] * freq2[i]) // Prodotto scalare
                .sum(); // Riduzione, come operazione terminale
                
        double sumOfSquares1 = IntStream.range(0, ALPHABET_SIZE)
                .mapToDouble(i -> (double) freq1[i] * freq1[i]) // Quadrati vettore 1
                .sum(); // Riduzione, come operazione terminale
                
        double sumOfSquares2 = IntStream.range(0, ALPHABET_SIZE)
                .mapToDouble(i -> (double) freq2[i] * freq2[i]) // Quadrati vettore 2
                .sum(); // Riduzione, come operazione terminale

        double norm1 = Math.sqrt(sumOfSquares1);
        double norm2 = Math.sqrt(sumOfSquares2);

        // Guardia: se almeno un vettore ha norma zero (es. parola vuota) la similarità non è definita
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (norm1 * norm2);
    }
}
