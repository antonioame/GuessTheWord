package gruppo05.gtwshared.utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Utility Class per operazioni di sicurezza (in particolare, hashing delle password).
 */
public class SecurityUtils {

    /**
     * Esegue l'hashing di una password utilizzando l'algoritmo SHA-256.
     * 
     * @param password Password (in chiaro) da cifrare.
     * @return Stringa esadecimale che rappresenta l'hash SHA-256 della password,
     *         oppure null se la password fornita è null.
     */
    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        try {
            // FASE 1) Ottenere istanza dell'algoritmo di hashing SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // FASE 2) Convertire la password in byte (codifica UTF-8) e calcolarne l'hash
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            // FASE 3) Restituire hash calcolato, formattato come stringa esadecimale
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("L'algoritmo di hashing SHA-256 non è disponibile.", e);
        }
    }

    /**
     * Converte un array di byte in una stringa esadecimale.
     * 
     * @param hash Array di byte da convertire.
     * @return Stringa esadecimale risultante.
     */
    private static String bytesToHex(byte[] hash) {
        // FASE 1) Istanziare builder con capacità doppia rispetto all'array
        StringBuilder hexString = new StringBuilder(2 * hash.length); // <= 2 caratteri hex per byte)
        // FASE 2) Iterare su ogni singolo byte dell'array
        for (byte b : hash) {
            // FASE 3) Convertire il byte in esadecimale.
                // In Java i byte possono essere negativi (range di rappresentazione include i negativi).
                // Quando li convertiamo, Java aggiunge dei bit extra (1) che "sporcano"
                // il risultato (creando stringhe lunghe come ffffffff).
                // L'operazione serve ad escludere i bit in eccesso e restituire
                // solo il valore originale pulito (da 0 a 255).
            String hex = Integer.toHexString(0xff & b);
            // FASE 4) Aggiungere uno '0' iniziale se il valore esadecimale è di una sola cifra
            if (hex.length() == 1) {
                hexString.append('0');
            }
            // FASE 5) Accodare la rappresentazione esadecimale del byte
            hexString.append(hex);
        }
        // FASE FINALE) Restituire la rappresentazione esadecimale completa come stringa
        return hexString.toString();
    }
}
