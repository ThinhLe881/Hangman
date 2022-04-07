package org.thinh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Thinh Le
 * <h1>Server</h1>
 * <p>This class implements the server of the game application</p>
 */

public class Server {
    protected Socket clientSocket = null;
    protected ServerSocket serverSocket = null;
    protected ClientThread thread = null;
    protected ClientThread[] threads = null;
    protected int numClient = 0;
    public static int numGuesses = 0;
    public String targetWord;
    public String currentWord;
    public static ArrayList<String> guessedChar = null;

    public static int MAX_CLIENTS = 4;
    protected int MAX_GUESSES = 6;
    public static int SERVER_PORT = 6868;
    public static String SERVER_ADDRESS = null;

    /**
     * Start the server
     */
    public Server() {
        try {
            // Set up server and display info for players to connect
            InetAddress localhost = InetAddress.getLocalHost();
            SERVER_ADDRESS = localhost.getHostAddress();
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("---------------------------------------------");
            System.out.println("Hangman Game Server is running");
            System.out.println("---------------------------------------------");
            System.out.println("Server address: " + SERVER_ADDRESS);
            System.out.println("Listening to port: " + SERVER_PORT);
            System.out.println("---------------------------------------------");
            threads = new ClientThread[MAX_CLIENTS];
            guessedChar = new ArrayList<>();

            /**
             * This is another thread run parallel with the server's main thread in order to
             * update the number of players, check if the game is finished
             * then reset the game info (target word, number of guesses,...)
             * to prepare for a new game
             */
            new Thread(() -> {
                while (true) {
                    for (int i = 0; i < numClient; i++) {
                        if (!threads[i].isAlive()) {
                            numClient--;
                            System.out.println("Online players: " + numClient);
                            System.out.println("---------------------------------------------");
                        }
                    }
                    if (numClient == 0) {
                        generateWord();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            while (true) {
                // This while loop will keep the server running
                // until its stopped manually from the command line
                clientSocket = serverSocket.accept();
                threads[numClient] = new ClientThread(clientSocket, targetWord,
                        currentWord, numGuesses, guessedChar);
                threads[numClient].start();
                numClient++;
                System.out.println("Online players: " + numClient);
                System.out.println("---------------------------------------------");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is for downloading a random word from the random word generator API
     */
    public void generateWord() {
        try {
            URL netURL = new URL("https://random-word-api.herokuapp.com//word?number=1");
            URLConnection conn = netURL.openConnection();
            conn.setDoOutput(false);
            conn.setDoInput(true);
            InputStream inStream = conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            String jsonData = buffer.toString();
            Pattern p = Pattern.compile("\"([^\"]*)\"");
            Matcher m = p.matcher(jsonData);
            while (m.find()) {
                targetWord = m.group(1);
            }
            currentWord = targetWord.replaceAll(".", "_");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main function to run the server
     * @param args
     */
    public static void main(String[] args) {
        Server server = new Server();
    }
}
