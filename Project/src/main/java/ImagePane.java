import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImagePane extends GridPane implements Initializable {
    @FXML
    public Label inputTxt;

    @FXML
    public ImageView imageContainer;

    public String pathDir = "output/images/";

    public ArrayList<File> images;

    public void getDataFromZip(){
        this.images = new ArrayList<File>();
        File dir = new File(pathDir);
        if (!dir.exists()){
            dir.mkdirs();
        }
        try{
            FileInputStream fis = new FileInputStream(inputTxt.getText());
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry zipEntry = zis.getNextEntry();
            //buffer for read and write data to file
            byte[] buffer = new byte[1024];
            while(zipEntry != null){
                String name = zipEntry.getName();
                if (!zipEntry.isDirectory()){
                    System.out.println("Unzipping " + name);
                    File newFile = new File(pathDir + File.separator + name);
                    //create directories for sub directories in zip
                    new File(newFile.getParent()).mkdirs();

                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    this.images.add(newFile);
                } else {
                    System.out.println("Unzipping dir " + name);
                    File newDir = new File(pathDir + File.separator + name);
                    newDir.mkdir();
                }
                //close this ZipEntry
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showImages(){
        new Thread(){
            public void run(){
                for(File img: images){
                    Platform.runLater(() ->{
                        Image image = new Image(img.toURI().toString());
                        imageContainer.setImage(image);
                    });
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        // System.out.println(images.get(0).getName());

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inputTxt.setText(Main.input);
        getDataFromZip();
        showImages();
        System.out.println(Main.input);
    }
}
