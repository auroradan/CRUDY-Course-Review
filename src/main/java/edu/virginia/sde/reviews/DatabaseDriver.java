package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("SqlResolve")
public class DatabaseDriver {
    private static final String sqliteFilename = "crud_course_review.sqlite";

    //AI Agent: ChatGPT
    //Prompt: how to make it so that the code only needs to connect once to the database so that there isn't
    //a worry about having multiple connections to a sqlite database
    private static Connection connection;

    static {
        try {
            // Establish the connection when the class is loaded
            connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFilename);
            connection.setAutoCommit(false);
            // Other initialization code...
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database", e);
        }
    }

    public static Connection getConnection(){
        return connection;
    }

    /**
     * Commit all changes since the connection was opened OR since the last commit/rollback
     */
    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * Creates the three database tables Stops, BusLines, and Routes, with the appropriate constraints including
     * foreign keys, if they do not exist already. If they already exist, this method does nothing.
     * As a hint, you'll need to create Routes last, and Routes must include Foreign Keys to Stops and
     * BusLines.
     * @throws SQLException
     */
    public void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String createUsersTable = "CREATE TABLE IF NOT EXISTS Users (" +
                    "Username TEXT PRIMARY KEY NOT NULL," +
                    "Password TEXT NOT NULL" +
                    ");";
            String courseTable = "CREATE TABLE IF NOT EXISTS Courses (" +
                    "ID TEXT PRIMARY KEY NOT NULL," +
                    "Subject TEXT NOT NULL," +
                    "Numb INTEGER NOT NULL," +
                    "Title TEXT NOT NULL," +
                    "Rating TEXT NOT NULL" +
                    ");";
            String reviewTable = "CREATE TABLE IF NOT EXISTS Reviews (" +
                    "ID TEXT PRIMARY KEY NOT NULL," +
                    "User TEXT NOT NULL," +
                    "Course TEXT NOT NULL," +
                    "Rating INTEGER NOT NULL," +
                    "Review TEXT NOT NULL," +
                    "Timestamp TEXT NOT NULL," +
                    "FOREIGN KEY (User) REFERENCES Users(username) ON DELETE CASCADE," +
                    "FOREIGN KEY (Course) REFERENCES Courses(ID) ON DELETE CASCADE" +
                    ");";
            statement.execute(createUsersTable);
            statement.execute(courseTable);
            statement.execute(reviewTable);
        }
    }

    public void addUser(User user) throws SQLException {
        String addUserQuery = "INSERT INTO Users (Username, Password) " +
                "SELECT ?, ? WHERE NOT EXISTS (" +
                "SELECT 1 FROM Users WHERE Username = ?);";
        try (PreparedStatement preparedStatement = connection.prepareStatement(addUserQuery)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getUsername());
            preparedStatement.executeUpdate();
        }
    }

    public int userExists(User user) throws SQLException {
        String username = user.getUsername();
        String password = user.getPassword();
        String query = "SELECT COUNT(*) FROM Users WHERE Username = ? AND Password = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    if (count > 0) {
                        return 1;
                    }
                }
            }
        }
        query = "SELECT COUNT(*) FROM Users WHERE Username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    if (count > 0) {
                        return 0;
                    }
                }
            }
        }
        return -1;
    }

    public void addCourse(Course course) throws SQLException {
        if (!courseExists(course)) {
            String addUserQuery = "INSERT INTO Courses (ID, Subject, Numb, Title, Rating) " +
                    "VALUES (?, ?, ?, ?, ?);";
            try (PreparedStatement preparedStatement = connection.prepareStatement(addUserQuery)) {
                preparedStatement.setString(1, course.getID());
                preparedStatement.setString(2, course.getSubject());
                preparedStatement.setInt(3, course.getNumber());
                preparedStatement.setString(4, course.getTitle().strip());
                preparedStatement.setString(5, course.getRating());
                preparedStatement.executeUpdate();
            }
        }
    }

    public boolean courseExists(Course course) throws SQLException {
        String title = course.getTitle().toLowerCase().strip();
        String query = "SELECT COUNT(*) FROM Courses WHERE LOWER(Title) = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, title);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    public Course getCourseByFields(String subject, int number, String title) throws SQLException {
        String query = "SELECT * FROM Courses WHERE Subject = ? AND Numb = ? AND Title = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, subject);
            preparedStatement.setInt(2, number);
            preparedStatement.setString(3, title);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String foundSubject = resultSet.getString("Subject");
                    int foundNumber = resultSet.getInt("Numb");
                    String foundTitle = resultSet.getString("Title");
                    String rating = resultSet.getString("Rating");
                    return new Course(foundSubject, foundNumber, foundTitle, rating);
                } else {
                    return null;
                }
            }
        }
    }

    public ObservableList<Course> getCoursesFromDatabase() throws SQLException {
        createTables();
        commit();
        ObservableList<Course> courses = FXCollections.observableArrayList();
        String query = "SELECT Subject, Numb, Title, Rating FROM Courses ORDER BY Subject ASC, Numb ASC";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String subject = resultSet.getString("Subject");
                    int number = resultSet.getInt("Numb");
                    String title = resultSet.getString("Title");
                    String rating = resultSet.getString("Rating");
                    Course course = new Course(subject, number, title, rating);
                    String averageRating = calculateAverageCourseRating(course);
                    course.setRating(averageRating);
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public ObservableList<Course> searchCoursesFromDatabase(String subject, String number, String title) throws SQLException {
        createTables();
        commit();
        ObservableList<Course> courses = FXCollections.observableArrayList();
        StringBuilder query = new StringBuilder("SELECT Subject, Numb, Title FROM Courses WHERE 1=1");
        if (!subject.isEmpty()) {
            query.append(" AND LOWER(Subject) = LOWER(?)");
        }
        if (!number.isEmpty()) {
            query.append(" AND Numb = ?");
        }
        if (!title.isEmpty()) {
            query.append(" AND LOWER(Title) LIKE LOWER(?)");
        }
        query.append(" ORDER BY Subject ASC, Numb ASC");
        try (PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {
            int parameterIndex = 1;
            if (!subject.isEmpty()) {
                preparedStatement.setString(parameterIndex++, subject);
            }
            if (!number.isEmpty()) {
                preparedStatement.setInt(parameterIndex++, Integer.parseInt(number));
            }
            if (!title.isEmpty()) {
                preparedStatement.setString(parameterIndex, "%" + title + "%");
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String resultSubject = resultSet.getString("Subject");
                    int resultNumber = resultSet.getInt("Numb");
                    String resultTitle = resultSet.getString("Title");
                    Course course = new Course(resultSubject, resultNumber, resultTitle);
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public void addReview(Review review) throws SQLException {
        String query = "INSERT INTO Reviews (ID, User, Course, Rating, Review, Timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, review.getID());
            preparedStatement.setString(2, review.getUser());
            preparedStatement.setString(3, review.getCourse());
            preparedStatement.setInt(4, review.getRating());
            preparedStatement.setString(5, review.getReview());
            preparedStatement.setString(6, review.getTimestamp());
            preparedStatement.executeUpdate();
        }
    }

    public ObservableList<Review> getReviewsForCourse(Course course) throws SQLException {
        ObservableList<Review> reviews = FXCollections.observableArrayList();
        String query = "SELECT * FROM Reviews WHERE Course = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, course.getID());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String ID = resultSet.getString("ID");
                    String user = resultSet.getString("User");
                    int rating = resultSet.getInt("Rating");
                    String review = resultSet.getString("Review");
                    String timestamp = resultSet.getString("Timestamp");
                    Review courseReview = new Review(ID, user, course, rating, review, timestamp);
                    reviews.add(courseReview);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public String calculateAverageCourseRating(Course course) throws SQLException {
        String courseId = course.getID();
        String query = "SELECT AVG(Rating) AS AverageRating FROM Reviews WHERE Course = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, courseId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    double averageRating = resultSet.getDouble("AverageRating");
                    if (!resultSet.wasNull()) {
                        return String.format("%.2f", averageRating);
                    }
                }
            }
        }
        return "N/A";
    }

    public Course getCourseByID(String ID) throws SQLException {
        String query = "SELECT * FROM Courses WHERE ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, ID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String Subject = resultSet.getString("Subject");
                    int Number = resultSet.getInt("Numb");
                    String Title = resultSet.getString("Title");
                    String rating = resultSet.getString("Rating");
                    return new Course(Subject, Number, Title, rating);
                } else {
                    return null;
                }
            }
        }
    }

    public boolean userReviewExists(String user, Course course) throws SQLException {
        String ID = course.getID();
        String query = "SELECT COUNT(*) FROM Reviews WHERE User = ? AND Course = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, ID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    public ObservableList<Review> getReviewsForUser(String user) throws SQLException {
        ObservableList<Review> reviews = FXCollections.observableArrayList();
        String query = "SELECT * FROM Reviews WHERE User = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, user);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String ID = resultSet.getString("ID");
                    Course course = getCourseByID(resultSet.getString("Course"));
                    int rating = resultSet.getInt("Rating");
                    String review = resultSet.getString("Review");
                    String timestamp = resultSet.getString("Timestamp");
                    Review courseReview = new Review(ID, user, course, rating, review, timestamp);
                    reviews.add(courseReview);
                }
            }
        }
        return reviews;
    }

    public void updateReview(Review review) throws SQLException {
        String query = "UPDATE Reviews SET Rating = ?, Review = ?, Timestamp = ? WHERE ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, review.getRating());
            preparedStatement.setString(2, review.getReview());
            preparedStatement.setString(3, review.getTimestamp());
            preparedStatement.setString(4, review.getID());
            preparedStatement.executeUpdate();
        }
    }

    public void deleteReview(Review review) throws SQLException {
        String query = "DELETE FROM Reviews WHERE ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, review.getID());
            preparedStatement.executeUpdate();
        }
    }
}