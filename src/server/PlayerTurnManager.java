package server;

import client.Player;

import java.util.List;

public class PlayerTurnManager {
    private boolean isAccessing;
    private List<Player> players;

    public PlayerTurnManager(List<Player> players) {
        this.isAccessing = false;
        this.players = players;
    }

    synchronized void requestTurn() throws InterruptedException {
        Thread currentT = Thread.currentThread();
        while(isAccessing) {
            wait();
        }
        isAccessing = true;
        System.out.println(currentT.getName() + " got a lock!");
    }

    synchronized void releaseTurn() {
        Thread currentT = Thread.currentThread();
        isAccessing = false;
        notifyAll();
        System.out.println(currentT.getName() + " released the lock!");
    }
}
