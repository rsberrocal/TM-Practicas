import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Main extends Application {

    public static String input;
    public List<String> params;

    public boolean hasInput = false;

    final static int WIDTH = 600;
    final static int HEIGHT = 400;

    public void showHelp() {
        System.out.println("@232");
        System.out.println("22323");

        System.exit(1);
    }

    public void setInput() throws Exception {
        int index = params.indexOf("-i");
        if (index == -1) {
            index = params.indexOf("--input");
        }
        if (index == -1) {
            printError("--input es un parametro necesario.");
        }
        int dataIdx = index + 1;
        if (dataIdx > params.size() -1){
            printError("No se ha encotrado el parametro");
        }
        System.out.println();
        if (params.get(dataIdx).contains("-") || params.get(dataIdx).contains("--") ){
            printError("El parametro no es correcto");
        } else{
            input = params.get(dataIdx);
            hasInput = true;
        }
    }

    public void printError(String error) {
        System.err.println(error);
        System.out.println("Use -h o --help para obtener mas informacion");
        System.exit(1);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // No implementamos JCommander debido a que JavaFx incorpora su parser propio de argumentos
        Parameters parameters = getParameters();
        if (parameters.getRaw().isEmpty()) {
            System.out.println("Faltan parametros, use -h o --help para saber que parametros usar");
            System.exit(0);
        }
        params = parameters.getRaw();
        if (params.contains("-h") || params.contains("--help")) {
            showHelp();
        }
        if (params.contains("-i") || params.contains("--input")) {
            setInput();
        }
        if (!hasInput) {
            printError("--input es un parametro necesario.");
        }
        // Faltaria poner el resto de parametros

        initApp(primaryStage);
    }

    public void initApp(Stage primaryStage) throws Exception {

        //Parent root = FXMLLoader.load(getClass().getResource("view/imageview.fxml"));
        // FXMLLoader loader = new FXMLLoader(getClass().getResource("view/main.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/imageview.fxml"));
        Parent root = loader.load();
        // GridPane pane = loader.load();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("TITULO");
        primaryStage.show();
    }

    public static void main(String... args) {
        launch(args);
        // launch(args);
    }
}
