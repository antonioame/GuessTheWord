package gruppo05.gtwserver.model;

import java.nio.file.Path;
import java.util.Random;
/**
 *
 * @author francesco-vecchione
 */
public class Source {
    private final SourceId id;
    private final Path path;

    public Source(int id, Path path) {
        this.id = new SourceId(id);
        this.path = path;
    }
    
    public Source(Path path) {
        this(   new Random().nextInt(Integer.MAX_VALUE) + 1, 
                path);
    }

    public int getId() {
        return id.getId();
    }

    public Path getPath() {
        return path;
    }
}
