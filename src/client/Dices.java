package client;

import java.security.SecureRandom;
import java.util.Arrays;

public class Dices {
    private SecureRandom engine;
    private int[] dicesRolled;
    private final int ROLL_UPPER_BOUND = 6;

    Dices() {
        engine = new SecureRandom();
        dicesRolled = new int[5];
    }

    private int rollDice() {
        return engine.nextInt(ROLL_UPPER_BOUND) + 1;
    }

    public void rerollDices(int[] indexes) {
        for(int index : indexes) {
            dicesRolled[index - 1] = rollDice();
        }
    }

    int[] rollDices() {
        dicesRolled = engine.ints(5, 1, ROLL_UPPER_BOUND).toArray();
        return dicesRolled;
    }

    public int[] getRolledDices() {
        return dicesRolled;
    }

    @Override
    public String toString() {
        return Arrays.toString(dicesRolled);
    }
}
