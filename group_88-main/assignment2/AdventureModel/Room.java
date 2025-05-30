package AdventureModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * This class contains the information about a room in the Adventure Game.
 */
public class Room implements Serializable {

    private final String adventureName;
    /**
     * The number of the room.
     */
    private int roomNumber;

    /**
     * The name of the room.
     */
    private String roomName;

    /**
     * The description of the room.
     */
    private String roomDescription;

    /**
     * The passage table for the room.
     */
    private PassageTable motionTable = new PassageTable();

    /**
     * The list of objects in the room.
     */
    public ArrayList<AdventureObject> objectsInRoom = new ArrayList<AdventureObject>();

    /**
     * A boolean to store if the room has been visited or not
     */
    private boolean isVisited;

    /**
     * The question associated with this room
     */
    private Question roomQuestion;


    /**
     * AdvGameRoom constructor.
     *
     * @param roomName: The name of the room.
     * @param roomNumber: The number of the room.
     * @param roomDescription: The description of the room.
     */
    public Room(String roomName, int roomNumber, String roomDescription, String adventureName){
        this.roomName = roomName;
        this.roomNumber = roomNumber;
        this.roomDescription = roomDescription;
        this.adventureName = adventureName;
        this.isVisited = false;
    }

    /**
     * This method adds a game object to the room.
     *
     * @param: object to be added to the room.
     */
    public void addGameObject(AdventureObject object){
        this.objectsInRoom.add(object);
    }

    /**
     * This method removes a game object from the room.
     *
     * @param: object to be removed from the room.
     */
    public void removeGameObject(AdventureObject object){
        this.objectsInRoom.remove(object);
    }

    /**
     * This method checks if an object is in the room.
     *
     * @param objectName: Name of the object to be checked.
     * @return: true if the object is present in the room, false otherwise.
     */
    public boolean checkIfObjectInRoom(String objectName){
        for(int i = 0; i<objectsInRoom.size();i++){
            if(this.objectsInRoom.get(i).getName().equals(objectName)) return true;
        }
        return false;
    }

    /**
     * Return a string listing objects in the room.
     */
    public String getObjectString() {
        StringBuilder s = new StringBuilder();

        if (this.objectsInRoom.isEmpty()) return "";

        int i = 0;
        for (AdventureObject o: this.objectsInRoom) {
            if (i == 0 ) s.append(o.getDescription());
            else s.append(",").append(o.getDescription());
            i++;
        }

        return s.toString();
    }


    public String getCommands() {
        PassageTable p = this.getMotionTable();
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

    /**
     * Sets the visit status of the room to true.
     */
    public void visit(){
        isVisited = true;
    }

    /**
     * Getter for returning an AdventureObject with a given name
     *
     * @param objectName: Object name to find in the room
     * @return: AdventureObject
     */
    public AdventureObject getObject(String objectName){
        for(int i = 0; i<objectsInRoom.size();i++){
            if(this.objectsInRoom.get(i).getName().equals(objectName)) return this.objectsInRoom.get(i);
        }
        return null;
    }

    /**
     * Getter method for the number attribute.
     *
     * @return: number of the room
     */
    public int getRoomNumber(){
        return this.roomNumber;
    }

    /**
     * Getter method for the description attribute.
     *
     * @return: description of the room
     */
    public String getRoomDescription(){
        return this.roomDescription.replace("\n", " ");
    }


    /**
     * Getter method for the name attribute.
     *
     * @return: name of the room
     */
    public String getRoomName(){
        return this.roomName;
    }

    /**
     * getter method for roomQuestion
     * @return: the question associated with this room
     */
    public Question getRoomQuestion(){ return roomQuestion; }

    /**
     * sets the question for this room
       * @param question
     */
    public void setRoomQuestion(Question question){
        roomQuestion = question;
    }

    /**
     * Getter method for the visit attribute.
     *
     * @return: visit status of the room
     */
    public boolean getVisited(){
        return this.isVisited;
    }


    /**
     * Getter method for the objectsInRoom attribute.
     *
     * @return: ArrayList of objects in the room
     */
    public ArrayList<String> getObjectsInRoom(){
        ArrayList<String> objects = new ArrayList<>();
        for (int i = 0; i < this.objectsInRoom.size(); i++) {
            objects.add(this.objectsInRoom.get(i).getName());
        }
        return objects;
    }


    /**
     * Getter method for the motionTable attribute.
     *
     * @return: motion table of the room
     */
    public PassageTable getMotionTable(){
        return this.motionTable;
    }




}
