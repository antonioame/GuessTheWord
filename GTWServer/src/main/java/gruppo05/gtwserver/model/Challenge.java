package gruppo05.gtwserver.model;

import gruppo05.gtwshared.utility.Difficulty;
import java.sql.Date;
import java.util.Random;

/**
 *
 * @author francesco-vecchione
 */
public class Challenge {
    private final ChallengeId id;
    private final Date date;
    private final Difficulty difficulty;
    private final String word;
    private final int source;

    public Challenge(int code, Date date, Difficulty difficulty, String word, int source) {
        this.id = new ChallengeId(code);
        this.date = date;
        this.difficulty = difficulty;
        this.word = word;
        this.source = source;
    }
    
    public Challenge(Date date, Difficulty difficulty, String word, int source) {
        this(   new Random().nextInt(Integer.MAX_VALUE) + 1,
                date,
                difficulty,
                word,
                source);
    }

    public int getCode() {
        return id.getCode();
    }

    public Date getDate() {
        return date;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getWord() {
        return word;
    }

    public int getSource() {
        return source;
    }
}
