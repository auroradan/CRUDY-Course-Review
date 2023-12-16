package edu.virginia.sde.reviews;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class MyReviewsController {

    private final DatabaseDriver database = new DatabaseDriver();

    @FXML
    private TableView<Review> myReviewsTable;
    @FXML
    private TableColumn<Review, String> courseColumn;
    @FXML
    private TableColumn<Review, String> ratingColumn;
    @FXML
    private TableColumn<Review, String> reviewColumn;
    @FXML
    private Label userLabel;
    @FXML
    private Label myReviewLabel;
    private String user;
    private int number;
    private String title;

    public void setUserDetails(String user) throws SQLException {
        this.user = user;
        updateUserLabel();
    }

    private void updateUserLabel() {
        userLabel.setText(user + "'s Reviews");
    }

    public void initialize() throws SQLException {
        Connection connection = DatabaseDriver.getConnection();
        courseColumn.setCellValueFactory(cellData -> {
            Review review = cellData.getValue();
            String courseID = review.getCourse();
            try {
                Course associatedCourse = database.getCourseByID(courseID);
                if (associatedCourse != null) {
                    String subject = associatedCourse.getSubject();
                    int number = associatedCourse.getNumber();
                    String title = associatedCourse.getTitle();
                    return new SimpleStringProperty(subject + " " + number + ": " + title);
                } else {
                    return new SimpleStringProperty("Error: Associated course not found.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return new SimpleStringProperty("Error: Unable to retrieve course details.");
            }
        });
        ratingColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", (double) cellData.getValue().getRating())));
        reviewColumn.setCellValueFactory(new PropertyValueFactory<>("review"));
        loadMyReviews(user);
    }

    @FXML
    private void handleViewMyReviews(){
        Review selectedReview = myReviewsTable.getSelectionModel().getSelectedItem();
        if (selectedReview == null) {
            myReviewLabel.setText("Please select a review from the table below by clicking on it.");
            myReviewLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        myReviewLabel.setText("");
        String courseID = selectedReview.getCourse();
        try {
            Course associatedCourse = database.getCourseByID(courseID);
            if (associatedCourse != null) {
                String subject = associatedCourse.getSubject();
                int number = associatedCourse.getNumber();
                String title = associatedCourse.getTitle();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("review-course-screen.fxml"));
                Parent root = fxmlLoader.load();
                ReviewCourseController reviewCourseController = fxmlLoader.getController();
                reviewCourseController.setCourseDetails(subject, number, title);
                reviewCourseController.loadReviews();
                Scene scene = new Scene(root);
                Stage stage = (Stage) userLabel.getScene().getWindow();
                stage.setTitle("Course Reviews Screen");
                stage.setScene(scene);
                stage.show();
            } else {
                myReviewLabel.setText("Error: Associated course not found.");
                myReviewLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleBackToCatalog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("course-search-screen.fxml"));
            Parent root = fxmlLoader.load();
            CourseSearchController controller = fxmlLoader.getController();
            controller.clearCourseSelection();
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Course Search Screen");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMyReviews(String user) throws SQLException {
        ObservableList<Review> reviews = database.getReviewsForUser(user);
        myReviewsTable.setItems(reviews);
    }
}