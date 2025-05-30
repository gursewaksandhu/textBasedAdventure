package AdventureModel;

import java.io.*;
import java.util.*;

/**
 * Class AdventureGame.  Handles all the necessary tasks to run the Adventure game.
 */
public class AdventureGame implements Serializable {
    private final String directoryName; //An attribute to store the Introductory text of the game.
    private String helpText; //A variable to store the Help text of the game. This text is displayed when the user types "HELP" command.
    private final HashMap<Integer, Room> rooms; //A list of all the rooms in the game.
    private HashMap<String,String> synonyms = new HashMap<>(); //A HashMap to store synonyms of commands.
    private final String[] actionVerbs = {"QUIT","INVENTORY","TAKE","DROP", "ANSWER", "HINT", "STATS", "SKIP"}; //List of action verbs (other than motions) that exist in all games. Motion vary depending on the room and game.
    public Player player; //The Player of the game.

    private final HashMap<Integer, Question> questions; // A list of all the questions and their room

    private QuestionTracker tracker;

    /**
     * Adventure Game Constructor
     * __________________________
     * Initializes attributes
     */
    public AdventureGame(String name){
        this.synonyms = new HashMap<>();
        this.rooms = new HashMap<>();
        this.questions = new HashMap<>();
        this.directoryName = "Games/" + name;
        this.tracker = new QuestionTracker();
        try {
            setUpGame();
        } catch (IOException e) {
            throw new RuntimeException("An Error Occurred: " + e.getMessage());
        }
    }

    /**
     * setUpGame
     * __________________________
     *
     * @throws IOException in the case of a file I/O error
     */
    public void setUpGame() throws IOException {

        String directoryName = this.directoryName;
        AdventureLoader loader = new AdventureLoader(this, directoryName);
        loader.loadGame();

        // set up the player's current location
        this.player = new Player(this.rooms.get(1));
    }

    /**
     * tokenize
     * __________________________
     *
     * @param input string from the command line
     * @return a string array of tokens that represents the command.
     */
    public String[] tokenize(String input){

        input = input.toUpperCase();
        String[] commandArray = input.split(" ");

        int i = 0;
        while (i < commandArray.length) {
            if(this.synonyms.containsKey(commandArray[i])){
                commandArray[i] = this.synonyms.get(commandArray[i]);
            }
            i++;
        }
        return commandArray;

    }

    /**
     * movePlayer
     * __________________________
     * Moves the player in the given direction, if possible.
     * Return false if the player wins or dies as a result of the move.
     *
     * @param direction the move command
     * @return false, if move results in death or a win (and game is over).  Else, true.
     */
    public boolean movePlayer(String direction) {

        direction = direction.toUpperCase();
        PassageTable motionTable = this.player.getCurrentRoom().getMotionTable(); //where can we move?
        if (!motionTable.optionExists(direction)) return true; //no move

        ArrayList<Passage> possibilities = new ArrayList<>();
        for (Passage entry : motionTable.getDirection()) {
            if (entry.getDirection().equals(direction)) { //this is the right direction
                possibilities.add(entry); // are there possibilities?
            }
        }

        //try the blocked passages first
        Passage chosen = null;
        for (Passage entry : possibilities) {
            System.out.println(entry.getIsBlocked());
            System.out.println(entry.getKeyName());

            if (chosen == null && entry.getIsBlocked()) {
                if (this.player.getInventory().contains(entry.getKeyName())) {
                    chosen = entry; //we can make it through, given our stuff
                    break;
                }
            } else { chosen = entry; } //the passage is unlocked
        }

        if (chosen == null) return true; //doh, we just can't move.

        int roomNumber = chosen.getDestinationRoom();
        Room room = this.rooms.get(roomNumber);
        this.player.setCurrentRoom(room);

        return !this.player.getCurrentRoom().getMotionTable().getDirection().get(0).getDirection().equals("FORCED");
    }

    /**
     * interpretAction
     * interpret the user's action.
     *
     * @param command: String representation of the command.
     */
    public String interpretAction(String command){
        String[] inputArray = tokenize(command); //look up synonyms

        PassageTable motionTable = this.player.getCurrentRoom().getMotionTable(); //where can we move?

        // Player may only move if the question has been answered
        if (motionTable.optionExists(inputArray[0])) {
            if (this.player.getCurrentRoom().getRoomQuestion().questionAnswered) {
                if (!movePlayer(inputArray[0])) {
                    if (this.player.getCurrentRoom().getMotionTable().getDirection().get(0).getDestinationRoom() == 0)
                        return "GAME OVER";
                    else return "FORCED";
                } //something is up!
                return null;
            } else {
                return "You must answer or skip the question to continue.";
            }
        }

        if(Arrays.asList(this.actionVerbs).contains(inputArray[0])) {
            if(inputArray[0].equals("QUIT")) { return "GAME OVER"; } //time to stop!
            else if(inputArray[0].equals("INVENTORY") && this.player.getInventory().size() == 0) return "INVENTORY IS EMPTY";
            else if(inputArray[0].equals("INVENTORY") && this.player.getInventory().size() > 0) return "THESE OBJECTS ARE IN YOUR INVENTORY:\n" + this.player.getInventory().toString();
            else if(inputArray[0].equals("ANSWER")){ // If the question has not been answered
                if (!this.player.getCurrentRoom().getRoomQuestion().questionAnswered){
                    if (this.player.getCurrentRoom().getRoomQuestion().checkAnswer(inputArray[1])){
                        //What to do if the question has the correct Response
                        // increase correct count, increase attempts, change question to answered
                        this.tracker.addAttempt(this.player.getCurrentRoom().getRoomQuestion(), true);
                        this.player.getCurrentRoom().getRoomQuestion().questionAnswered = true;
                        return "Correct, the Answer was: "  + inputArray[1];


                    } else {
                        this.tracker.addAttempt(this.player.getCurrentRoom().getRoomQuestion(), false);
                        return "Incorrect Answer, Try again!";
                    }
                } else {
                    return "You have already skipped or answered this question";
                }
            }
            else if (inputArray[0].equals("STATS")) {
                return this.tracker.createReport();
            }
            else if (inputArray[0].equals("SKIP")){
                if (this.player.getCurrentRoom().getRoomQuestion().questionAnswered){
                    return "You've already completed this question. Use 'commands' to see available directions or use 'help'";
                } else {
                    this.tracker.skipsUsed();
                    this.player.getCurrentRoom().getRoomQuestion().questionAnswered = true;
                    return "Question has been skipped. You may now proceed to the next room.";
                }
            }
            else if (inputArray[0].equals("HINT")){
                if (this.player.getCurrentRoom().getRoomQuestion().questionAnswered) {
                    return "You've already completed this question. Use 'commands' to see available directions or use 'help'";
                }
                else{
                    this.tracker.hintUsed();
                    return this.player.getCurrentRoom().getRoomQuestion().getQuestionHint();
                }
            }
            else if(inputArray[0].equals("TAKE") && inputArray.length < 2) return "THE TAKE COMMAND REQUIRES AN OBJECT";
            else if(inputArray[0].equals("DROP") && inputArray.length < 2) return "THE DROP COMMAND REQUIRES AN OBJECT";
            else if(inputArray[0].equals("TAKE") && inputArray.length >= 2) {
                if(this.player.getCurrentRoom().checkIfObjectInRoom(inputArray[1])) {
                    this.player.takeObject(inputArray[1]);
                    return "YOU HAVE TAKEN:\n " + inputArray[1].toUpperCase();
                } else {
                    return "THIS OBJECT IS NOT HERE:\n " + inputArray[1].toUpperCase();
                }
            }
            else if(inputArray[0].equals("DROP") && inputArray.length >= 2) {
                if(this.player.checkIfObjectInInventory(inputArray[1])) {
                    this.player.dropObject(inputArray[1]);
                    return "YOU HAVE DROPPED:\n " + inputArray[1].toUpperCase();
                } else {
                    return "THIS OBJECT IS NOT IN YOUR INVENTORY:\n " + inputArray[1].toUpperCase();
                }
            }

        }
        return "INVALID COMMAND FOR THIS ROOM";
    }

    public String getDirectoryName() {
        return this.directoryName;
    }

    public String getInstructions() {
        return helpText;
    }

    public Player getPlayer() {
        return this.player;
    }


    public void saveModel(File file) {
        try {
            FileOutputStream outfile = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(outfile);
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, Room> getRooms() {
        return this.rooms;
    }

    public HashMap<String, String> getSynonyms() {
        return this.synonyms;
    }

    public void setHelpText(String help) {
        this.helpText = help;
    }

public HashMap<Integer, Question> getQuestions(){ return this.questions; }

    public String getCommands() {
        Room currentRoom = this.player.getCurrentRoom();
        PassageTable p = currentRoom.getMotionTable();
        Set<String> retSet = new HashSet<>();
        for (Passage q: p.passageTable) {
            retSet.add(q.getDirection());
        }
        StringBuilder retString = new StringBuilder();
        int i = 0;
        for (String s: retSet) {
            if (i == 0) retString.append(s);
            else retString.append(",").append(s);
            i++;
        }
        return retString.toString();
    }
}
