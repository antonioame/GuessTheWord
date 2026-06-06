package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 */
public class Player {
    final String username;
    final String password;
    final int totalPlayedTime;
    final int totalMatchesWon;
    final int totalMatchesPlayed;

    public Player(String username, String password, int totalPlayedTime, int totalMatchesWon, int totalMatchesPlayed) {
        this.username = username;
        this.password = password;
        this.totalPlayedTime = totalPlayedTime;
        this.totalMatchesWon = totalMatchesWon;
        this.totalMatchesPlayed = totalMatchesPlayed;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getTotalPlayedTime() {
        return totalPlayedTime;
    }

    public int getTotalMatchesWon() {
        return totalMatchesWon;
    }

    public int getTotalMatchesPlayed() {
        return totalMatchesPlayed;
    }
    
    
}
