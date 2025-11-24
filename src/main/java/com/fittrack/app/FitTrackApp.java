package com.fittrack.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FitTrackApp extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize Database
        new com.fittrack.db.DatabaseManager().initializeDatabase();

        scene = new Scene(loadFXML("login"), 1024, 768);
        scene.getStylesheets().add(FitTrackApp.class.getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("FitTrack");
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FitTrackApp.class.getResource("/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
