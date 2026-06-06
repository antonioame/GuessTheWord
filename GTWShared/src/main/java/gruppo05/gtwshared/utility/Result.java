package gruppo05.gtwshared.utility;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta gli esiti possibili di una partita disputata da un giocatore.
 * @invariant
 * Gli stati dell'enumerazione sono immutabili e limitati ai quattro esiti predefiniti (WIN, LOSE, DRAW, TIMEOUT).
 */
public enum Result {
   WIN, LOSE, DRAW, TIMEOUT; 
}
