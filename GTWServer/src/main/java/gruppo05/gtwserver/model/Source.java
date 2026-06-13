package gruppo05.gtwserver.model;

import java.nio.file.Path;
import java.util.Random;

/**
 * @brief Rappresenta una sorgente di dati (Source) identificata da un id numerico e associata a un percorso di file.
 * @invariant
 * L'identificativo id e il percorso del file sono immutabili (final).
 */
public class Source {
    
    /**
     * @brief L'identificativo numerico univoco della sorgente, utilizzato come chiave primaria.
     */
    private final int id;
    
    /**
     * @brief Il percorso di file del sistema associato alla sorgente.
     */
    private final Path path;

    /**
     * @brief Costruttore completo per creare un oggetto Source con un identificativo numerico esplicito.
     * @param[in] id Il codice numerico univoco da assegnare alla sorgente.
     * @param[in] path Il percorso del file associato alla sorgente.
     * @pre
     * Il parametro path non deve essere null.
     * @post
     * Viene creata una nuova istanza di SourceId memorizzata nel campo id.
     */
    public Source(int id, Path path) {
        this.id = id;
        this.path = path;
    }
    
    /**
     * @brief Costruttore secondario che genera automaticamente un identificativo casuale per la sorgente.
     * @param[in] path Il percorso del file associato alla sorgente.
     * @pre
     * Il parametro path non deve essere null.
     * @post
     * La sorgente viene istanziata richiamando il costruttore principale con un intero casuale compreso tra 1 e Integer.MAX_VALUE.
     */
    public Source(Path path) {
        this(   new Random().nextInt(Integer.MAX_VALUE) + 1, 
                path);
    }

    /**
     * @brief Restituisce l'identificativo numerico della sorgente.
     * @return L'identificativo id della sorgente.
     */
    public int getId() {
        return id;
    }

    /**
     * @brief Restituisce il percorso del file della sorgente.
     * @return Un oggetto Path corrispondente al percorso memorizzato.
     */
    public Path getPath() {
        return path;
    }

    /**
     * @brief Calcola l'hash code della sorgente basandosi sull'identificativo id.
     * @return Il valore dell'hash code calcolato.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.id;
        return hash;
    }

    /**
     * @brief Confronta questa sorgente con l'oggetto specificato per verificarne l'uguaglianza.
     * @param[in] obj L'oggetto da confrontare con la sorgente corrente.
     * @return true se l'oggetto specificato è uguale a questa sorgente, false altrimenti.
     * @post
     * Il risultato è true se e solo se l'oggetto passato non è null,
     * è un'istanza di Source e ha lo stesso id di questo oggetto.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Source other = (Source) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
    
    
}
