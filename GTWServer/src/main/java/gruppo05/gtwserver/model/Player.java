package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 */
public class Player {
    final String username;
    final String password;
    final int totalPlayedTime;
    final int totalGamesWon;
    final int totalGamesPlayed;

    public Player(String username, String password, int totalPlayedTime, int totalGamesWon, int totalGamesPlayed) {
        this.username = username;
        this.password = password;
        this.totalPlayedTime = totalPlayedTime;
        this.totalGamesWon = totalGamesWon;
        this.totalGamesPlayed = totalGamesPlayed;
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

    public int getTotalGamesWon() {
        return totalGamesWon;
    }

    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }
    
    
}
