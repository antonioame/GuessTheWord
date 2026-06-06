package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 */
public class Player {
    private final PlayerId id;
    private final String password;
    private final int totalPlayedTime;
    private final int totalGamesWon;
    private final int totalGamesPlayed;

    public Player(String username, String password, int totalPlayedTime, int totalGamesWon, int totalGamesPlayed) {
        this.id = new PlayerId(username);
        this.password = password;
        this.totalPlayedTime = totalPlayedTime;
        this.totalGamesWon = totalGamesWon;
        this.totalGamesPlayed = totalGamesPlayed;
    }

    public String getUsername() {
        return id.getUsername();
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
