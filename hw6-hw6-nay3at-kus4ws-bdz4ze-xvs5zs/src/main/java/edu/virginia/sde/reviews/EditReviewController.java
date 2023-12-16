package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EditReviewController {

    private final DatabaseDriver database = new DatabaseDriver();

    private CourseUpdateCallback courseUpdateCallback;

    @FXML
    private TextField ratingInput;
    @FXML
    private TextArea reviewInput;
    private Stage stage;
    private Review review;
    private boolean reviewEdited = false;

    public void initialize() throws SQLException {
        Connection connection = DatabaseDriver.getConnection();
        database.createTables();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setReview(Review review) {
        this.review = review;
        ratingInput.setText(Integer.toString(review.getRating()));
        reviewInput.setText(review.getReview());
    }

    public void setCourseUpdateCallback(CourseUpdateCallback callback) {
        this.courseUpdateCallback = callback;
    }

    @FXML
    private void editReviewSubmit() {
        try {
            String ratingString = ratingInput.getText();
            String reviewString = reviewInput.getText();
            int rating;
            try {
                rating = Integer.parseInt(ratingString);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Rating must be an integer.");
                return;
            }
            if (rating < 0 || rating > 5) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Rating must be between 1-5.");
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM-dd-yyyy");
            String timestamp = now.format(formatter);
            review.setRating(rating);
            review.setReview(reviewString);
            review.setTimestamp(timestamp);
            database.updateReview(review);
            database.commit();
            reviewEdited = true;
            showAlert(Alert.AlertType.INFORMATION, "Success", "Review edited successfully.");
            ratingInput.clear();
            reviewInput.clear();
            stage.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error while accessing the database.");
        }
    }

    public boolean isReviewEdited() {
        return reviewEdited;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}