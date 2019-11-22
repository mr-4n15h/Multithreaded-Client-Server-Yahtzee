package client;

import java.util.ArrayList;

public enum ScoreType {
    ONES("Ones"),
    TWOS("Twos"),
    THREES("Threes"),
    FOURS("Fours"),
    FIVES("Fives"),
    SIXES("Sixes"),
    BONUS("Bonus"),

    THREE_OF_A_KIND("Three of a kind"),
    FOUR_OF_A_KIND("Four of a kind"),
    FULL_HOUSE("Full house"),
    SMALL_STRAIGHT("Small straight"),
    LARGE_STRAIGHT("Large straight"),
    CHANCE("Chance"),
    YAHTZEE("Yahtzee");


    public static ArrayList<ScoreType> SCORE_TYPES;

    final static int STARTING_UPPER_CATEGORY_INDEX= ONES.ordinal();
    final static int ENDING_UPPER_CATEGORY_INDEX = SIXES.ordinal();

    final static int STARTING_LOWER_CATEGORY_INDEX= THREE_OF_A_KIND.ordinal();
    final static int ENDING_LOWER_CATEGORY_INDEX = YAHTZEE.ordinal();

    static {
        initialiseScores();
    }

    private static void initialiseScores() {
        SCORE_TYPES = new ArrayList<>();
        addUpperCategories();
        addLowerCategories();
    }

    public static ScoreType getScoreType(String index) {
        return SCORE_TYPES.get(Integer.valueOf(index) - 1);
    }

    private static void addUpperCategories() {
        final ScoreType[] SCORES = ScoreType.values();
        for(int i = STARTING_UPPER_CATEGORY_INDEX; i <= ENDING_UPPER_CATEGORY_INDEX; i++) {
            SCORE_TYPES.add(SCORES[i]);
        }
    }

    private static void addLowerCategories() {
        final ScoreType[] SCORES = ScoreType.values();
        for(int i = STARTING_LOWER_CATEGORY_INDEX; i <= ENDING_LOWER_CATEGORY_INDEX; i++) {
            SCORE_TYPES.add(SCORES[i]);
        }
    }

    private String name;

    ScoreType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}