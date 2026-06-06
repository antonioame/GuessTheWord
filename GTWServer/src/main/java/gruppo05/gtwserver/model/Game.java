package gruppo05.gtwserver.model;

import gruppo05.gtwshared.utility.Result;

/**
 *
 * @author francesco-vecchione
 */
public class Game {
    private final GameId id;
    private final Result result;
    private final int responseTime;

    public Game(String player, int challenge, Result result, int timeToAnswer) {
        this.id = new GameId(player, challenge);
        this.result = result;
        this.responseTime = timeToAnswer;
    }

    public String getPlayer() {
        return id.getPlayer();
    }

    public int getChallenge() {
        return id.getChallenge();
    }

    public Result getResult() {
        return result;
    }

    public int getResponseTime() {
        return responseTime;
    }
}
