package client;

public class TurnManager {
    // These values are shared amongst the players
    // Each player thread will access these values
    private boolean isAccessing;

    private int playerIndex;
    private final int MAX_NUMBER_OF_PLAYERS;

    public TurnManager(int playersInGame) {
        this.isAccessing = false;
        this.MAX_NUMBER_OF_PLAYERS = playersInGame;
    }
    private synchronized void increasePlayerIndex() {
        playerIndex = playerIndex == MAX_NUMBER_OF_PLAYERS - 1 ? 0 : ++playerIndex;
    }

    public synchronized int getPlayerTurn() {
        return playerIndex;
    }

    public synchronized void requestTurn() throws InterruptedException {
        Thread me = Thread.currentThread();
        while(isAccessing) {
            wait();
        }
        System.out.println(me.getName());
        isAccessing = true;
    }

    public synchronized void releaseTurn() {
        isAccessing = false;
        notifyAll();
        increasePlayerIndex();
    }
}
