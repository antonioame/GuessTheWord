package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 */
public class Word {
    private final WordId id;
    private final int frequency;

    public Word(String token, int frequency, int source) {
        id = new WordId(token, source);
        this.frequency = frequency;
    }

    public String getToken() {
        return id.getToken();
    }

    public int getFrequency() {
        return frequency;
    }

    public int getSource() {
        return id.getSource();
    }
    
    
}
