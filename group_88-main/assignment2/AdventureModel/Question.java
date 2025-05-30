package AdventureModel;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class Question implements Serializable {


    private final Integer questionNumber;
    /**
     * The topic of the Question
     */
    public String questionTopic;
    /**
     * The sentence of the Question
     */
    public String questionQuestion;
    /**
     * A hint to for the Question
     */
    public Queue<String> questionHint;
    /**
     * the answer to the Question
     */
    private final String questionAnswer;   // Change this to private!!!

    /**
     * Boolean representing whether a question has been correctly answered
     */
    public boolean questionAnswered;

    public Question(int questionNumber, String questionTopic, String questionQuestion, String[] Hint, String questionAnswer){
        this.questionNumber = questionNumber;
        this.questionTopic = questionTopic;
        this.questionQuestion = questionQuestion;
        this.questionHint = new LinkedList<String>();
        Collections.addAll(this.questionHint, Hint); // A super method that adds all values in the String[] to the queue

        this.questionAnswer = questionAnswer;
        this.questionAnswered = false;
    }

    /**
     * This method returns the hint for the Question
     * @return: a string containing the hint
     */
    public String getQuestionHint() {
        String hint = this.questionHint.remove();
        this.questionHint.add(hint);
        return hint;
    }

    /**
     * This method returns the question for the Question
     * @return: a string containing the question
     */
    public String getQuestionQuestion() {
        return questionQuestion;
    }

    /**
     * This method checks if the given answer is correct.
     * @param: answer to be compared
     * @return: true if answer given matches correct answer
     */
    public boolean checkAnswer(String answer){
        this.questionAnswered = this.questionAnswer.equalsIgnoreCase(answer);
        return this.questionAnswered;
    }

    /**
     * This method returns the question number
     * @return: an Integer value representing the question number
     */
    public Integer getQuestionNumber() {
        return questionNumber;
    }
}
