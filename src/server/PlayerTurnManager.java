package server;

import client.Player;

import java.util.List;

public class PlayerTurnManager {
    private boolean isAccessing;
    private List<Player> players;
    private int playerIndex;

    public PlayerTurnManager(List<Player> players) {
        this.isAccessing = false;
        this.players = players;
    }

    synchronized void increasePlayerIndex() {
        playerIndex = playerIndex == players.size() - 1 ? 0 : ++playerIndex;
    }

    private synchronized Player getPlayer() {
        return players.get(playerIndex);
    }

    synchronized void requestTurn() throws InterruptedException {
        while(isAccessing) {
            wait();
        }
        isAccessing = true;
        System.out.println(getPlayer().getPlayerName() + " got a lock!");
        increasePlayerIndex();
    }

    synchronized void releaseTurn() {
        isAccessing = false;
        notifyAll();
        System.out.println(getPlayer().getPlayerName() + " released the lock!");
    }
}
