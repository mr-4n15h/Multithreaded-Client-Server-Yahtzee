package server;

import client.Player;
import constants.ServerOperationConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Broadcast {
    private List<Player> players;

    Broadcast() {}

    Broadcast(List<Player> players) {
        this.players = Collections.synchronizedList(players);
    }

    private synchronized void broadcastMessage(String message) {
        for(final Player player: players) {
            player.sendMessage(message);
        }
    }

    synchronized void broadcastMessage(Player currentPlayer, String message) {
        final String PLAYER_NAME = currentPlayer.getPlayerName();
        for(final Player player: players) {
            if(!player.getPlayerName().equals(PLAYER_NAME))
                player.sendMessage(message);
        }
    }

    private synchronized void broadcastDicesRolledMessage(String message) {
        for(final Player player: players) {
            final String PLAYER_ROLLED_DICES = message.replaceAll(player.getPlayerName(), "You");
            player.sendMessage(PLAYER_ROLLED_DICES);
        }
    }

    synchronized void broadcastPlayersRoundNumber(int roundNumber) {
        broadcastMessage(ServerOperationConstants.ROUND_NUMBER);
        final String ROUND_NUMBER = String.valueOf(roundNumber);
        broadcastMessage(ROUND_NUMBER);
    }

    synchronized void broadcastDicesRolledByPlayers(String dicesRolled) {
        broadcastDicesRolledMessage(dicesRolled);
    }

    void broadcastPlayerScoreTotal() {
        StringBuilder scoresString = new StringBuilder();
        for(Player player : players)
            scoresString.append(player.getPlayerName()).append("'s total score is ").append(player.getTotalScore()).append("\n");
        broadcastMessage(scoresString.toString());
    }

    synchronized void declareWinner() {
        // Implement the bonus in upper section

        // Inform them the game has finished
        broadcastMessage(ServerOperationConstants.GAME_END);

        // Players sorted by their score (highest to smallest)
        List<Player> playerSortedByScores = players.stream()
                                                   .sorted(Comparator.comparing(Player::getTotalScore).reversed())
                                                   .collect(Collectors.toCollection(ArrayList::new));
        // The highest score achieved by a player
        // When debugged, the scoreboard will be pr
        final int HIGHEST_SCORE = playerSortedByScores.get(0).getTotalScore();
        final Predicate<Player> SAME_SCORE_AS_TOP_PLAYER = player -> player.getTotalScore() == HIGHEST_SCORE;

        // Obtain the winner
        // There can be a draw in the game
        List<Player> winner = playerSortedByScores.stream()
                                                  .filter(SAME_SCORE_AS_TOP_PLAYER)
                                                  .collect(Collectors.toCollection(ArrayList::new));

        System.out.println("Max score: " + HIGHEST_SCORE);

        broadcastMessage(ServerOperationConstants.WINNER_ANNOUNCEMENT);

        if(winner.size() >= 2) {
            String namesOfPlayer = winner.stream().map(Player::getPlayerName).collect(Collectors.joining(", "));
            System.out.println("There is a draw between " + announcePlayersWithDrawScore(namesOfPlayer));
            broadcastMessage("There is a draw between " + announcePlayersWithDrawScore(namesOfPlayer));
        } else {
            System.out.println(winner.get(0).getPlayerName() + " is the winner!");
            broadcastMessage(winner.get(0).getPlayerName() + " is the winner!");
        }
        System.exit(0);
    }

    private synchronized String announcePlayersWithDrawScore(String names) {
        String replace = ", ";
        String replacement = ", and ";
        int start = names.lastIndexOf(replace);
        return names.substring(0, start) +
                replacement +
                names.substring(start + replace.length()) + ".";
    }

}
