package gruppo05.gtwserver.model;

import gruppo05.gtwshared.utility.Result;

/**
 *
 * @author francesco-vecchione
 */
public class Game {
    final String player;
    final int challenge;
    final Result result;
    final int responseTime;

    public Game(String player, int challenge, Result result, int timeToAnswer) {
        this.player = player;
        this.challenge = challenge;
        this.result = result;
        this.responseTime = timeToAnswer;
    }

    public String getPlayer() {
        return player;
    }

    public int getChallenge() {
        return challenge;
    }

    public Result getResult() {
        return result;
    }

    public int getResponseTime() {
        return responseTime;
    }
}
