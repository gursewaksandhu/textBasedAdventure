package AdventureModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is a representation of a SINGLETON Design pattern
 */
public class QuestionTracker implements Serializable {

    /**
     * Keeps track of total questions correct
     */
    private int questionsCorrect;

    /**
     * Keeps track of total number of attempts made
     */
    private int questionsAttempts;

    /**
     * Keeps track of [correct, attempts] by question topic
     */
    private HashMap<String, ArrayList<Integer>> questionsByTopic;

    /**
     * keeps track of the total number of hints used
     */
    private int totalHints;

    /**
     * Keeps track of total number of skips
     */
    private int totalSkips;

    /**
     * keeps track of longest correct streak
     */
    private int correctStreak;
    /**
     * Keeps a list of all the questions the play has Seen
     */
    private ArrayList<Question> questionsSeen;

    public QuestionTracker(){
        this.questionsCorrect = 0;
        this.questionsAttempts = 0;
        this.questionsByTopic = new HashMap<String, ArrayList<Integer>>(2);
        this.totalHints = 0;
        this.totalSkips = 0;
        this.questionsSeen = new ArrayList<Question>();
    }

    private void addCorrect(){ this.questionsCorrect++; }

    private void addAttempts(){ this.questionsAttempts++; }

    private void addTopicRecord(String topic, Boolean correct){
        if (this.questionsByTopic.containsKey(topic)){
            int corr = this.questionsByTopic.get(topic).get(0);
            if (correct) {
                corr = this.questionsByTopic.get(topic).get(0) + 1;
                //this.questionsByTopic.get(topic).add(0, corr);
            }
            int attempts = this.questionsByTopic.get(topic).get(1) + 1;;

            ArrayList<Integer> val = new ArrayList<>();
            val.add(corr);
            val.add(attempts);
            this.questionsByTopic.put(topic, val);


            this.questionsByTopic.get(topic).add(1, attempts);
        }
        else{
            ArrayList<Integer> val = new ArrayList<>(2);
            if (!correct) {
                val.add(0);
            }
            else{
                val.add(1);
            }
            val.add(1);
            this.questionsByTopic.put(topic, val);
        }
    }

    private void addQuestionsSeen(Question question){ this.questionsSeen.add(question); }

    public void hintUsed(){
        this.totalHints++;
    }

    public void skipsUsed(){
        this.totalSkips++;
    }

    public void addAttempt(Question question, Boolean correct){
        if (correct){
            addCorrect();
            this.correctStreak++;
        } else{
            this.correctStreak = 0;
        }
        addAttempts();
        // check if Questions seen already contains the question
        if (!this.questionsSeen.contains(question))
            addQuestionsSeen(question);
        addTopicRecord(question.questionTopic, correct);
    }

    public String createReport(){
        String str = "";

        str += "Questions Correct: " + this.questionsCorrect + "\n"
                + "Question Attempts: " + this.questionsAttempts + "\n"
                + "Hints Used: " + this.totalHints + "\n"
                + "Skips Used: " + this.totalSkips + "\n"
                + "\n";

        for (String topic : this.questionsByTopic.keySet()){
            str += topic + "\n" + "Percent Correct: ";
            int corr = this.questionsByTopic.get(topic).get(0);
            int atte = this.questionsByTopic.get(topic).get(1);
            float roundedPercent = (float) corr / atte;
            roundedPercent = roundedPercent * 100;
            str += roundedPercent + "%" + "\n";
        }

        str += "\n";
        str += "Questions Seen: " + "\n";

        // Show list of all questions seen
        for (Question q: this.questionsSeen){
            str += q.questionQuestion + "\n";
        }

        System.out.println(str);
        return str;
    }
}
