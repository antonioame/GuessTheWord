package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 */
public class WordId {
    private final String token;
    private final int source;    

    public WordId(String token, int source) {
        this.token = token;
        this.source = source;
    }

    public String getToken() {
        return token;
    }

    public int getSource() {
        return source;
    }
}
