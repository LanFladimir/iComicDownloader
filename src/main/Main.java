package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("mainpage.fxml"));
        primaryStage.setTitle("iComic Downloader");
        primaryStage.setScene(new Scene(root, 630, 400));
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> System.exit(-1));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
