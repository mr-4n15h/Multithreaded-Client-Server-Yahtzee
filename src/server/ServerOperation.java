package server;

import client.Player;
import client.ScoreType;
import constants.ServerOperationConstants;

import java.util.Arrays;
import java.util.List;

// TODO: Fix the protocol
//       Record each operation


class ServerOperation {
    private List<Player> players;

    ServerOperation(List<Player> players) {
        this.players = players;
    }

    void welcomePlayers() {
        for(Player player : players) {
            player.sendMessage("\n\n\t\t\t\t\t\t\t\t\tGame will start now!!!\t\t\t\t\t\t\t\n");
        }
    }

    private String getScoreBoardOfAllPlayers() {
        // Create a StringBuilder to store the score board of the players
        StringBuilder scoreBoardOfAllPlayers = new StringBuilder();
        for(Player player : players) {
            final String RESULT = player.getPlayerName() + ": " + player.getPlayerScoreBoard();
            scoreBoardOfAllPlayers.append(RESULT);
        }
        return scoreBoardOfAllPlayers.toString();
    }

    String getPlayersDicesRolled() {
        StringBuilder result = new StringBuilder();
        for(Player player : players) {
            result.append(player.getPlayerName()).append(" rolled: ").append(player.getRolledDices()).append("\n");
        }
        return result.toString();
    }

    void sendPlayersScoreBoard() {
        // Send all the players of the scores
        for(Player player : players) {
            String scoreBoard = getScoreBoardOfAllPlayers();
            String formattedScoreBoard = scoreBoard.replaceAll(player.getPlayerName(), "You");
            player.sendMessage(formattedScoreBoard);
        }
    }

    void grantPlayerTurn(Player player) {
        player.sendMessage(ServerOperationConstants.PLAYER_TURN);
        System.out.println(ServerOperationConstants.PLAYER_TURN + " to " + player.getPlayerName()); // Inform player that it is their turn
        final String REPLY_FROM_CLIENT = player.readMessage();
        System.out.println(REPLY_FROM_CLIENT + " from " + player.getPlayerName());     // This is the response from server
        if (REPLY_FROM_CLIENT.equals(ServerOperationConstants.PLAYER_REQUEST_TURN)) {
            player.sendMessage(ServerOperationConstants.PLAYER_GRANTED_TURN);
            System.out.println(ServerOperationConstants.PLAYER_GRANTED_TURN + " to " + player.getPlayerName());
            letPlayerRerollDices(player);
        }
    }

    private void letPlayerRerollDices(Player player) {
        boolean rerollingDices = true;
        final Broadcast broadcast = new Broadcast(players);
        int rerollChances = 3;

        player.sendMessage(ServerOperationConstants.PLAYER_REROLL_DICES);
        System.out.println(ServerOperationConstants.PLAYER_REROLL_DICES + " to " + player.getPlayerName());

        while(rerollingDices) {
            System.out.println("Waiting for a message from " + player.getPlayerName());
            String messageFromPlayer = player.readMessage();
            System.out.println(messageFromPlayer + " from " + player.getPlayerName());

            // Player has finished rerolling dice
            if(messageFromPlayer.equals(ServerOperationConstants.PLAYER_FINISH_REROLL_DICES)) {
                broadcast.broadcastMessage(player, player.getPlayerName()+ " finished rerolling\n");
                rerollingDices = false;

                // Here the player has rerolled dice again
            } else if(messageFromPlayer.equals(ServerOperationConstants.PLAYER_REROLLED_DICES)) {
                rerollChances--;
                broadcast.broadcastMessage(player, player.getPlayerName() + " rerolled and has " + rerollChances + " chances to reroll");

                // Player
            } else if(messageFromPlayer.equals(ServerOperationConstants.PLAYER_NUMBER_OF_DICES_REROLLED)) {
                String numberOfDicesToReroll = player.readMessage();
                int[] dicesIndexToReroll = player.readArrayMessage();
                player.getDices().rerollDices(dicesIndexToReroll);
                broadcast.broadcastMessage(player, player.getPlayerName() + " chose " + numberOfDicesToReroll + " dice(s) to reroll");
                broadcast.broadcastMessage(player, player.getPlayerName() + " chose " + Arrays.toString(dicesIndexToReroll) + " dice index(es) to reroll");
                player.getDices().rerollDices(dicesIndexToReroll);

                // Obtain the new rerolled dices
                final String NEW_REROLLED_DICES = Arrays.toString(player.getDices().getRolledDices());

                // Send the player the newly rerolled dices
                player.sendMessage(ServerOperationConstants.PLAYER_NEW_REROLLED_DICE);
                player.sendMessage(NEW_REROLLED_DICES);

                // Send the rerolled dices to every players
                broadcast.broadcastMessage(player, ServerOperationConstants.OTHER_PLAYER_REROLL_DICES);
                broadcast.broadcastMessage(player, "After rerolling, " + player.getPlayerName() + "'s new dices are " + NEW_REROLLED_DICES);

                // Player has requested what they can score in (the category)
            } else if(messageFromPlayer.equals(ServerOperationConstants.PLAYER_GET_WHAT_CAN_BE_SCORED)) {
                final String WHAT_CAN_BE_SCORED = player.getWhatCanBeScored();
                player.sendMessage(WHAT_CAN_BE_SCORED);

                broadcast.broadcastMessage(player, ServerOperationConstants.OTHER_PLAYER_GET_WHAT_CAN_BE_SCORED);
                broadcast.broadcastMessage(player, player.getPlayerName() + " can score in the following: " + WHAT_CAN_BE_SCORED);

                // Player has finished their turn
            } else if(messageFromPlayer.equals(ServerOperationConstants.PLAYER_FINISH_TURN)) {
                System.out.println(messageFromPlayer);
            } else if(messageFromPlayer.equals(ServerOperationConstants.PLAYER_CHOOSE_SCORING_CATEGORY)) {
                String categoryChosenByPlayer = player.readMessage();

                int pointScored = player.getCategoryScoredPoint(categoryChosenByPlayer);
                final String NAME_OF_CATEGORY_CHOSEN = ScoreType.getScoreType(categoryChosenByPlayer).getName();

                player.setScore(ScoreType.getScoreType(categoryChosenByPlayer), pointScored);
                System.out.println("Scored point: " + pointScored);
                System.out.println(player.getPlayerName() + " picked to score in " + NAME_OF_CATEGORY_CHOSEN);
                System.out.println("Player's new scoreboard: " + player.getScoreBoard());

                broadcast.broadcastMessage(player, ServerOperationConstants.OTHER_PLAYER_CHOSE_SCORING_CATEGORY);
                broadcast.broadcastMessage(player, player.getPlayerName() + " chose to score in " + NAME_OF_CATEGORY_CHOSEN + " giving them " + pointScored + " points");
            }
        }
    }
}