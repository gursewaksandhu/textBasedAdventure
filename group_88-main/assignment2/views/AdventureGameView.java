package views;

import AdventureModel.AdventureGame;
import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.audio.JavaClipAudioPlayer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.layout.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.event.EventHandler;
import javafx.scene.AccessibleRole;

import java.io.File;

//TODO: Make TTS Method using FreeTTS library
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import javax.swing.*;

public class AdventureGameView {

    AdventureGame model; //model of the game
    Stage stage; //stage on which all is rendered
    Button saveButton, loadButton, helpButton; //buttons
    Boolean helpToggle = false; //is help on display?

    GridPane gridPane = new GridPane(); //to hold images and buttons
    Label roomDescLabel = new Label(); //to hold room description and/or instructions
    VBox objectsInRoom = new VBox(); //to hold room items
    VBox objectsInInventory = new VBox(); //to hold inventory items
    ImageView roomImageView; //to hold room image
    TextField inputTextField; //for user input
    Label questionDisplayLabel = new Label(); // to hold the current question for this room

    Thread speakingThread;

    private MediaPlayer mediaPlayer; //to play audio
    private boolean mediaPlaying; //to know if the audio is playing

    public AdventureGameView(AdventureGame model, Stage stage) {
        this.model = model;
        this.stage = stage;
        intiUI();
    }

    public void intiUI() {

        // setting up the stage
        this.stage.setTitle("allinso1's Adventure Game"); //customize your title

        //Inventory + Room items
        objectsInInventory.setSpacing(10);
        objectsInInventory.setAlignment(Pos.TOP_CENTER);
        objectsInRoom.setSpacing(10);
        objectsInRoom.setAlignment(Pos.TOP_CENTER);

        // GridPane, anyone?
        gridPane.setPadding(new Insets(20));
        gridPane.setBackground(new Background(new BackgroundFill(
                Color.valueOf("#000000"),
                new CornerRadii(0),
                new Insets(0)
        )));

        //Three columns, three rows for the GridPane
        ColumnConstraints column1 = new ColumnConstraints(150);
        ColumnConstraints column2 = new ColumnConstraints(650);
        ColumnConstraints column3 = new ColumnConstraints(150);
        column3.setHgrow( Priority.SOMETIMES ); //let some columns grow to take any extra space
        column1.setHgrow( Priority.SOMETIMES );

        // Row constraints
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints( 550 );
        RowConstraints row3 = new RowConstraints();
        row1.setVgrow( Priority.SOMETIMES );
        row3.setVgrow( Priority.SOMETIMES );

        gridPane.getColumnConstraints().addAll( column1 , column2 , column1 );
        gridPane.getRowConstraints().addAll( row1 , row2 , row1 );

        // Buttons
        saveButton = new Button("Save");
        saveButton.setId("Save");
        customizeButton(saveButton, 100, 50);
        makeButtonAccessible(saveButton, "Save Button", "This button saves the game.", "This button saves the game. Click it in order to save your current progress, so you can play more later.");
        addSaveEvent();

        loadButton = new Button("Load");
        loadButton.setId("Load");
        customizeButton(loadButton, 100, 50);
        makeButtonAccessible(loadButton, "Load Button", "This button loads a game from a file.", "This button loads the game from a file. Click it in order to load a game that you saved at a prior date.");
        addLoadEvent();

        helpButton = new Button("Instructions");
        helpButton.setId("Instructions");
        customizeButton(helpButton, 200, 50);
        makeButtonAccessible(helpButton, "Help Button", "This button gives game instructions.", "This button gives instructions on the game controls. Click it to learn how to play.");
        addInstructionEvent();

        HBox topButtons = new HBox();
        topButtons.getChildren().addAll(saveButton, helpButton, loadButton);
        topButtons.setSpacing(10);
        topButtons.setAlignment(Pos.CENTER);

        inputTextField = new TextField();
        inputTextField.setFont(new Font("Arial", 16));
        inputTextField.setFocusTraversable(true);

        inputTextField.setAccessibleRole(AccessibleRole.TEXT_AREA);
        inputTextField.setAccessibleRoleDescription("Text Entry Box");
        inputTextField.setAccessibleText("Enter commands in this box.");
        inputTextField.setAccessibleHelp("This is the area in which you can enter commands you would like to play.  Enter a command and hit return to continue.");
        addTextHandlingEvent(); //attach an event to this input field

        //labels for inventory and room items
        Label objLabel =  new Label("Objects in Room");
        objLabel.setAlignment(Pos.CENTER);
        objLabel.setStyle("-fx-text-fill: white;");
        objLabel.setFont(new Font("Arial", 16));

        Label invLabel =  new Label("Your Inventory");
        invLabel.setAlignment(Pos.CENTER);
        invLabel.setStyle("-fx-text-fill: white;");
        invLabel.setFont(new Font("Arial", 16));

        //add all the widgets to the GridPane
        gridPane.add( objLabel, 0, 0, 1, 1 );  // Add label
        gridPane.add( topButtons, 1, 0, 1, 1 );  // Add buttons
        gridPane.add( invLabel, 2, 0, 1, 1 );  // Add label

        Label commandLabel = new Label("What would you like to do?");
        commandLabel.setStyle("-fx-text-fill: white;");
        commandLabel.setFont(new Font("Arial", 16));

        updateScene("");
        updateItems();

        // adding the text area and submit button to a VBox
        VBox textEntry = new VBox();
        textEntry.setStyle("-fx-background-color: #000000;");
        textEntry.setPadding(new Insets(20, 20, 20, 20));
        textEntry.getChildren().addAll(commandLabel, inputTextField);
        textEntry.setSpacing(10);
        textEntry.setAlignment(Pos.CENTER);
        gridPane.add( textEntry, 0, 2, 3, 1 );

        // Render everything
        var scene = new Scene( gridPane ,  1000, 800);
        scene.setFill(Color.BLACK);
        this.stage.setScene(scene);
        this.stage.setResizable(false);
        this.stage.show();

    }

    public static void makeButtonAccessible(Button inputButton, String name, String shortString, String longString) {
        inputButton.setAccessibleRole(AccessibleRole.BUTTON);
        inputButton.setAccessibleRoleDescription(name);
        inputButton.setAccessibleText(shortString);
        inputButton.setAccessibleHelp(longString);
    }

    private void customizeButton(Button inputButton, int w, int h) {
        inputButton.setPrefSize(w, h);
        inputButton.setFont(new Font("Arial", 16));
        inputButton.setStyle("-fx-background-color: #17871b; -fx-text-fill: white;");
        inputButton.setFocusTraversable(true);
    }

    private void addTextHandlingEvent() {
        inputTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    String text = inputTextField.getText();
                    submitEvent(text);
                    inputTextField.clear();
                } else if (keyEvent.getCode() == KeyCode.TAB) {
                    objectsInRoom.requestFocus(); //first in scene graph
                }
            }
        });
    }


    private void submitEvent(String text) {

        text = text.strip(); //get rid of white space
        //stopArticulation(); //if speaking, stop
        stopReading();

        //TODO: Checks if Question has been answered. If not checks if given response is a viable answer.

        if (text.equalsIgnoreCase("LOOK") || text.equalsIgnoreCase("L")) {
            String roomDesc = this.model.getPlayer().getCurrentRoom().getRoomDescription();
            String objectString = this.model.getPlayer().getCurrentRoom().getObjectString();
            if (!objectString.isEmpty()) roomDescLabel.setText(roomDesc + "\n\nObjects in this room:\n" + objectString);
            //articulateRoomDescription(); //all we want, if we are looking, is to repeat description.
            textReader(this.model.player.getCurrentRoom().getRoomDescription());
            return;
        } else if (text.equalsIgnoreCase("HELP") || text.equalsIgnoreCase("H")) {
            showInstructions();
            return;
        } else if (text.equalsIgnoreCase("COMMANDS") || text.equalsIgnoreCase("C")) {
            showCommands();
            textReader(roomDescLabel.getText());
            return;
        }

        //try to move!
        String output = this.model.interpretAction(text); //process the command!

        if (output == null || (!output.equals("GAME OVER") && !output.equals("FORCED") && !output.equals("HELP"))) {
            updateScene(output);
            updateItems();
        } else if (output.equals("GAME OVER")) {
            updateScene("");
            updateItems();
            PauseTransition pause = new PauseTransition(Duration.seconds(10));
            pause.setOnFinished(event -> {
                Platform.exit();
            });
            pause.play();
        } else if (output.equals("FORCED")) {

            updateScene(null);
            inputTextField.setEditable(false);
            inputTextField.clear();

            double time = 6.0;

            if (this.model.getPlayer().getCurrentRoom().getRoomNumber() == 24) time = 14.0;
            if (this.model.getPlayer().getCurrentRoom().getRoomNumber() == 28) time = 14.0;

            PauseTransition pause = new PauseTransition(Duration.seconds(time));
            pause.setOnFinished(event -> {
                //stopArticulation();
                stopReading();
                updateScene(null);
                updateItems();
                inputTextField.setEditable(true);
                inputTextField.clear();
                submitEvent("FORCED");
            });
            pause.play();
        }
    }

    private void showCommands() {
        String commands = this.model.getPlayer().getCurrentRoom().getCommands();
        String formatted = "You can move in these directions:\n\n";
        formatted += commands;
        roomDescLabel.setText(formatted);
        roomDescLabel.setAlignment(Pos.CENTER);
    }

    public void updateScene(String textToDisplay) {
        getRoomImage(); //get the image of the current room
        formatText(textToDisplay); //format the text to display
        roomDescLabel.setPrefWidth(550);
        roomDescLabel.setPrefHeight(500);
        roomDescLabel.setTextOverrun(OverrunStyle.CLIP);
        roomDescLabel.setWrapText(true);
        questionDisplayLabel.setText("Question: " + this.model.player.getCurrentRoom().getRoomQuestion().getQuestionQuestion());
        questionDisplayLabel.setStyle("-fx-text-fill: white;");
        questionDisplayLabel.setFont(new Font("Arial", 16));
        questionDisplayLabel.setAlignment(Pos.CENTER);
        questionDisplayLabel.setPrefWidth(550);
        questionDisplayLabel.setPrefHeight(500);
        questionDisplayLabel.setTextOverrun(OverrunStyle.CLIP);
        questionDisplayLabel.setWrapText(true);

        HBox labelsPane = new HBox(roomDescLabel, questionDisplayLabel);
        labelsPane.setAlignment(Pos.TOP_CENTER);
        labelsPane.setStyle("-fx-background-color: #000000;");

        VBox roomPane = new VBox(roomImageView,labelsPane);
        roomPane.setPadding(new Insets(10));
        roomPane.setAlignment(Pos.TOP_CENTER);
        roomPane.setStyle("-fx-background-color: #000000;");

        gridPane.add(roomPane, 1, 1);
        stage.sizeToScene();

        //finally, articulate the description
        //if (textToDisplay == null || textToDisplay.isBlank()) articulateRoomDescription();

        if (textToDisplay == null || textToDisplay.isBlank())
            textReader(this.model.player.getCurrentRoom().getRoomDescription() + "\n" + "Here is your question: " + this.model.player.getCurrentRoom().getRoomQuestion().questionQuestion);
    }

    public void textReader(String text){
        // NEED TO CREATE THREAD IN ORDER to have voice play at same time as UI, otherwise the voice occurs first and then the UI appears
        SwingWorker<Void, Void> TTS = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                System.setProperty("freetts.voices",
                        "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
                Voice voice = VoiceManager.getInstance().getVoice("kevin16");
                try{
                    voice.allocate();
                    speakingThread = Thread.currentThread();
                    voice.speak(text);
                }
                finally {
                    voice.deallocate();
                    speakingThread = null;
                }

                return null;
            }
        };
        TTS.execute();
    }

    public void stopReading(){
        if (speakingThread != null)
            speakingThread.interrupt();
    }

    private void formatText(String textToDisplay) {
        if (textToDisplay == null || textToDisplay.isBlank()) {
            String roomDesc = this.model.getPlayer().getCurrentRoom().getRoomDescription() + "\n";
            String objectString = this.model.getPlayer().getCurrentRoom().getObjectString();
            if (!objectString.isEmpty()) roomDescLabel.setText(roomDesc + "\n\nObjects in this room:\n" + objectString);
            else roomDescLabel.setText(roomDesc);
            //if (!objectString.isEmpty()) roomDescLabel = new Label(roomDesc + "\n\nObjects in this room:\n" + objectString);
            //else roomDescLabel = new Label(roomDesc);
        } else roomDescLabel.setText(textToDisplay);
            //roomDescLabel = new Label(textToDisplay);

        roomDescLabel.setStyle("-fx-text-fill: white;");
        roomDescLabel.setFont(new Font("Arial", 16));
        roomDescLabel.setAlignment(Pos.CENTER);

    }

    private void getRoomImage() {

        int roomNumber = this.model.getPlayer().getCurrentRoom().getRoomNumber();
        String roomImage = this.model.getDirectoryName() + "/room-images/" + roomNumber + ".png";

        Image roomImageFile = new Image(roomImage);
        roomImageView = new ImageView(roomImageFile);
        roomImageView.setPreserveRatio(true);
        roomImageView.setFitWidth(400);
        roomImageView.setFitHeight(400);

        //set accessible text
        roomImageView.setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        roomImageView.setAccessibleText(this.model.getPlayer().getCurrentRoom().getRoomDescription());
        roomImageView.setFocusTraversable(true);

    }

    public void updateItems() {

        //next, render the objects in the room
        int numberOfObjects = this.model.getPlayer().getCurrentRoom().getObjectsInRoom().size();
        objectsInRoom.getChildren().clear();

        for(int i = 0; i < numberOfObjects; i++){
            String objectName = this.model.getPlayer().getCurrentRoom().getObjectsInRoom().get(i);
            String objectImage = this.model.getDirectoryName() + "/objectImages/" + objectName + ".jpg";
            Image objectImageFile = new Image(objectImage);
            ImageView objectImageView = new ImageView(objectImageFile);
            objectImageView.setFitWidth(100);
            objectImageView.setFitHeight(60);
            Button objectButton = new Button();
            objectButton.setPrefWidth(100);
            objectButton.setPrefHeight(60);

            objectButton.setId(objectName);
            objectButton.setGraphic(objectImageView);

            //set accessible details
            objectButton.setAccessibleRole(AccessibleRole.BUTTON);
            objectButton.setAccessibleRoleDescription(objectName);
            objectButton.setAccessibleText("This object is in the room.");
            objectButton.setAccessibleHelp("This object is in the room: " + objectName + ". Click it in order to place it in your inventory.");
            objectButton.setFocusTraversable(true);

            objectButton.setOnAction(e -> {
                this.model.getPlayer().takeObject(objectName);
                objectsInRoom.getChildren().remove(objectButton);
                updateItems();
            });

            objectButton.setContentDisplay(ContentDisplay.TOP);
            objectButton.setText(objectName);
            objectButton.setStyle("-fx-text-fill: black; -fx-font-size: 16px;");

            objectsInRoom.getChildren().add(objectButton);
        }


        //inventory
        int numObjects = this.model.getPlayer().getInventory().size();
        objectsInInventory.getChildren().clear();

        String objectImage;
        Image objectImageFile;
        ImageView objectImageView;
        for(int i = 0; i < numObjects; i++){

            String objectName = this.model.getPlayer().getInventory().get(i);
            objectImage = this.model.getDirectoryName() + "/objectImages/" + objectName + ".jpg";

            objectImageFile = new Image(objectImage);
            objectImageView = new ImageView(objectImageFile);
            objectImageView.setFitWidth(100);
            objectImageView.setFitHeight(60);

            Button objectButton = new Button();
            objectButton.setId(objectName);
            objectButton.setGraphic(objectImageView);
            objectButton.setAccessibleRole(AccessibleRole.BUTTON);
            objectButton.setAccessibleRoleDescription(objectName);
            objectButton.setAccessibleText("This object is in your inventory");
            objectButton.setAccessibleHelp("This object is in your inventory: " + objectName + ". Click it in order to leave it in the room.");
            objectButton.setFocusTraversable(true);

            objectButton.setOnAction(e -> {
                this.model.getPlayer().dropObject(objectName);
                objectsInInventory.getChildren().remove(objectButton);
                updateItems();
            });

            objectButton.setContentDisplay(ContentDisplay.TOP);
            objectButton.setText(objectName);
            objectButton.setStyle("-fx-text-fill: black; -fx-font-size: 16px;");

            objectsInInventory.getChildren().add(objectButton);
        }

        ScrollPane scO = new ScrollPane(objectsInRoom);
        scO.setPadding(new Insets(10));
        scO.setStyle("-fx-background: #000000; -fx-background-color:transparent;");
        scO.setFitToWidth(true);
        gridPane.add(scO,0,1);

        ScrollPane scI = new ScrollPane(objectsInInventory);
        scI.setFitToWidth(true);
        scI.setStyle("-fx-background: #000000; -fx-background-color:transparent;");
        gridPane.add(scI,2,1);

    }

    public void showInstructions() {
        if (!helpToggle) {
            gridPane.requestFocus();

            Button closeButton = new Button("Hide Instructions");
            closeButton.setId("Close");
            closeButton.setPrefSize(400, 50);
            closeButton.setFont(new Font("Arial", 16));
            closeButton.setStyle("-fx-background-color: #17871b; -fx-text-fill: white;");
            closeButton.setFocusTraversable(true);

            closeButton.setAccessibleRole(AccessibleRole.BUTTON);
            closeButton.setAccessibleRoleDescription("Close");
            closeButton.setAccessibleText("This button will exit the instructions");
            closeButton.setAccessibleHelp("This button will exit the instructions. Click it in order to continue the game.");

            closeButton.setOnAction(e -> {
                showInstructions();
            });

            Label instructions = new Label(this.model.getInstructions());
            instructions.setStyle("-fx-text-fill: #ffffff;");
            instructions.setFont(new Font("Arial", 16));
            instructions.setWrapText(true);
            instructions.setAlignment(Pos.CENTER);

            VBox instructionBox = new VBox(closeButton, instructions);
            instructionBox.setAlignment(Pos.CENTER);
            instructions.setPadding(new Insets(20));
            ScrollPane roomPane = new ScrollPane(instructionBox);
            roomPane.setPadding(new Insets(10));
            roomPane.setStyle("-fx-background: #000000;");
            roomPane.setFitToWidth(true);
            roomPane.setFitToHeight(false);
            roomPane.setFocusTraversable(true);

            gridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) == 1 && GridPane.getColumnIndex(node) == 1);
            gridPane.add(roomPane, 1, 1);
            helpToggle = true;
        } else {
            gridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) == 1 && GridPane.getColumnIndex(node) == 1);
            updateScene(null);
            helpToggle = false;
        }
    }

    /**
     * This method handles the event handling code for the
     * help button.
     */
    public void addInstructionEvent() {
        helpButton.setOnAction(e -> {
            //stopArticulation(); //if speaking, stop
            stopReading();
            showInstructions();
        });
    }

    /**
     * This method handles the event handling code for the
     * save button.
     */
    public void addSaveEvent() {
        saveButton.setOnAction(e -> {
            gridPane.requestFocus();
            SaveView saveView = new SaveView(this);
        });
    }

    /**
     * This method handles the event handling code for the
     * load button.
     */
    public void addLoadEvent() {
        loadButton.setOnAction(e -> {
            gridPane.requestFocus();
            LoadView loadView = new LoadView(this);
        });
    }


    /**
     * This method articulates Room Descriptions
     */
    public void articulateRoomDescription() {
        String musicFile;
        String adventureName = this.model.getDirectoryName();
        String roomName = this.model.getPlayer().getCurrentRoom().getRoomName();

        if (!this.model.getPlayer().getCurrentRoom().getVisited()) musicFile = "./" + adventureName + "/sounds/" + roomName.toLowerCase() + "-long.mp3" ;
        else musicFile = "./" + adventureName + "/sounds/" + roomName.toLowerCase() + "-short.mp3" ;
        musicFile = musicFile.replace(" ","-");

        Media sound = new Media(new File(musicFile).toURI().toString());

        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
        mediaPlaying = true;

    }

    /**
     * This method stops articulations
     */
    public void stopArticulation() {
        if (mediaPlaying) {
            mediaPlayer.stop();
            mediaPlaying = false;
        }
    }
}
