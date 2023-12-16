package edu.virginia.sde.reviews;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class ReviewCourseController {

    private final DatabaseDriver database = new DatabaseDriver();

    @FXML
    private TableView<Review> reviewTable;
    @FXML
    private TableColumn<Review, String> ratingColumn;
    @FXML
    private TableColumn<Review, String> timestampColumn;
    @FXML
    private TableColumn<Review, String> reviewColumn;
    @FXML
    private Label courseLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label reviewLabel;
    private UserService userService;
    private String subject;
    private int number;
    private String title;
    private boolean sceneLoaded = false;

    public void setCourseDetails(String subject, int number, String title) throws SQLException {
        this.subject = subject;
        this.number = number;
        this.title = title;
        updateCourseLabel();
        updateRatingLabel();
    }

    private void updateCourseLabel() {
        courseLabel.setText(subject + " " + number + ": " + title);
    }

    private void updateRatingLabel() throws SQLException {
        Course course = database.getCourseByFields(subject, number, title);
        String averageRating = database.calculateAverageCourseRating(course);
        ratingLabel.setText("Average Rating: " + averageRating);
    }

    public void initialize() throws SQLException {
        Connection connection = DatabaseDriver.getConnection();
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        ratingColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f", (double) cellData.getValue().getRating())));
        reviewColumn.setCellValueFactory(new PropertyValueFactory<>("review"));
        loadReviews();
    }

    @FXML
    private void handleAddReview() {
        try {
            String username = userService.getCurrentUser();
            Course course = database.getCourseByFields(subject, number, title);
            if (database.userReviewExists(username, course)) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Your review for this course already exists.");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("add_review_popup.fxml"));
            Parent root = fxmlLoader.load();
            AddReviewController addReviewController = fxmlLoader.getController();
            Stage stage = new Stage();
            UserService userService = new UserService();
            addReviewController.setStage(stage, userService);
            addReviewController.setCourseDetails(subject, number, title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Review");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            if (addReviewController.isReviewAdded()) {
                reviewLabel.setText("Review added successfully.");
                reviewLabel.setStyle("-fx-text-fill: red;");
                loadReviews();
            } else {
                reviewLabel.setText("Review addition canceled or failed.");
                reviewLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleEditReview() {
        Review selectedReview = reviewTable.getSelectionModel().getSelectedItem();
        if (selectedReview == null) {
            reviewLabel.setText("Please select a review from the table below by clicking on it.");
            reviewLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        String username = userService.getCurrentUser();;
        try {
            if (selectedReview.getUser().equals(username)) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("edit_review_popup.fxml"));
                Parent root = fxmlLoader.load();
                EditReviewController editReviewController = fxmlLoader.getController();
                editReviewController.setReview(selectedReview);
                Stage stage = new Stage();
                editReviewController.setStage(stage);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Edit Review");
                stage.setScene(new Scene(root));
                stage.showAndWait();
                if (editReviewController.isReviewEdited()) {
                    reviewLabel.setText("Review edited successfully.");
                    reviewLabel.setStyle("-fx-text-fill: red;");
                    loadReviews();
                } else {
                    reviewLabel.setText("Review edit canceled or failed.");
                    reviewLabel.setStyle("-fx-text-fill: red;");
                }
            } else {
                reviewLabel.setText("You can only edit your own review.");
                reviewLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleDeleteReview() {
        Review selectedReview = reviewTable.getSelectionModel().getSelectedItem();
        if (selectedReview == null) {
            reviewLabel.setText("Please select a review from the table below by clicking on it.");
            reviewLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        String username = userService.getCurrentUser();;
        try {
            if (selectedReview.getUser().equals(username)) {
                boolean confirmDelete = showConfirmationDialog("Confirm Delete", "Are you sure you want to delete this review?");
                if (confirmDelete) {
                    database.deleteReview(selectedReview);
                    database.commit();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Review deleted successfully.");
                    reviewLabel.setText("Review deleted successfully.");
                    reviewLabel.setStyle("-fx-text-fill: red;");
                    loadReviews();
                }
            } else {
                reviewLabel.setText("You can only delete your own review.");
                reviewLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            reviewLabel.setText("An error occurred while deleting the review.");
            reviewLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirmationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    private void handleBackToCatalog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("course-search-screen.fxml"));
            Parent root = fxmlLoader.load();
            CourseSearchController controller = fxmlLoader.getController();
            controller.clearCourseSelection();
            Stage stage = (Stage) courseLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Course Search Screen");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadReviews() throws SQLException {
        Course course = database.getCourseByFields(subject, number, title);
        String currentUser = userService.getCurrentUser();
        if (course != null) {
            ObservableList<Review> reviews = database.getReviewsForCourse(course);
            Optional<Review> userReview = reviews.stream()
                    .filter(review -> review.getUser().equals(currentUser))
                    .findFirst();
            reviewTable.setRowFactory(tv -> new TableRow<Review>() {
                @Override
                protected void updateItem(Review review, boolean empty) {
                    super.updateItem(review, empty);
                    if (empty || review == null) {
                        setStyle("");
                    } else {
                        String currentUser = userService.getCurrentUser();
                        if (review.getUser().equals(currentUser)) {
                            if (isSelected()) {
                                setStyle("");
                            } else {
                                setStyle("-fx-background-color: #ffff99;");
                            }
                        } else {
                            setStyle("");
                        }
                    }
                }
            });
            reviewTable.setItems(reviews);
            updateRatingLabel();
            if (!sceneLoaded) {
                if (userReview.isPresent()) {
                    reviewLabel.setText("Your review for this course is highlighted in the table.");
                    reviewLabel.setStyle("-fx-text-fill: red;");
                } else {
                    reviewLabel.setText("You have not submitted a review for this course.");
                    reviewLabel.setStyle("-fx-text-fill: red;");
                }
                sceneLoaded = true;
            }
        } else {
            reviewTable.setItems(FXCollections.observableArrayList());
        }
    }
}