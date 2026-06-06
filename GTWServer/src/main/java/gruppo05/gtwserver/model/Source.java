package gruppo05.gtwserver.model;

import java.nio.file.Path;

/**
 *
 * @author francesco-vecchione
 */
public class Source {
    final int Id;
    final Path path;

    public Source(int Id, Path path) {
        this.Id = Id;
        this.path = path;
    }

    public int getId() {
        return Id;
    }

    public Path getPath() {
        return path;
    }
}
