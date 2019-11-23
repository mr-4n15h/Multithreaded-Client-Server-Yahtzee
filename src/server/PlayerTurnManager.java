package server;

import client.Player;

import java.util.List;

class PlayerTurnManager {
    private boolean isAccessing;
    private List<Player> players;
    private int playerIndex;

    PlayerTurnManager(List<Player> players) {
        this.isAccessing = false;
        this.players = players;
    }
    private synchronized void increasePlayerIndex() {
        playerIndex = playerIndex == players.size() - 1 ? 0 : ++playerIndex;
    }

    synchronized Player getPlayer() {
        return players.get(playerIndex);
    }

    synchronized int getPlayerIndex() {
        return playerIndex;
    }

    synchronized void requestTurn() throws InterruptedException {
        while(isAccessing) {
            wait();
        }
        isAccessing = true;
        System.out.println(getPlayer().getPlayerName() + " got a lock!");
        // Print the name of the players that are waiting for lock
        int currentPlayerIndex = playerIndex;
        for(int i = 0; i < players.size(); i++) {
            if(i != currentPlayerIndex) {
                String playerWaitingForLock = players.get(i).getPlayerName();
                System.out.println(playerWaitingForLock + " is waiting for lock! ");
            }
        }
    }

    synchronized void releaseTurn() {
        isAccessing = false;
        notifyAll();
        System.out.println(getPlayer().getPlayerName() + " released the lock!");
        increasePlayerIndex();
    }
}
