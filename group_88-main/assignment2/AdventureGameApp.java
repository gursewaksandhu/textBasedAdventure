import AdventureModel.AdventureGame;
import javafx.application.Application;
import javafx.stage.Stage;
import views.AdventureGameView;

import java.io.IOException;

public class AdventureGameApp extends  Application {

    AdventureGame model;
    AdventureGameView view;

    public static void main(String[] args) {
        launch(args);
    }

    /*
    * JavaFX is a Framework, and to use it we will have to
    * respect its control flow!  To start the game, we need
    * to call "start" ...
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        boolean audible = true; //change this if you don't want audio!
        this.model = new AdventureGame("TinyGame");
        this.view = new AdventureGameView(model, primaryStage);
    }

}
