package org.thinh;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * @author Thinh Le
 * <h1>Client</h1>
 * <p>This class implements the client object handling the backend features
 * for connecting with the server</p>
 */

public class Client {
    private Socket socket = null;
    private DataOutputStream dataOutputStream = null;
    private DataInputStream dataInputStream = null;

    private static String SERVER_ADDRESS = null;
    private static int SERVER_PORT;

    /**
     * Create Client object with a socket connect to the server
     * Create data IO streams for the socket to transfer and receive data from server
     * @param serverAddress Server's IP address
     * @param port Server's port
     */
    public Client (String serverAddress, int port) {
        try {
            // Create socket
            SERVER_ADDRESS = serverAddress;
            SERVER_PORT = port;
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_ADDRESS);
        } catch (IOException ex) {
            System.err.println("IOException while connecting to server: " + SERVER_ADDRESS);
        }
        if (socket == null) {
            System.err.println("Socket is null");
        }
        try {
            // Create data IO streams
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("IOException while opening a read/write connection");
        }
    }

    /**
     * Get the current state of the target word from the server
     * @return String The current state of the target word
     */
    public String getCurrentWord() {
        String word = "";
        try {
            dataOutputStream.writeUTF("CURRENT-WORD");
            word = dataInputStream.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return word;
        }
    }

    /**
     * Get the target word from the server
     * @return String The target word
     */
    public String getTargetWord() {
        String targetWord = "";
        try {
            dataOutputStream.writeUTF("TARGET-WORD");
            targetWord = dataInputStream.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return targetWord;
        }
    }

    /**
     * Get the current number of guesses
     * @return int The current number of guesses
     */
    public int getNumGuesses() {
        int numGuesses = 0;
        try {
            dataOutputStream.writeUTF("GUESSED-NUM");
            numGuesses = Integer.parseInt(dataInputStream.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return numGuesses;
        }
    }

    /**
     * Get the list of letters been guessed
     * @return ArrayList The list of all guessed characters
     */
    public ArrayList<String> getGuessedChar() {
        ArrayList<String> guessedChar = new ArrayList<>();
        try {
            dataOutputStream.writeUTF("GUESSED-CHAR");
            String message;
            while (true) {
                if (dataInputStream.available() > 0) {
                    message = dataInputStream.readUTF();
                    if (message.equalsIgnoreCase("end()")) {
                        break;
                    }
                    guessedChar.add(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return guessedChar;
        }
    }

    /**
     * Send the client's guess to the server
     * @param guess The guess letter or word of the client
     * @return String The message about the result of the guess (correct, incorrect, or win/lose)
     */
    public String sendGuess(String guess) {
        String message = "";
        try {
            dataOutputStream.writeUTF("GUESS-WORD");
            dataOutputStream.writeUTF(guess);
            message = dataInputStream.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return message;
        }
    }

    /**
     * Get winning condition of the game
     * @return String The message about the winning condition of the game (win, lose, or continue)
     */
    public String isWin() {
        String message = "";
        try {
            dataOutputStream.writeUTF("IS-WIN");
            message = dataInputStream.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return message;
        }
    }

    /**
     * Disconnect from the server, close socket
     */
    public void closeClient() {
        try {
            dataOutputStream.writeUTF("CLOSE");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
