package gruppo05.gtwserver.model;

import gruppo05.gtwshared.utility.Result;

/**
 *
 * @author francesco-vecchione
 */
public class Match {
    final String player;
    final int challenge;
    final Result result;
    final int timeToAnswer;

    public Match(String player, int challenge, Result result, int timeToAnswer) {
        this.player = player;
        this.challenge = challenge;
        this.result = result;
        this.timeToAnswer = timeToAnswer;
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

    public int getTimeToAnswer() {
        return timeToAnswer;
    }
}
