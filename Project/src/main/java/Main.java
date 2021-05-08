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
        System.out.println("Lista de parametros a usar:");
        System.out.println("-i, --input {path}*");
        System.out.println("El zip a usar para generar el resultado. Es obligatorio incluir un input.");
        System.out.println("Es importante que sea un zip");
        System.out.println("-o, --output {path}");
        System.out.println("Nombre donde se va a guardar el archivo generado");
        System.out.println("-e, --encode");
        System.out.println("Se aplicara la codificacion sobre el conjunto de imagenes obtenido como input");
        System.out.println("-d, --decode");
        System.out.println("Se aplicara la decodificacion sobre el conjunto de imagenes obtenido como input");
        System.out.println("--fps {value}");
        System.out.println("Numero de imagenes por segundos con los que se reproducira el video");
        System.out.println("--binarization {value}");
        System.out.println("Filtro de binarizacion utilizando como threshold el valor indicado");
        System.out.println("--negative");
        System.out.println("Se aplicara el filtro negativo sobre la imagen");
        System.out.println("--averaging {value}");
        System.out.println("Se aplica un filtro de mediana sobre zonas de value x value");
        System.out.println("--nTiles {value}");
        System.out.println("Numero de teselas en la cual dividir la imagen.");
        System.out.println("--seekRange {value}");
        System.out.println("Desplazamiento maximo en la busqueda de coincidencias");
        System.out.println("--GOP {value}");
        System.out.println("Numero de imagenes entre dos frames de referencia");
        System.out.println("--quality {value}");
        System.out.println("Factor de calidad que determina la coincidencia de dos teselas");
        System.out.println("-b --batch");
        System.out.println("No se mostrara la reproduccion del resultado.");
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
        // Mirar si es un zip o no
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

    public void setOutput(){

    }

    public void setEncode(){

    }

    public void setDecode(){

    }

    public void setFPS(){

    }

    public void setBinarization(){

    }

    public void setNegative(){

    }

    public void setAveraging(){

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

        if (params.contains("-o") || params.contains("--output")) {
            setOutput();
        }

        if (params.contains("-e") || params.contains("--encode")) {
            setEncode();
        }

        if (params.contains("-d") || params.contains("--decode")) {
            setDecode();
        }

        if (params.contains("--fps")) {
            setFPS();
        }

        if (params.contains("--binarization")) {
            setBinarization();
        }

        if (params.contains("--negative")) {
            setNegative();
        }

        if (params.contains("--averaging")) {
            setAveraging();
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
