package edu.virginia.sde.reviews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CourseSearchScreen {

    public static void load(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(CourseSearchScreen.class.getResource("course-search-screen.fxml"));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
