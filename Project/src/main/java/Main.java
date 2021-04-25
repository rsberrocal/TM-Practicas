import com.beust.jcommander.JCommander;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {

    final static int WIDTH = 600;
    final static int HEIGHT = 400;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/main.fxml"));
        GridPane pane = loader.load();
        Scene scene = new Scene(pane, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("TITULO");
        primaryStage.show();
    }

    public static void main(String... args){
        //JComander
        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);

        launch(args);
    }
}
