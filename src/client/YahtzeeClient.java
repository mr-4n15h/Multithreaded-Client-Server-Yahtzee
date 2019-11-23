package client;

import constants.ServerOperationConstants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class YahtzeeClient extends Thread {
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Scanner inputKeyboard;
    private int rerollChances;
    private boolean gameHasEnded;
    private ArrayList<Integer> categoriesToScore;

    private YahtzeeClient() throws IOException {
        InetAddress localHost = InetAddress.getLocalHost();
        Socket socketConnection = new Socket(localHost, 4321);
        System.out.println("Client initialised: " + InetAddress.getLocalHost() + " at " + socketConnection.getLocalPort());
        output = new ObjectOutputStream(socketConnection.getOutputStream());
        input = new ObjectInputStream(socketConnection.getInputStream());
        inputKeyboard = new Scanner(System.in);
        categoriesToScore = new ArrayList<>();
        start();
    }

    @Override
    public void run() {
        try {
            printWelcomeMessage();
            while(!gameHasEnded) {
                // Receive a message from server
                respondToServerOperations();
            }
        }catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void printWelcomeMessage() {
        String messageFromServer = null;
        try {
            messageFromServer = (String) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(messageFromServer);
    }

    private void respondToServerOperations() throws IOException, ClassNotFoundException {
        while(!gameHasEnded) {
            String operationFromServer = (String) input.readObject();
            switch(operationFromServer) {
                case ServerOperationConstants.ROUND_NUMBER:
                    String roundNumber = (String) input.readObject();
                    System.out.println("==================[ Round " + roundNumber + " of 13" + " ]==================");
                    break;
                case ServerOperationConstants.OTHER_PLAYER_TURN:
                    String playerTurnName = (String) input.readObject();
                    System.out.println("It is " + playerTurnName + "'s turn");
                    break;
                case ServerOperationConstants.PLAYER_TURN:
                    output.writeObject(ServerOperationConstants.PLAYER_REQUEST_TURN);
                    String replyFromServer = (String) input.readObject();
                    if(replyFromServer.equals(ServerOperationConstants.PLAYER_GRANTED_TURN))
                        System.out.println("It is your turn");
                    break;
                case ServerOperationConstants.PLAYER_FINISH_TURN:
                    informServerTurnFinished();
                    break;
                case ServerOperationConstants.PLAYER_ROLL_DICES:
                    String diceRolled = (String) input.readObject();
                    System.out.println("You rolled: " + diceRolled);
                    break;
                case ServerOperationConstants.PLAYER_REROLL_DICES:
                    // Read the number of reroll chances the player has currently
                    rerollChances = 3;
                    rerollDices();
                    break;
                case ServerOperationConstants.OTHER_PLAYER_REROLL_DICES:
                case ServerOperationConstants.OTHER_PLAYER_CHOSE_SCORING_CATEGORY:
                case ServerOperationConstants.OTHER_PLAYER_GET_WHAT_CAN_BE_SCORED:
                    String messageFromServer = (String) input.readObject();
                    System.out.println(messageFromServer);
                    break;
                case ServerOperationConstants.GAME_END:
                    String messageFromServer2 = (String) input.readObject();
                    if(messageFromServer2.equals(ServerOperationConstants.WINNER_ANNOUNCEMENT)) {
                        String winnerName = (String) input.readObject();
                        System.out.println(winnerName);
                    }
                    gameHasEnded = true;
                    break;
                default:
                    System.out.println(operationFromServer);
                    break;
            }
        }
    }

    private boolean chooseRerollOption() {
        String optionChosen;
        boolean INVALID_CHOICE;
        do {
            System.out.println("\nTotal re-rolls remaining: " + rerollChances);
            System.out.print("Would you like to re-roll dice(s) (y/n)? ");
            optionChosen = inputKeyboard.nextLine();
            INVALID_CHOICE = !((optionChosen.equals("y") || optionChosen.equals("n")));
            if(INVALID_CHOICE)
                System.out.println("Please enter y or n.");
        } while(INVALID_CHOICE);
        return optionChosen.equals("y");
    }

    private void rerollDices() {
        do {
            final boolean OPTION_CHOSEN_IS_Y = chooseRerollOption();
            if(OPTION_CHOSEN_IS_Y && rerollChances >= 1) {
                // Send a message to the server to inform the player rerolled dices
                try {
                    output.writeObject(ServerOperationConstants.PLAYER_REROLLED_DICES);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Decrease the number of rerolling chances
                rerollChances--;

                // The number of dices to reroll
                final int NUMBER_OF_DICE_TO_REROLL = askNumberOfDicesToReroll();

                try {
                    output.writeObject(ServerOperationConstants.PLAYER_NUMBER_OF_DICES_REROLLED);
                    output.writeObject(String.valueOf(NUMBER_OF_DICE_TO_REROLL));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int[] dicesIndexToReroll = askRerollInputChoices(NUMBER_OF_DICE_TO_REROLL);
                // Send the index of the dices to change
                try {
                    // Array must be sent in int, not string
                    output.writeObject(dicesIndexToReroll);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Total number of rerolls remaining: " + rerollChances);

                // Print the new rerolled dices
                try {
                    String message = (String) input.readObject();
                    if(message.equals(ServerOperationConstants.PLAYER_NEW_REROLLED_DICE)) {
                        System.out.println("After rerolling, your dices are " + input.readObject());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                //System.out.println(Arrays.toString(dicesRolled));
                System.out.println();
            } else {
                break;
            }
        } while(rerollChances >= 1);
        // Once done, reset the re-roll chances to 3 for the next round
        informServerTurnFinished();
        System.out.print("Your turn is finished for this round, please wait until all the players have their turn\n\n\n");
        //inputKeyboard.nextLine();
    }

    // This method handles the bad input exception
    private int readInteger() {
        int enteredInteger = 0;
        try {
            enteredInteger = inputKeyboard.nextInt();
            inputKeyboard.nextLine();   // Remove the '\n' character
        } catch(InputMismatchException e) {
            inputKeyboard.nextLine();   // Remove the bad input
            System.out.println();
        }
        return enteredInteger;
    }

    private int[] askRerollInputChoices(int numberOfDicesToReroll) {
        int[] dicesNumberToReroll = new int[numberOfDicesToReroll];
        for(int i = 1; i <= numberOfDicesToReroll; ) {
            System.out.print("Select a dice: ");
            final int DICE_INDEX = readInteger();
            final boolean INVALID_CHOICE = (DICE_INDEX <= 0 || DICE_INDEX >= 6);
            if(INVALID_CHOICE) {
                System.out.println("Please enter an integer in [1,5] range");
            } else {
                dicesNumberToReroll[i - 1] = DICE_INDEX;
                i++;
            }
        }
        return dicesNumberToReroll;
    }

    private int askNumberOfDicesToReroll() {
        int numberOfDiceToReroll;
        boolean INVALID_CHOICE;
        do {
            System.out.print("Enter the number of dice(s) to reroll: ");
            numberOfDiceToReroll = readInteger();
            INVALID_CHOICE = (numberOfDiceToReroll <= 0 || numberOfDiceToReroll >= 6);
            if(INVALID_CHOICE)
                System.out.println("Please enter an integer in [1,5] range");
        } while(INVALID_CHOICE);
        return numberOfDiceToReroll;
    }

    private void askServerWhatCanBeScored() {
        try {
            output.writeObject(ServerOperationConstants.PLAYER_GET_WHAT_CAN_BE_SCORED);
            System.out.print("With your dice, you can score in the following: ");
            String scoreBoard = (String) input.readObject();
            System.out.println(scoreBoard);
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void chooseScoringCategory(int categoryChosen) {
        try {
            final String CATEGORY_INDEX = String.valueOf(categoryChosen);
            output.writeObject(ServerOperationConstants.PLAYER_CHOOSE_SCORING_CATEGORY);
            output.writeObject(CATEGORY_INDEX);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printCategoryToScoreInMessage(int number, String message) {
        if(!categoriesToScore.contains(number))
            System.out.println(message);
    }

    private void printUpperSectionScoringCategories() {
        printCategoryToScoreInMessage(1, "Enter 1 to select Ones");
        printCategoryToScoreInMessage(2, "Enter 2 to select Twos");
        printCategoryToScoreInMessage(3, "Enter 3 to select Threes");
        printCategoryToScoreInMessage(4, "Enter 4 to select Fours");
        printCategoryToScoreInMessage(5, "Enter 5 to select Fives");
        printCategoryToScoreInMessage(6, "Enter 6 to select Sixes");
    }

    private void printLowerSectionScoringCategories() {
        printCategoryToScoreInMessage(7, "Enter 7 to select Three of a Kind");
        printCategoryToScoreInMessage(8, "Enter 8 to select Four of a Kind");
        printCategoryToScoreInMessage(9, "Enter 9 to select Full House");
        printCategoryToScoreInMessage(10, "Enter 10 to select Small Straight");
        printCategoryToScoreInMessage(11, "Enter 11 to select Large Straight");
        printCategoryToScoreInMessage(12, "Enter 12 to select Chance");
        printCategoryToScoreInMessage(13, "Enter 13 to select Yahtzee");
    }

    private void printScoringCategories() {
        printUpperSectionScoringCategories();
        printLowerSectionScoringCategories();
    }

    private void chooseWhatToScoreIn() {
        boolean INVALID_CHOICE;
        printScoringCategories();
        do {
            System.out.print("Enter option to score in: ");
            int numberToScoreIn = readInteger();
            boolean numberToScoreInHasBeenScoredAlready = categoriesToScore.contains(numberToScoreIn);
            INVALID_CHOICE = (numberToScoreIn <= 0 || numberToScoreIn >= 14) || numberToScoreInHasBeenScoredAlready;
            if(numberToScoreInHasBeenScoredAlready) {
                System.out.println("This scoring category has already been scored in");
            } else if(INVALID_CHOICE) {
                System.out.println("Please enter an integer in [1,13] range");
            } else {
                categoriesToScore.add(numberToScoreIn);
                chooseScoringCategory(numberToScoreIn);
            }
        } while(INVALID_CHOICE);

    }

    private void informServerTurnFinished() {
        try {
            // What can be scored
            askServerWhatCanBeScored();
            chooseWhatToScoreIn();
            output.writeObject(ServerOperationConstants.PLAYER_FINISH_REROLL_DICES);
            output.writeObject(ServerOperationConstants.PLAYER_FINISH_TURN);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new YahtzeeClient();
    }
}