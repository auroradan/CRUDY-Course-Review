package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class AddCourseController {

    private final DatabaseDriver database = new DatabaseDriver();

    private CourseUpdateCallback courseUpdateCallback;

    @FXML
    private TextField subjectInput;
    @FXML
    private TextField numberInput;
    @FXML
    private TextField titleInput;
    private Stage stage;
    private UserService userService;

    public void setStage(Stage stage, UserService userService) {
        this.stage = stage;
        this.userService = userService;
    }

    public void initialize() throws SQLException {
        Connection connection = DatabaseDriver.getConnection();
        database.createTables();
    }

    public void setCourseUpdateCallback(CourseUpdateCallback callback) {
        this.courseUpdateCallback = callback;
    }

    @FXML
    private void addCourseSubmit(){
        try {
            String subject = subjectInput.getText().toUpperCase();
            String numberString = numberInput.getText();
            String title = titleInput.getText();
            if (subject.isEmpty() || numberString.isEmpty() || title.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "All fields are required.");
                return;
            }
            if (subject.length() < 2 || subject.length() > 4 || !allLetters(subject)){
                showAlert(Alert.AlertType.ERROR, "Input Error", "Subject must be 2-4 letters.");
                return;
            }
            int number;
            try {
                number = Integer.parseInt(numberString);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Number must be an integer.");
                return;
            }
            Course course = new Course(subject, number, title);
            if (database.courseExists(course)) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Course already exists.");
                return;
            }
            if (numberString.length() != 4){
                showAlert(Alert.AlertType.ERROR, "Input Error", "Number must have exactly 4 digits.");
                return;
            }
            if (title.length() > 50){
                showAlert(Alert.AlertType.ERROR, "Input Error",
                        "Title can only be a maximum of 50 characters long. Your title contains "
                                + title.length() + " characters.");
                return;
            }
            database.addCourse(course);
            database.commit();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Course added successfully.");
            subjectInput.clear();
            numberInput.clear();
            titleInput.clear();
            stage.close();
            if (courseUpdateCallback != null) {
                courseUpdateCallback.onCourseUpdated();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error while accessing the database.");
        }
    }

    private boolean allLetters(String str){
        String alpha = "abcdefghijklmnopqrstuvwxyz";
        str = str.toLowerCase();
        for (int i = 0; i < str.length(); i++){
            if (!alpha.contains(str.substring(i, i + 1))){
                return false;
            }
        }
        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}