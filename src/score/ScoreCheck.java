package score;

import client.Dices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ScoreCheck {
    private Dices diceRolled;
    private StringBuilder scoreablePointsString;
    private ArrayList<Integer> pointsScored;

    public ScoreCheck(Dices dices) {
        this.diceRolled = dices;
    }

    public String getScorablePoints() {
        scoreablePointsString = new StringBuilder();
        pointsScored = new ArrayList<>();
        checkUpperSections();
        checkLowerSections();
        return scoreablePointsString.toString();
    }

    private static int checkSingleUpperSectionCombination(int[] dices, int score) {
        return IntStream.of(dices).filter(x -> x == score).sum();
    }

    // Upper Sections
    private synchronized void checkUpperSections() {
        int[] dices = diceRolled.getRolledDices();
        final int ONES_SUM = checkSingleUpperSectionCombination(dices, 1);
        final int TWOS_SUM = checkSingleUpperSectionCombination(dices, 2);
        final int THREES_SUM = checkSingleUpperSectionCombination(dices, 3);
        final int FOURS_SUM = checkSingleUpperSectionCombination(dices, 4);
        final int FIVES_SUM = checkSingleUpperSectionCombination(dices, 5);
        final int SIXES_SUM = checkSingleUpperSectionCombination(dices, 6);

        storePoints(ONES_SUM, TWOS_SUM, THREES_SUM, FOURS_SUM, FIVES_SUM, SIXES_SUM);

        scoreablePointsString.append("[").append("Ones: ").append(ONES_SUM).append("]");
        scoreablePointsString.append("[").append("Twos: ").append(TWOS_SUM).append("]");
        scoreablePointsString.append("[").append("Threes: ").append(THREES_SUM).append("]");
        scoreablePointsString.append("[").append("Fours: ").append(FOURS_SUM).append("]");
        scoreablePointsString.append("[").append("Fives: ").append(FIVES_SUM).append("]");
        scoreablePointsString.append("[").append("Sixes: ").append(SIXES_SUM).append("]");
        //System.out.println("Bonus: " + BONUS_SUM);
    }


    private synchronized void checkLowerSections() {
        int[] dices = diceRolled.getRolledDices();
        final int TOK = getThreeOfAKindScore(dices);
        final int FOK = getFourOfAKindScore(dices);
        final int FULL_HOUSE = getFullHouseScore(dices);
        int SMALL_STRAIGHT = getSmallStraightScore(dices);
        int LARGE_STRAIGHT = getLargeStraightScore(dices);
        int CHANCE = getChanceScore(dices);
        int YAHTZEE = getYahtzeeScore(dices);

        storePoints(TOK, FOK, FULL_HOUSE, SMALL_STRAIGHT, LARGE_STRAIGHT, CHANCE, YAHTZEE);

        scoreablePointsString.append("[").append("Three of a kind: ").append(TOK).append("]");
        scoreablePointsString.append("[").append("Four of a kind: ").append(FOK).append("]");
        scoreablePointsString.append("[").append("Full house: ").append(FULL_HOUSE).append("]");
        scoreablePointsString.append("[").append("Small straight: ").append(SMALL_STRAIGHT).append("]");
        scoreablePointsString.append("[").append("Large straight: ").append(LARGE_STRAIGHT).append("]");
        scoreablePointsString.append("[").append("Chance: ").append(CHANCE).append("]");
        scoreablePointsString.append("[").append("Yahtzee: ").append(YAHTZEE).append("]");
    }

    private void storePoints(int ... scores) {
        for(final int score : scores) {
            pointsScored.add(score);
        }
    }

    public int getScorePoint(int index) {
        return pointsScored.get(index - 1);
    }

    // Very important the array is sorted before it is returned to the user
    private static ArrayList<Integer> getDicesArrayList(int[] dicesRolled) {
        return IntStream.of(dicesRolled)
                .sorted()
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Working
    private static int getThreeOfAKindScore(int[] dices) {
        ArrayList<Integer> dicesRolledArrayList = getDicesArrayList(dices);
        for(Integer numberRolled : dicesRolledArrayList) {
            final boolean HAS_THREE_OF_A_KIND = Collections.frequency(dicesRolledArrayList, numberRolled) >= 3;
            if(HAS_THREE_OF_A_KIND)
                return IntStream.of(dices).sum();
        }
        return 0;
    }

    // Working
    private static int getFourOfAKindScore(int[] dices) {
        ArrayList<Integer> dicesRolledArrayList = getDicesArrayList(dices);
        for(Integer numberRolled : dicesRolledArrayList) {
            final boolean HAS_FOUR_OF_A_KIND = Collections.frequency(dicesRolledArrayList, numberRolled) >= 4;
            if(HAS_FOUR_OF_A_KIND)
                return IntStream.of(dices).sum();
        }
        return 0;
    }

    // Working
    private static int getFullHouseScore(int[] dices) {
        ArrayList<Integer> dicesRolledArrayList = getDicesArrayList(dices);
        final Integer NUMBER_WITH_THREE_OF_A_KIND = dicesRolledArrayList.stream().filter(x -> Collections.frequency(dicesRolledArrayList, x) >= 3).findFirst().orElse(0);
        if(NUMBER_WITH_THREE_OF_A_KIND != 0) {
            dicesRolledArrayList.removeIf(x -> x.equals(NUMBER_WITH_THREE_OF_A_KIND));
            if(dicesRolledArrayList.size() == 2 && (dicesRolledArrayList.get(0).equals(dicesRolledArrayList.get(1)))) {
                return 25;
            }
        }
        return 0;
    }

    private static ArrayList<Integer> getArrayListInRange(int start, int end) {
        return IntStream.rangeClosed(start, end).boxed().collect(Collectors.toCollection(ArrayList::new));
    }

    private static int getSmallStraightScore(int[] dices) {
        ArrayList<Integer> dicesRolledArrayList = getDicesArrayList(dices);
        ArrayList<Integer> combinationOne = getArrayListInRange(1, 4);   // [1,2,3,4]
        ArrayList<Integer> combinationTwo = getArrayListInRange(2, 5);   // [2,3,4,5]
        ArrayList<Integer> combinationThree = getArrayListInRange(3, 6); // [3,4,5,6]
        final boolean CONTAINS_A_COMBINATION = dicesRolledArrayList.containsAll(combinationOne) || dicesRolledArrayList.containsAll(combinationTwo) || dicesRolledArrayList.containsAll(combinationThree);
        return CONTAINS_A_COMBINATION ? 30 : 0;
    }

    private static int getLargeStraightScore(int[] dices) {
        ArrayList<Integer> dicesRolledArrayList = getDicesArrayList(dices);
        ArrayList<Integer> combinationOne = getArrayListInRange(1, 5);  // [1,2,3,4,5]
        ArrayList<Integer> combinationTwo = getArrayListInRange(2, 6);  // [2,3,4,5,6]
        final boolean CONTAINS_A_COMBINATION = dicesRolledArrayList.containsAll(combinationOne) || dicesRolledArrayList.containsAll(combinationTwo);
        return CONTAINS_A_COMBINATION ? 40 : 0;
    }

    private static int getChanceScore(int[] dices) {
        return IntStream.of(dices).sum();
    }

    private static int getYahtzeeScore(int[] dices) {
        ArrayList<Integer> dicesRolledArrayList = getDicesArrayList(dices);
        for(Integer numberRolled : dicesRolledArrayList) {
            final boolean HAS_YAHTZEE = Collections.frequency(dicesRolledArrayList, numberRolled) == 5;
            if(HAS_YAHTZEE)
                return 50;
        }
        return 0;
    }
}