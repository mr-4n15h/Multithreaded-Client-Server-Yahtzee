package server;

import java.util.Scanner;

public class YahtzeeClientTurnManager {
    private boolean isAccessing;
    private Scanner input;

    public YahtzeeClientTurnManager() {
        this.isAccessing = false;
    }

    synchronized void requestTurn() throws InterruptedException {
        while(isAccessing) {
            wait();
        }
        isAccessing = true;
    }

    synchronized void releaseTurn() throws InterruptedException {
        isAccessing = false;
        notifyAll();
    }
}
