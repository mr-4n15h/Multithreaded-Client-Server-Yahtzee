package client;

import score.ScoreCheck;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

public class Player implements Runnable {
    private PlayerScoreBoard scoreBoard;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private int playerNumber;
    private String playerName;
    private int[] dicesRolled;
    private Dices dices;
    private ScoreCheck scoreCheck;
    private TurnManager turnManager;

    public Player(Socket socket) {
        this.socket = socket;
        this.scoreBoard = new PlayerScoreBoard();
        this.dices = new Dices();
        this.scoreCheck = new ScoreCheck(dices);
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " says hi!");

    }

    public void setTurnManager(TurnManager turnManager) {
        this.turnManager = turnManager;
    }

    public TurnManager getTurnManager() {
        return turnManager;
    }

    public String getRolledDices() {
        dicesRolled = dices.rollDices();
        return Arrays.toString(dicesRolled);
    }

    public void sendMessage(String message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readMessage() {
        try {
            return (String) input.readObject();
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int[] readArrayMessage() {
        try {
            return (int[]) input.readObject();
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getScoreBoard() {
        return scoreBoard.toString();
    }

    public Socket getSocket() {
        return socket;
    }

    public void setInputStream(ObjectInputStream input) {
        this.input = input;
    }

    public void setOutputStream(ObjectOutputStream output) {
        this.output = output;
    }

    public ObjectInputStream getInputStream() {
        return input;
    }

    public ObjectOutputStream getOutputStream() {
        return output;
    }

    public void setScore(ScoreType scoreType, int points) {
        scoreBoard.setScore(scoreType, points);
    }


    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public String getPlayerName() {
        return playerName;
    }


    public void setPlayerNumber(int num) {
        this.playerNumber = num;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }


    public int getTotalScore() {
        synchronized (this) {
            int bonusTotal = scoreBoard.getUpperSectionBonus();
            if (bonusTotal != 0)
                scoreBoard.setScore(ScoreType.BONUS, bonusTotal);
            return scoreBoard.total();
        }
    }

    public String getWhatCanBeScored() {
        return scoreCheck.getScorablePoints();
    }

    public int getCategoryScoredPoint(String index) {
        return scoreCheck.getScorePoint(Integer.valueOf(index));
    }

    public String getPlayerScoreBoard() {
        return scoreBoard.toString();
    }

    public Dices getDices() {
        return dices;
    }

}
