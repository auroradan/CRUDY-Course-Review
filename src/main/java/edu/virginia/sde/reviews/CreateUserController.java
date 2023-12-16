package edu.virginia.sde.reviews;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;

public class CreateUserController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;
    private Stage stage;
    private UserService userService;

    public void setStage(Stage stage, UserService userService) {
        this.stage = stage;
        this.userService = userService;
    }

    private final DatabaseDriver database = new DatabaseDriver();

    public void initialize() throws SQLException {
        Connection connection = DatabaseDriver.getConnection();
        database.createTables();
    }
    @FXML
    private void handleCreateUserAction() {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (database.userExists(new User(username, password)) == 1) {
                showAlert(Alert.AlertType.ERROR, "User Creation Failed", "User already exists");
            }
            else if (database.userExists(new User(username, password)) == 0) {
                showAlert(Alert.AlertType.ERROR, "User Creation Failed", "Username already in use. Please choose another username.");
            }
            else if (username.equals("") || password.equals("")){
                showAlert(Alert.AlertType.ERROR, "User Creation Failed", "Both the username and password field must be non-empty");
            }
            else if (password.length() < 8){
                showAlert(Alert.AlertType.ERROR, "User Creation Failed", "Password must be at least 8 characters long");
            }
            else {
                database.addUser(new User(username, password));
                showAlert(Alert.AlertType.INFORMATION, "User Successfully Created", "Your account has successfully been created!\nYou can now log in using your new credentials.");
                database.commit();
                stage.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error while accessing the database.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
