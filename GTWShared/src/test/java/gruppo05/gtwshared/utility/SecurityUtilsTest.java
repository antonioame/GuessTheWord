package gruppo05.gtwshared.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @class   SecurityUtilsTest
 * @brief   Test unitari per {@link SecurityUtils}.
 * 
 * @details
 *          I test coprono i seguenti scenari per il metodo {@code hashPassword}:
 *          <ul>
 *              <li>Password valida => Restituisce l'hash SHA-256 corretto (lunghezza 64, caratteri esadecimali)</li>
 *              <li>Password null => Restituisce null senza sollevare eccezioni</li>
 *              <li>Password identiche => Restituiscono lo stesso hash</li>
 *              <li>Password diverse => Restituiscono hash diversi</li>
 *          </ul>
 */
public class SecurityUtilsTest {

    /**
     * @brief Verifica che l'hashing di una password valida restituisca una stringa esadecimale di 64 caratteri.
     */
    @Test
    public void testHashPassword_PasswordValida_HashValido() {
        String password = "passwordSicura123!";
        String hash = SecurityUtils.hashPassword(password);

        assertNotNull(hash, "L'hash non deve essere null per una password valida");
        assertEquals(64, hash.length(), "L'hash SHA-256 deve essere lungo esattamente 64 caratteri");
        assertTrue(hash.matches("^[a-f0-9]{64}$"), "L'hash deve contenere solo caratteri esadecimali minuscoli");
    }

    /**
     * @brief Verifica che l'hashing di una password null restituisca null.
     */
    @Test
    public void testHashPassword_PasswordNull_RestituisceNull() {
        String hash = SecurityUtils.hashPassword(null);
        
        assertNull(hash, "L'hash deve essere null se la password fornita è null");
    }

    /**
     * @brief Verifica che l'hashing della stessa password più volte restituisca sempre lo stesso risultato.
     */
    @Test
    public void testHashPassword_PasswordIdentiche_HashUguali() {
        String password = "testPassword";
        String hash1 = SecurityUtils.hashPassword(password);
        String hash2 = SecurityUtils.hashPassword(password);

        assertEquals(hash1, hash2, "L'hash calcolato su password identiche deve essere uguale");
    }

    /**
     * @brief Verifica che l'hashing di password diverse restituisca risultati diversi.
     */
    @Test
    public void testHashPassword_PasswordDiverse_HashDiversi() {
        String hash1 = SecurityUtils.hashPassword("passwordUno");
        String hash2 = SecurityUtils.hashPassword("passwordDue");

        assertNotEquals(hash1, hash2, "L'hash calcolato su password diverse deve essere diverso");
    }
}
