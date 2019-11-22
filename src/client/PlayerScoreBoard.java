package client;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerScoreBoard implements Serializable
{
    private LinkedHashMap<ScoreType, Integer> playerScoreBoard;
    private String result;

    PlayerScoreBoard() {
        playerScoreBoard = new LinkedHashMap<>();
        setScoreTypesInPlayerScoreBoard();
    }

    private void setScoreTypesInPlayerScoreBoard() {
        final ScoreType[] SCORE_TYPES = ScoreType.values();
        for(int i = 0; i < SCORE_TYPES.length; i++) {
            playerScoreBoard.put(SCORE_TYPES[i], 0);
        }
    }

    void setScore(ScoreType type, int value) {
        playerScoreBoard.put(type, value);
    }

    // Function to test the (keys, values)
    private void formatBoardString() {
        StringBuilder formattedPlayerScoreBoard = new StringBuilder();
        for(Map.Entry<ScoreType, Integer> item : playerScoreBoard.entrySet()) {
            String scoreType = item.getKey().getName();
            String scorePoints = item.getValue().toString();
            final String SCORE_ROW = "[" + scoreType + ": " + scorePoints + "]";
            formattedPlayerScoreBoard.append(SCORE_ROW);
        }
        formattedPlayerScoreBoard.append("\n");
        result = formattedPlayerScoreBoard.toString();
    }

    public int total() {
        int playerTotalScore = 0;
        for(Map.Entry<ScoreType, Integer> item : playerScoreBoard.entrySet()) {
            int pointScored = item.getValue();
            playerTotalScore += pointScored;
        }
        return playerTotalScore;
    }

    public synchronized int getUpperSectionBonus() {
        int playerTotalUpperScore = 0;
        int counter = 0;
        for(Map.Entry<ScoreType, Integer> item : playerScoreBoard.entrySet()) {
            if(counter == 6)
                break;
            int pointScored = item.getValue();
            playerTotalUpperScore += pointScored;
            counter++;
        }
        // Testing
        return playerTotalUpperScore >= 63 ? 35 : 0;
    }


    @Override
    public String toString() {
        formatBoardString();
        return result;
    }
}
