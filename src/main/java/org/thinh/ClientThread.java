package org.thinh;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author Thinh Le
 * <h1>ClientThread</h1>
 * <p>This class implements the client connection handler
 * The server connects with each client on its own connection handler thread</p>
 */

public class ClientThread extends Thread {
    protected Socket socket = null;
    private DataOutputStream dataOutputStream = null;
    private DataInputStream dataInputStream = null;
    public static int numGuesses = 0;
    public static String targetWord = "";
    public static String currentWord = "";
    public static ArrayList<String> guessedChar = null;

    protected int MAX_GUESSES = 6;

    /**
     * Create the connection thread between client and server
     * @param socket The client socket
     * @param targetWord The shared target word from the server
     * @param currentWord The shared current state of target word from the server
     * @param numGuesses The shared number of guesses from the server
     * @param guessedChar The shared list of all guessed letters from the server
     */
    public ClientThread(Socket socket, String targetWord, String currentWord, int numGuesses, ArrayList<String> guessedChar) {
        super();
        this.socket = socket;
        this.targetWord = targetWord;
        this.currentWord = currentWord;
        this.numGuesses = numGuesses;
        this.guessedChar = new ArrayList<>(guessedChar);
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("IOException while opening a read/write connection");
        }
    }

    /**
     * Run the thread. When this function ends, the thread ends, connection is destroyed
     */
    public void run() {
        boolean endOfSession = false;
        // If endOfSession is true, break while loop and disconnect
        try {
            while (!endOfSession) {
                 endOfSession = processCommand();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle all of the commands and features requested by the client
     * @return boolean True if the request is closing the connection
     * or error in reading from the client, False for other features
     * @throws IOException
     */
    public boolean processCommand() throws IOException {
        synchronized (this) {
            String command = null;
            try {
                command = dataInputStream.readUTF();
            } catch (IOException e) {
                System.err.println("Error reading command from socket");
                return true;
            }

            // Get the current state of the target word
            if (command.equalsIgnoreCase("CURRENT-WORD")) {
                dataOutputStream.writeUTF(currentWord);
                return false;
            }

            // Get the target word
            if (command.equalsIgnoreCase("TARGET-WORD")) {
                dataOutputStream.writeUTF(targetWord);
                return false;
            }

            // Get the current number of guesses
            if (command.equalsIgnoreCase("GUESSED-NUM")) {
                dataOutputStream.writeUTF(String.valueOf(numGuesses));
                return false;
            }

            // Get the list of letters been guessed
            if (command.equalsIgnoreCase("GUESSED-CHAR")) {
                for (String s : guessedChar) {
                    dataOutputStream.writeUTF(s);
                }
                dataOutputStream.writeUTF("end()");
                return false;
            }

            // Send the guess letter to the server
            if (command.equalsIgnoreCase("GUESS-WORD")) {
                String guessedChar = dataInputStream.readUTF();

                // This case is when the player guessing the hold word and correct
                if (targetWord.equalsIgnoreCase(guessedChar)) {
                    currentWord = targetWord;
                    dataOutputStream.writeUTF("DONE");
                    return false;
                }

                // This case is when the player guessing single letter and correct
                this.guessedChar.add(guessedChar);
                if (targetWord.toLowerCase().contains(guessedChar)) {
                    String temp[] = targetWord.split("");
                    for (int i = 0; i < temp.length; i++) {
                        if (temp[i].equalsIgnoreCase(guessedChar)) {
                            currentWord = currentWord.substring(0, i) + guessedChar + currentWord.substring(i+1);
                        }
                    }
                    dataOutputStream.writeUTF("CORRECT!");
                    return false;
                }

                // This case is when player's guess is not correct
                numGuesses++;
                // This case to check if number of guesses exceed max number of guesses
                // If exceeds, player lose
                if (numGuesses >= MAX_GUESSES) {
                    dataOutputStream.writeUTF("DONE");
                    return false;
                }
                dataOutputStream.writeUTF("WRONG! Try again");
                return false;
            }

            // Get winning condition of the game
            if (command.equalsIgnoreCase("IS-WIN")) {
                if (currentWord.equalsIgnoreCase(targetWord)) {
                    dataOutputStream.writeUTF("CONGRATULATION!");
                    return false;
                } else if (numGuesses >= MAX_GUESSES) {
                    dataOutputStream.writeUTF("OUT OF GUESSES!");
                    return false;
                } else {
                    dataOutputStream.writeUTF("CONTINUE");
                    return false;
                }
            }

            // Disconnect from the server
            if (command.equalsIgnoreCase("CLOSE")) {
                // return true back to 'run' method, so endOfSession breaks the while loop
                return true;
            }
        }
        return true;
    }
}
