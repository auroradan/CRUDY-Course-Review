package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.*;

public class CourseSearchController implements CourseUpdateCallback{

    private final DatabaseDriver database = new DatabaseDriver();

    @FXML
    private TableView<Course> coursesTable;

    @FXML
    private TableColumn<Course, String> subjectColumn;

    @FXML
    private TableColumn<Course, Integer> numberColumn;

    @FXML
    private TableColumn<Course, String> titleColumn;

    @FXML
    private TableColumn<Course, String> ratingColumn;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label searchLabel;
    @FXML
    private TextField subjectField;
    @FXML
    private TextField numberField;
    @FXML
    private TextField titleField;

    public void initialize() throws SQLException {
        Connection connection = DatabaseDriver.getConnection();
        String username = UserService.getCurrentUser();
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        loadCourses();
        welcomeLabel.setText("Welcome, " + username + ", to the CRUDdy Course Reviews!");
        searchLabel.setText("");
    }

    @FXML
    private void handleSearch() throws SQLException {
        String subject = subjectField.getText().trim();
        String numberString = numberField.getText().trim();
        String title = titleField.getText().trim();
        boolean isValid = true;
        if (subject.isEmpty() && numberString.isEmpty() && title.isEmpty()) {
            searchLabel.setText("All search fields are empty. Any existing filters have been reset.");
            searchLabel.setStyle("-fx-text-fill: red;");
            loadCourses();
            isValid = false;
        }
        else {
            if (!subject.isEmpty()) {
                if (subject.length() < 2 || subject.length() > 4 || !allLetters(subject)) {
                    searchLabel.setText("Subject must be 2-4 letters.");
                    searchLabel.setStyle("-fx-text-fill: red;");
                    isValid = false;
                }
            }
            if (!numberString.isEmpty()) {
                try {
                    int number = Integer.parseInt(numberString);
                    if (numberString.length() != 4) {
                        searchLabel.setText("Number must have exactly 4 digits.");
                        searchLabel.setStyle("-fx-text-fill: red;");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    searchLabel.setText("Number must be an integer.");
                    searchLabel.setStyle("-fx-text-fill: red;");
                    isValid = false;
                }
            }
            if (title.length() > 50) {
                searchLabel.setText("Title can only be a maximum of 50 characters long. Your title contains " + title.length() + " characters.");
                searchLabel.setStyle("-fx-text-fill: red;");
                isValid = false;
            }
        }
        if (isValid) {
            try {
                ObservableList<Course> searchResults = database.searchCoursesFromDatabase(subject, numberString, title);
                coursesTable.setItems(searchResults);
                searchLabel.setText("Search was successful.");
                searchLabel.setStyle("-fx-text-fill: red;");
            } catch (SQLException e) {
                e.printStackTrace();
                searchLabel.setText("An error occurred while searching for courses.");
                searchLabel.setStyle("-fx-text-fill: red;");
            }
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

    @FXML
    private void handleAddCourse() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("add_course_popup.fxml"));
            Parent root = fxmlLoader.load();
            AddCourseController addCourseController = fxmlLoader.getController();
            addCourseController.setCourseUpdateCallback(this);
            Stage stage = new Stage();
            UserService userService = new UserService();
            addCourseController.setStage(stage, userService);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Course");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewReviews(){
        Course selectedCourse = coursesTable.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            searchLabel.setText("Please select a course from the table below by clicking on it.");
            searchLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        searchLabel.setText("");
        String subject = selectedCourse.getSubject();
        int number = selectedCourse.getNumber();
        String title = selectedCourse.getTitle();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("review-course-screen.fxml"));
            Parent root = fxmlLoader.load();
            ReviewCourseController reviewCourseController = fxmlLoader.getController();
            reviewCourseController.setCourseDetails(subject, number, title);
            reviewCourseController.loadReviews();
            Scene scene = new Scene(root);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle("Course Reviews Screen");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearCourseSelection() {
        if (coursesTable != null) {
            coursesTable.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void handleLogOut(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("initial-login-screen.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Log-In Screen");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadCourses() throws SQLException {
        ObservableList<Course> courses = database.getCoursesFromDatabase();
        coursesTable.setItems(courses);
    }

    @Override
    public void onCourseUpdated() {
        try {
            loadCourses();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleMyReviews(){
        String user = UserService.getCurrentUser();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("my-reviews-screen.fxml"));
            Parent root = fxmlLoader.load();
            MyReviewsController myReviewsController = fxmlLoader.getController();
            UserService.setCurrentUser(user);
            myReviewsController.setUserDetails(user);
            myReviewsController.loadMyReviews(UserService.getCurrentUser());
            Scene scene = new Scene(root);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle("My Reviews Screen");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}