package org.thinh;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLOutput;
import java.util.ArrayList;

/**
 * @author Thinh Le
 * <h1>ClientGUI</h1>
 * <p>This class implements the client GUI of a multiplayer hangman game application</p>
 */

public class ClientGUI extends Application {
    // Initialize variables
    private double W_HEIGHT = 375, W_WIDTH = 400;
    private Scene introScene, menuScene, gameScene;
    private Image logo = new Image("/images/logo.png");

    /**
     * Initialize some JavaFX elements here
     * so they can be used inside other threads and helper methods
     */
    private Label wordLb;
    private TextArea usedLettersTa;
    private Canvas hangmanCanvas;
    private Label statusLb;

    /**
     * Client object, number of guess, a array to store the guessed letters, and an alert popup
     * to display game result.
     */
    private Client client;
    private int numGuesses = 0;
    private ArrayList<String> guessedChar;
    private Alert alert;

    private static String SERVER_ADDRESS = null;
    private static int SERVER_PORT;

    /**
     * This is the start function to implement the application
     * @param primaryStage The main stage/window of the application
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Intro scene - Take Server's IP Address and Server's Port
        Label ipAddressLb = new Label("Server's IP Address:");
        Label portLb = new Label("Server's Port:");
        Label connectStatusLb = new Label();

        TextField ipAddressTf = new TextField();
        TextField portTf = new TextField();

        Button connectBtn = new Button("Connect");

        // These HBoxes and VBoxes are for align the elements in GridPane
        HBox ipAddressHBox = new HBox(ipAddressLb);
        ipAddressHBox.setAlignment(Pos.CENTER);
        HBox portHBox = new HBox(portLb);
        portHBox.setAlignment(Pos.CENTER);
        VBox ipAddressVBox = new VBox(ipAddressHBox, ipAddressTf);
        ipAddressVBox.setSpacing(5);
        VBox portVBox = new VBox(portHBox, portTf);
        portVBox.setSpacing(5);
        HBox connectBtnHBox = new HBox(connectBtn);
        connectBtnHBox.setAlignment(Pos.CENTER);
        HBox connectSttLbHBox = new HBox(connectStatusLb);
        connectSttLbHBox.setAlignment(Pos.CENTER);

        GridPane introGrid = new GridPane();
        introGrid.setAlignment(Pos.CENTER);
        introGrid.setVgap(10);
        introGrid.add(ipAddressVBox,0,0);
        introGrid.add(portVBox,0,1);
        introGrid.add(connectBtnHBox,0,3);
        introGrid.add(connectSttLbHBox,0,2);

        introScene = new Scene(introGrid, W_WIDTH, W_HEIGHT);
        primaryStage.setScene(introScene);

        connectBtn.setOnAction(event -> { primaryStage.setScene(menuScene); });


        // Menu scene
        ImageView logoImgV = new ImageView(logo);
        HBox logoHBox = new HBox(logoImgV);
        logoHBox.setAlignment(Pos.CENTER);

        Button playBtn = new Button("Play");
        playBtn.setPrefWidth(100);

        Button exitBtn = new Button("Exit");
        exitBtn.setPrefWidth(100);
        exitBtn.setOnAction(e -> Platform.exit());

        // These HBoxes and VBoxes are for align the elements in GridPane
        VBox menuBtnVBox = new VBox();
        menuBtnVBox.getChildren().addAll(playBtn, exitBtn);
        menuBtnVBox.setAlignment(Pos.CENTER);
        menuBtnVBox.setSpacing(10);

        GridPane menuGrid = new GridPane();
        menuGrid.setAlignment(Pos.CENTER);
        menuGrid.setVgap(10);
        menuGrid.add(logoHBox,0,0);
        menuGrid.add(menuBtnVBox,0,1);

        menuScene = new Scene(menuGrid, W_WIDTH, W_HEIGHT);

        // 'playBtn' open gameScene, which will handle all of the features during the game
        playBtn.setOnAction(event -> {
            // Can change this to "localhost" and 6868 for easy testing
            SERVER_ADDRESS = ipAddressTf.getText();
            SERVER_PORT = Integer.parseInt(portTf.getText());
            client = new Client(SERVER_ADDRESS, SERVER_PORT);


            // Game scene
            wordLb = new Label();
            wordLb.setFont(Font.font("Arial", FontWeight.BOLD,15));
            Label usedLettersLb = new Label("Guessed Letters:");
            statusLb = new Label();

            TextField guessTf = new TextField();

            usedLettersTa = new TextArea();

            Button guessBtn = new Button("Guess");
            guessBtn.prefWidth(100);

            hangmanCanvas = new Canvas(200,200);

            // These HBoxes and VBoxes are for align the elements in GridPane
            HBox usedLettersHBox = new HBox(usedLettersLb);
            VBox usedLettersVBox = new VBox(usedLettersHBox, usedLettersTa);
            HBox hangmanHBox = new HBox(hangmanCanvas, usedLettersVBox);
            HBox guessesHBox = new HBox(guessTf, guessBtn);
            HBox wordHBox = new HBox(wordLb);
            HBox statusHBox = new HBox(statusLb);

            usedLettersTa.setEditable(false);
            usedLettersTa.setPrefWidth(175);

            usedLettersHBox.setAlignment(Pos.CENTER);
            usedLettersVBox.setSpacing(10);
            hangmanHBox.setAlignment(Pos.CENTER);
            hangmanHBox.setSpacing(10);
            guessesHBox.setAlignment(Pos.CENTER);
            guessesHBox.setSpacing(10);
            wordHBox.setAlignment(Pos.CENTER);
            statusHBox.setAlignment(Pos.CENTER);

            GridPane gameGrid = new GridPane();
            gameGrid.setAlignment(Pos.CENTER);
            gameGrid.setVgap(10);
            gameGrid.add(wordHBox,0,0);
            gameGrid.add(hangmanHBox, 0, 2);
            gameGrid.add(guessesHBox,0,4);
            gameGrid.add(statusHBox,0,5);

            gameScene = new Scene(gameGrid, W_WIDTH, W_HEIGHT);

            /**
             * This thread runs in the background to auto-update game UI
             * so player don't need to do any action to update game UI
             */
            Thread update = new Thread(new Runnable() {
                @Override
                public void run() {
                    /**
                     * Helper object to auto-update game UI inside Thread
                     */
                    Runnable updater = new Runnable() {
                        @Override
                        public void run() {
                            synchronized (this) {
                                update();
                            }
                        }
                    };
                    /**
                     * Helper object to auto-check winning condition of the game inside Thread
                     */
                    Runnable endGame = new Runnable() {
                        @Override
                        public void run() {
                            synchronized (this) {
                                end();
                                primaryStage.setScene(menuScene);
                            }
                        }
                    };
                    // This while loop will keep the thread running until winning condition occur
                    while (client.isWin().equalsIgnoreCase("CONTINUE")) {
                        Platform.runLater(updater);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {}
                    }
                    Platform.runLater(endGame);
                }
            });
            update.start();

            guessBtn.setOnAction(e -> {
                // Pause the background thread to prevent JavaFX elements being changed simultaneously
                try {
                    update.sleep(500);
                } catch (InterruptedException ex) {}
                String message = "";
                String guessWord = guessTf.getText();
                guessTf.clear();
                // Check if input is alphabetical letters, and whether if its been guessed before
                if (!isStringOnlyAlphabet(guessWord)) {
                    statusLb.setText("Invalid input! Try again");
                    statusLb.setTextFill(Color.RED);
                } else if (guessedChar.contains(guessWord)) {
                    statusLb.setText("Letter is already guessed! Try again");
                    statusLb.setTextFill(Color.RED);
                } else {
                    message = client.sendGuess(guessWord);
                    // Winning condition occur, display result popup and back to menuScene
                    if (message.equalsIgnoreCase("DONE")) {
                        update.stop();
                        end();
                        primaryStage.setScene(menuScene);
                    } else {
                        // Continue the game
                        update();
                        if (message.equalsIgnoreCase("CORRECT!")) {
                            statusLb.setTextFill(Color.GREEN);
                        } else {
                            statusLb.setTextFill(Color.RED);
                        }
                        statusLb.setText(message);
                    }
                }
                update.interrupt();
            });


            primaryStage.setScene(gameScene);
        });


        primaryStage.setTitle("Hangman");
        primaryStage.getIcons().add(logo);
        primaryStage.show();
    }


    /**
     * This method is for checking if the input is alphabetical
     * @param str The string which going to be checked
     * @return boolean If the string contains only alphabetical letters
     */
    public static boolean isStringOnlyAlphabet(String str) {
        return ((str != null)
                && (!str.equals(""))
                && (str.matches("^[a-zA-Z]*$")));
    }


    /**
     * This method is for updating the game UI
     */
    private void update() {
        wordLb.setText(client.getCurrentWord());
        guessedChar = new ArrayList<>(client.getGuessedChar());
        String usedLetters = "";
        for (String s : guessedChar) {
            usedLetters += s + "\n";
        }
        String currentUsedLetters = usedLettersTa.getText();
        if (!currentUsedLetters.equals(usedLetters)) {
            usedLettersTa.setText(usedLetters);
            statusLb.setText("");
        }
        numGuesses = client.getNumGuesses();
        drawHangman();
    }


    /**
     * This method is for display the result popup when the game ends
     */
    private void end() {
        update();
        wordLb.setText(client.getTargetWord());
        // 'alert' is a window popup
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Result");
        alert.setHeaderText(null);
        alert.setContentText(client.isWin() + "\n" + "The word is " + client.getTargetWord());
        alert.showAndWait();
        // Disconnect with the server
        client.closeClient();
    }


    /**
     * This method is to draw the hangman
     */
    private void drawHangman() {
        GraphicsContext gc = hangmanCanvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.clearRect(0, 0, 200, 200);

        // Hangman Stand
        gc.setFill(Color.BLACK);
        gc.strokeLine(0,200,100,200);
        gc.strokeLine(50,0,50,200);
        gc.strokeLine(50,0,150,0);
        gc.strokeLine(150,0,150,20);

        // Hangman figure
        int hangmanCount = 0;
        while (hangmanCount < numGuesses) {
            if (hangmanCount == 0) {
                gc.strokeOval(135,20,30,30); //head
            }
            else if (hangmanCount == 1) {
                gc.strokeLine(150,50,150,120); //body
            }
            else if (hangmanCount == 2) {
                gc.strokeLine(150,80,100,30); // left arm
            }
            else if (hangmanCount == 3) {
                gc.strokeLine(150, 80, 200, 30); // right arm
            }
            else if (hangmanCount == 4) {
                gc.strokeLine(150,120,100,175); //left leg
            }
            else {
                gc.strokeLine(150,120,200,175); //right leg
            }
            hangmanCount++;
        }
    }


    public static void main(String[] args) { launch(args); }
}
