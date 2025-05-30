package AdventureModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class AdventureLoader {

    private AdventureGame game;
    private String adventureName;

    public AdventureLoader(AdventureGame game, String directoryName) {
        this.game = game;
        this.adventureName = directoryName;
    }
    public void loadGame() throws IOException {
        parseRooms();
        parseObjects();
        parseSynonyms();
        parseQuestions();
        this.game.setHelpText(parseOtherFile("help"));
    }

    private void parseRooms() throws IOException {

        int roomNumber;

        String roomFileName = this.adventureName + "/rooms.txt";
        BufferedReader buff = new BufferedReader(new FileReader(roomFileName));

        while (buff.ready()) {

            String currRoom = buff.readLine(); // first line is the number of a room

            roomNumber = Integer.parseInt(currRoom); //current room number

            // now need to get room name
            String roomName = buff.readLine();

            // now we need to get the description
            String roomDescription = "";
            String line = buff.readLine();
            while (!line.equals("-----")) {
                roomDescription += line + "\n";
                line = buff.readLine();
            }
            roomDescription += "\n";

            // now we make the room object
            Room room = new Room(roomName, roomNumber, roomDescription, adventureName);

            // now we make the motion table
            line = buff.readLine(); // reads the line after "-----"
            while (line != null && !line.equals("")) {
                String[] part = line.split(" \s+"); // have to use regex \\s+ as we don't know how many spaces are between the direction and the room number
                String direction = part[0];
                String dest = part[1];
                if (dest.contains("/")) {
                    String[] blockedPath = dest.split("/");
                    String dest_part = blockedPath[0];
                    String object = blockedPath[1];
                    Passage entry = new Passage(direction, dest_part, object);
                    room.getMotionTable().addDirection(entry);
                } else {
                    Passage entry = new Passage(direction, dest);
                    room.getMotionTable().addDirection(entry);
                }
                line = buff.readLine();
            }
            this.game.getRooms().put(room.getRoomNumber(), room);
        }

    }

    /**
     * User Story #1
     * Parses a list of questions from the game folder
     * @throws IOException
     */
    public void parseQuestions() throws IOException{
        String objectFileName = this.adventureName + "/questions.txt";
        BufferedReader buff = new BufferedReader(new FileReader(objectFileName));

        while (buff.ready()){
            String questionTopic = buff.readLine();
            Integer questionNumber = Integer.valueOf(buff.readLine());
            //String questionTopic = buff.readLine();
            String questionQuestion = buff.readLine();
            String hint = buff.readLine();
            String questionAnswer = buff.readLine();
            String seperator = buff.readLine();
            if (seperator != null && !seperator.isEmpty()){
                System.out.println("Formatting Error!");
            }
            String[] questionHint = hint.split(", ");

            Question question = new Question(questionNumber, questionTopic, questionQuestion, questionHint, questionAnswer);
            this.game.getQuestions().put(question.getQuestionNumber(), question);
            this.game.getRooms().get(questionNumber).setRoomQuestion(question);
        }

    }

    public void parseObjects() throws IOException {

        String objectFileName = this.adventureName + "/objects.txt";
        BufferedReader buff = new BufferedReader(new FileReader(objectFileName));

        while (buff.ready()) {
            String objectName = buff.readLine();
            String objectDescription = buff.readLine();
            String objectLocation = buff.readLine();
            String separator = buff.readLine();
            if (separator != null && !separator.isEmpty())
                System.out.println("Formatting Error!");
            int i = Integer.parseInt(objectLocation);
            Room location = this.game.getRooms().get(i);
            AdventureObject object = new AdventureObject(objectName, objectDescription, location);
            location.addGameObject(object);
        }

    }

    public void parseSynonyms() throws IOException {
        String synonymsFileName = this.adventureName + "/synonyms.txt";
        BufferedReader buff = new BufferedReader(new FileReader(synonymsFileName));
        String line = buff.readLine();
        while(line != null){
            String[] commandAndSynonym = line.split("=");
            String command1 = commandAndSynonym[0];
            String command2 = commandAndSynonym[1];
            this.game.getSynonyms().put(command1,command2);
            line = buff.readLine();
        }

    }

    public String parseOtherFile(String fileName) throws IOException {
        String text = "";
        fileName = this.adventureName + "/" + fileName + ".txt";
        BufferedReader buff = new BufferedReader(new FileReader(fileName));
        String line = buff.readLine();
        while (line != null) { // while not EOF
            text += line+"\n";
            line = buff.readLine();
        }
        return text;
    }

}
