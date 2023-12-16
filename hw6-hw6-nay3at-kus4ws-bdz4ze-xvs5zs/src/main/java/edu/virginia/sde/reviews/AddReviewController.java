package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.sql.Connection;
import java.sql.SQLException;

public class AddReviewController {

    private final DatabaseDriver database = new DatabaseDriver();

    private CourseUpdateCallback courseUpdateCallback;

    @FXML
    private TextField ratingInput;
    @FXML
    private TextArea reviewInput;
    private Stage stage;
    private UserService userService;
    private String subject;
    private int number;
    private String title;
    private boolean reviewAdded = false;
    public void initialize() throws SQLException {
        Connection connection = DatabaseDriver.getConnection();
        database.createTables();
    }
    public void setStage(Stage stage, UserService userService) {
        this.stage = stage;
        this.userService = userService;
    }

    public void setCourseDetails(String subject, int number, String title) {
        this.subject = subject;
        this.number = number;
        this.title = title;
    }

    public void setCourseUpdateCallback(CourseUpdateCallback callback) {
        this.courseUpdateCallback = callback;
    }

    @FXML
    private void addReviewSubmit(){
        try{
            String ratingString = ratingInput.getText();
            String reviewString = reviewInput.getText();
            int rating;
            try {
                rating = Integer.parseInt(ratingString);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Rating must be an integer.");
                return;
            }
            if (rating < 1 || rating > 5){
                showAlert(Alert.AlertType.ERROR, "Input Error", "Rating must be between 1-5.");
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM-dd-yyyy");
            String timestamp = now.format(formatter);
            String username = userService.getCurrentUser();
            Course course = database.getCourseByFields(subject, number, title);
            if (database.userReviewExists(username, course)) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Your review for this course already exists.");
                return;
            }
            Review review = new Review(username, database.getCourseByFields(subject, number, title), rating, reviewString, timestamp);
            database.addReview(review);
            database.commit();
            reviewAdded = true;
            showAlert(Alert.AlertType.INFORMATION, "Success", "Review added successfully.");
            ratingInput.clear();
            reviewInput.clear();
            stage.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error while accessing the database.");
        }
    }

    public boolean isReviewAdded() {
        return reviewAdded;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}