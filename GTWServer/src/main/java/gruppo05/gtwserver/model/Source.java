package gruppo05.gtwserver.model;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Random;

/**
 *
 * @author francesco-vecchione
 * 
 * @brief Rappresenta una sorgente di dati (Source) identificata da un id numerico e associata a un percorso di file.
 * @invariant
 * L'identificativo composto SourceId e il percorso del file sono immutabili (final).
 */
public class Source {
    
    /**
     * @brief L'identificativo univoco della sorgente contenente il codice numerico.
     */
    private final SourceId id;
    
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
        this.id = new SourceId(id);
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

    // Da commentare
    public SourceId getId() {
        return id;
    }

    /**
     * @brief Restituisce il percorso del file della sorgente.
     * @return Un oggetto Path corrispondente al percorso memorizzato.
     */
    public Path getPath() {
        return path;
    }

    // Da commentare
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    // Da commentare
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
        if (!this.id.equals(other.getId())) {
            return false;
        }
        return true;
    }
    
    
}
