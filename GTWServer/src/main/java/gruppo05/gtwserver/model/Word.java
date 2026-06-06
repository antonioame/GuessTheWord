package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 */
public class Word {
    final String token;
    final int frequency;
    final int source;

    public Word(String token, int frequency, int source) {
        this.token = token;
        this.frequency = frequency;
        this.source = source;
    }

    public String getToken() {
        return token;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getSource() {
        return source;
    }
    
    
}
