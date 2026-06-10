package gruppo05.gtwserver.sourcemanager.internal.similarity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @brief   Classe di test per {@link LetterFrequencySimilarity}.
 *
 * @details Verifica il corretto funzionamento del calcolo di similarità
 *          tra due parole, basato sul vettore di frequenze e cosine similarity.
 *          I test coprono vari scenari: parole identiche, anagrammi,
 *          differenze per suffissi, parole completamente diverse, 
 *          caratteri fuori range e stringhe vuote.
 *
 */
class LetterFrequencySimilarityTest {

    // === CAMPI DELLA CLASSE DI TEST ===

    /**
     * @brief Istanza del predicato da testare.
     */
    private LetterFrequencySimilarity similarityPredicate;

    // === METODI DI SETUP ===

    /**
     * @brief   Inizializza le risorse necessarie prima di ogni test.
     * @details Assegna una nuova istanza di {@link LetterFrequencySimilarity}
     *          garantendo che ogni test sia isolato dagli altri e che operi su un oggetto "nuovo".
     *
     * @post    L'oggetto similarityPredicate è istanziato e pronto all'uso nei casi di test.
     */
    @BeforeEach
    void setUp() {
        similarityPredicate = new LetterFrequencySimilarity();
    }

    // === METODI DI TEST ===

    /**
     * @brief   Testa il comportamento con due parole identiche.
     * @details Il cosine similarity per vettori uguali deve essere 1.0, 
     *          che è superiore alla soglia.
     */
    @Test
    void testIdenticalWords() {
        assertTrue(similarityPredicate.test("casa", "casa"));
    }

    /**
     * @brief   Testa il comportamento con due anagrammi.
     * @details Parole con stesse lettere in ordine diverso generano
     *          lo stesso vettore di frequenza, per cui il risultato atteso è true.
     */
    @Test
    void testAnagrams() {
        assertTrue(similarityPredicate.test("roma", "amor"));
    }

    /**
     * @brief   Testa due parole sufficientemente simili (stessa radice, suffisso diverso).
     * @details "giocatore" e "giocatori" condividono 8 lettere su 9, garantendo
     *          un cosine similarity > 0.80.
     */
    @Test
    void testSimilarWords() {
        assertTrue(similarityPredicate.test("giocatore", "giocatori"));
    }

    /**
     * @brief   Testa due parole completamente disgiunte e diverse.
     * @details Non condividono alcuna lettera.
     *          Il prodotto scalare è 0, quindi similarity = 0.0.
     */
    @Test
    void testCompletelyDifferentWords() {
        assertFalse(similarityPredicate.test("casa", "fumo"));
    }

    /**
     * @brief   Testa la gestione di lettere accentate e fuori range.
     * @details "città" viene processata escludendo la 'à'. La parola risultante 
     *          (che contribuisce con le sole lettere 'c', 'i', 't', 't') viene
     *          comparata a "citta", risultando sufficientemente simile.
     *
     * @post    Il metodo testato deve restituire true senza lanciare eccezioni.
     */
    @Test
    void testWordsWithAccentedLetters() {
        assertTrue(similarityPredicate.test("città", "citta"));
    }

    /**
     * @brief   Testa il comportamento in presenza di caratteri speciali ignorati.
     * @details Trattini o altri simboli devono essere ignorati dal calcolo vettoriale
     *          poiché finiscono fuori dal range ASCII considerato [0, 25].
     */
    @Test
    void testWordsWithSpecialCharacters() {
        assertTrue(similarityPredicate.test("c-a-s-a", "casa"));
    }

    /**
     * @brief   Testa la gestione di parole vuote (norma euclidea zero).
     * @details Se la parola è vuota, il vettore è nullo e il metodo testato
     *          deve gestire la divisione per zero (impedendola) e restituire 0.0 (false).
     *
     * @post    Il metodo testato deve restituire false senza lanciare eccezioni.
     */
    @Test
    void testEmptyWord() {
        assertFalse(similarityPredicate.test("", "cane"));
        assertFalse(similarityPredicate.test("gatto", ""));
        assertFalse(similarityPredicate.test("", ""));
    }
}
