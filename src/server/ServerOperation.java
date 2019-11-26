package server;

import client.Player;
import client.ScoreType;
import constants.ServerOperationConstants;

import java.util.Arrays;
import java.util.List;

class ServerOperation {
    private List<Player> players;
    private Broadcast broadcast;

    ServerOperation(List<Player> players) {
        this.players = players;
        this.broadcast = new Broadcast(players);
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
        int rerollChances = 3;

        player.sendMessage(ServerOperationConstants.PLAYER_REROLL_DICES);
        System.out.println(ServerOperationConstants.PLAYER_REROLL_DICES + " to " + player.getPlayerName());

        while(rerollingDices) {
            System.out.println("Waiting for a message from " + player.getPlayerName());
            String messageFromPlayer = player.readMessage();
            System.out.println(messageFromPlayer + " from " + player.getPlayerName());
            switch (messageFromPlayer) {
                case ServerOperationConstants.PLAYER_FINISH_REROLL_DICES:
                    broadcast.broadcastMessage(player, player.getPlayerName() + " finished rerolling\n");
                    rerollingDices = false;
                    break;
                case ServerOperationConstants.PLAYER_REROLLED_DICES:
                    rerollChances--;
                    broadcast.broadcastMessage(player, player.getPlayerName() + " rerolled and has " + rerollChances + " chances to reroll");
                    break;
                case ServerOperationConstants.PLAYER_NUMBER_OF_DICES_REROLLED:
                    rerollDice(player);
                    break;
                case ServerOperationConstants.PLAYER_GET_WHAT_CAN_BE_SCORED:
                    displayWhatCanBeScored(player);
                    break;
                case ServerOperationConstants.PLAYER_FINISH_TURN:
                    System.out.println(messageFromPlayer);
                    break;
                case ServerOperationConstants.PLAYER_CHOOSE_SCORING_CATEGORY:
                    updateCategoryChosen(player);
                    break;
            }
        }
    }

    private void rerollDice(Player player) {
        String numberOfDicesToReroll = player.readMessage();     // Get the number of dice to reroll
        int[] dicesIndexToReroll = player.readArrayMessage();	 // Get the dice indexes to change
        player.getDices().rerollDices(dicesIndexToReroll);		//  Reroll dice by passing the indexes
        broadcast.broadcastMessage(player, player.getPlayerName() + " chose " + numberOfDicesToReroll + " dice(s) to reroll");
        broadcast.broadcastMessage(player, player.getPlayerName() + " chose " + Arrays.toString(dicesIndexToReroll) + " dice index(es) to reroll");

        // Obtain the new rerolled dices
        final String NEW_REROLLED_DICES = Arrays.toString(player.getDices().getRolledDices());
        // Send the player the newly rerolled dices
        player.sendMessage(ServerOperationConstants.PLAYER_NEW_REROLLED_DICE);
        player.sendMessage(NEW_REROLLED_DICES);
        // Send the rerolled dices to every players
        broadcast.broadcastMessage(player, ServerOperationConstants.OTHER_PLAYER_REROLL_DICES);
        broadcast.broadcastMessage(player, "After rerolling, " + player.getPlayerName() + "'s new dices are " + NEW_REROLLED_DICES);
    }

    private void displayWhatCanBeScored(Player player) {
        final String WHAT_CAN_BE_SCORED = player.getWhatCanBeScored();
        player.sendMessage(WHAT_CAN_BE_SCORED);
        // Inform other players what the user can score in
        broadcast.broadcastMessage(player, ServerOperationConstants.OTHER_PLAYER_GET_WHAT_CAN_BE_SCORED);
        broadcast.broadcastMessage(player, player.getPlayerName() + " can score in the following: " + WHAT_CAN_BE_SCORED);
    }

    private void updateCategoryChosen(Player player) {
        String categoryChosenByPlayer = player.readMessage();
        int pointScored = player.getCategoryScoredPoint(categoryChosenByPlayer);
        final String NAME_OF_CATEGORY_CHOSEN = ScoreType.getScoreType(categoryChosenByPlayer).getName();
        player.setScore(ScoreType.getScoreType(categoryChosenByPlayer), pointScored);
        updatePlayersCategoryChosen(player, NAME_OF_CATEGORY_CHOSEN, pointScored);
    }

    private void updatePlayersCategoryChosen(Player player, String categoryChosen, int pointScored) {
        System.out.println("Scored point: " + pointScored);
        System.out.println(player.getPlayerName() + " picked to score in " + categoryChosen);
        System.out.println("Player's new scoreboard: " + player.getScoreBoard());
        broadcast.broadcastMessage(player, ServerOperationConstants.OTHER_PLAYER_CHOSE_SCORING_CATEGORY);
        broadcast.broadcastMessage(player, player.getPlayerName() + " chose to score in " + categoryChosen + " giving them " + pointScored + " points");
    }
}