package gruppo05.gtwserver.model;

import gruppo05.gtwshared.utility.Difficulty;
import java.sql.Date;

/**
 *
 * @author francesco-vecchione
 */
public class Challenge {
    final int code;
    final Date date;
    final Difficulty difficulty;
    final String word;
    final int source;

    public Challenge(int code, Date date, Difficulty difficulty, String word, int source) {
        this.code = code;
        this.date = date;
        this.difficulty = difficulty;
        this.word = word;
        this.source = source;
    }

    public int getCode() {
        return code;
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
