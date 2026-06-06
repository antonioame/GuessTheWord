package gruppo05.gtwserver.model;

/**
 *
 * @author francesco-vecchione
 */
public class GameId {
    private final String player;
    private final int challenge;

    public GameId(String player, int challenge) {
        this.player = player;
        this.challenge = challenge;
    }

    public String getPlayer() {
        return player;
    }

    public int getChallenge() {
        return challenge;
    }
}
