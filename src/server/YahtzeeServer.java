package server;

import client.Player;
import constants.ServerOperationConstants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

// Only display categories that you can score in!

// TODO: THE MESSAGES SENT IS IN WRONG ORDER
//       Causes it to process incorrectly

// TODO: Suspend the threads of the players when it is someone else's turn (block the inputs from the players)
// TODO: Develop a protocol that handles when clients leave the game without informing the server
// TODO: After suspending the threads, create a mechanism that avoid the deadlock
// TODO: Sketch the architecture and the protocols for the game (e.g. client requesting to have their turn)

public class YahtzeeServer {
    private ServerSocket socket;
    private int playerNumber = 0;
    private Scanner inputKeyboard;

    // The players
    private List<Player> players;
    private String startGameOrNotOption = "";
    private boolean startGame;
    private Broadcast broadcast;
    private ServerOperation serverOperation;

    private YahtzeeServer(int portNumber) throws IOException {
        // Handle only up to 50 connection requests
        this.socket = new ServerSocket(portNumber, 50);
        System.out.println("Yahtzee server initialised at port " + portNumber);
        this.inputKeyboard = new Scanner(System.in);
        this.players = Collections.synchronizedList(new ArrayList<>());
        runServer();
    }

    private void runServer() {
        while(true) {
            try {
                handlePlayerConnectionRequests();
                serverOperation = new ServerOperation(players);
                broadcast = new Broadcast(players);
                serverOperation.welcomePlayers();
                //serverOperation.sendPlayersScoreBoard();
                startGame();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void handlePlayerConnectionRequests() throws IOException {
        while(true) {
            if(playerNumber >= 3 && !startGame) {
                startGame = askStartGameOrNot();
                if(startGame) {
                    System.out.println("The game will now begin");
                    System.out.println("The game contains " + playerNumber + " players.");
                    break;
                } else {
                    startGameOrNotOption = "";
                }
            }
            acceptPlayerConnection();
        }
    }

    private void acceptPlayerConnection() throws IOException {
        // The server will need to accept the connection of client
        // even if the game has started
        // If the game has started, then send a message to the client
        // informing them the game has started and they cannot join session
        players.add(new Player(socket.accept()));

        Socket connectionSocket =  players.get(playerNumber).getSocket();
        if(!startGame)
            System.out.println("Accepted a connection from " + connectionSocket.getInetAddress() + ":" + connectionSocket.getPort() + " at port " + connectionSocket.getLocalPort());
        else
            System.out.println("Game has started, but a client has been denied to join the game from" + connectionSocket.getInetAddress() + ":" + connectionSocket.getPort() + " at port " + connectionSocket.getLocalPort());
        ObjectOutputStream outputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
        ObjectInputStream inputStream = new ObjectInputStream(connectionSocket.getInputStream());

        // If there are enough players and the game has started
        // decine the connection requests
        if(playerNumber >= 3 && startGame) {
            String message = "Cannot join game, as game is currently being played. Please try again later.";
            outputStream.writeObject(message);
            players.remove(players.size()-1);
        } else {
            // Write the player number to the client
            players.get(playerNumber).setInputStream(inputStream);
            players.get(playerNumber).setOutputStream(outputStream);
            players.get(playerNumber).setPlayerNumber(playerNumber + 1);
            players.get(playerNumber).setPlayerName("Player " + (playerNumber + 1));
            String message = "You are Player " + (++playerNumber);

            outputStream.writeObject(message);
            System.out.println("Total players in game: " + playerNumber);
        }
        System.out.println(players);
    }

    private boolean askStartGameOrNot() {
        System.out.println("Three players (or more) has joined the game");
        while(!(startGameOrNotOption.equalsIgnoreCase("n") || startGameOrNotOption.equals("y"))) {
            System.out.print("Would you like to start the game (y/n)? ");
            startGameOrNotOption = inputKeyboard.nextLine();
        }
        return startGameOrNotOption.equals("y");
    }

    private synchronized void startGame() {
        int totalAcks = 0;
        for(int roundNumber = 1; roundNumber <= 13; ) {
            // Send the round number to all the players
            broadcast.broadcastPlayersRoundNumber(roundNumber);
            broadcast.broadcastDicesRolledByPlayers(serverOperation.getPlayersDicesRolled());
            serverOperation.sendPlayersScoreBoard();

            for(Player player : players) {
                // Inform each player whose turn it is right now
                broadcast.broadcastMessage(player, ServerOperationConstants.OTHER_PLAYER_TURN);
                broadcast.broadcastMessage(player, player.getPlayerName());

                System.out.println("-----------Begin Transaction-----------");

                // Let the player have their turn
                serverOperation.grantPlayerTurn(player);

                // Block until the user sends the PLAYER_FINISH_TURN message to the server
                String messageFromClient = player.readMessage();
                System.out.println(messageFromClient + " from " + player.getPlayerName());
                if(messageFromClient.equals(ServerOperationConstants.PLAYER_FINISH_TURN)) {
                    totalAcks++;
                    System.out.println("-----------End Transaction-----------\n");
                }

                if(totalAcks == players.size()) {
                    System.out.println("Got all acks for round " + roundNumber);
                    totalAcks = 0;
                    broadcast.broadcastPlayerScoreTotal();
                    roundNumber++;
                }
            }
        }
        broadcast.declareWinner();
    }

    public static void main(String[] args) throws IOException {
        new YahtzeeServer(4321);
    }
}