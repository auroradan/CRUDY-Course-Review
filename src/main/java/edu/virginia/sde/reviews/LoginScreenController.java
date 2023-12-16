package edu.virginia.sde.reviews;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class LoginScreenController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final DatabaseDriver database = new DatabaseDriver();

    public void initialize() throws SQLException {
        Connection connection = DatabaseDriver.getConnection();
        database.createTables();
    }

    @FXML
    private void handleLoginAction() {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (authenticate(username, password) == 1) {
                // Login successful
                UserService.setCurrentUser(username);
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setTitle("Course Search Screen");
                CourseSearchScreen.load(stage);
            } else {
                // Login failed
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error while accessing the database.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int authenticate(String username, String password) throws SQLException {
        return database.userExists(new User(username, password));
    }

    @FXML
    private void handleCreateUser() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("create-user-popup.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            UserService userService = new UserService();
            CreateUserController controller = fxmlLoader.getController();
            controller.setStage(stage, userService);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Create User");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void handleCloseApplication() {
        Platform.exit();
    }
}