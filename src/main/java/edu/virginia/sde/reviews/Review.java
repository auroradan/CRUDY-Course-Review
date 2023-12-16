package edu.virginia.sde.reviews;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;

public class Review {
    private final String ID;
    private final String user;
    private final SimpleStringProperty course;
    private SimpleIntegerProperty rating;
    private SimpleStringProperty review;
    private SimpleStringProperty timestamp;

    public Review(String user, Course course, int rating, String review, String timestamp) {
        this.ID = course + user;
        this.user = user;
        this.course = new SimpleStringProperty(course.getID());
        this.rating = new SimpleIntegerProperty(rating);
        this.review = new SimpleStringProperty(review);
        this.timestamp = new SimpleStringProperty(timestamp);
    }

    public Review(String ID, String user, Course course, int rating, String review, String timestamp) {
        this.ID = ID;
        this.user = user;
        this.course = new SimpleStringProperty(course.getID());
        this.rating = new SimpleIntegerProperty(rating);
        this.review = new SimpleStringProperty(review);
        this.timestamp = new SimpleStringProperty(timestamp);
    }

    public String getID() {
        return ID;
    }

    public String getUser() {
        return user;
    }

    public String getCourse() {
        return course.get();
    }

    public int getRating() {
        return rating.get();
    }

    public String getReview() {
        return review.get();
    }

    public String getTimestamp() {
        return timestamp.get();
    }

    public void setRating(int rating) {
        this.rating.set(rating);
    }

    public void setReview(String review) {
        this.review.set(review);
    }

    public void setTimestamp(String timestamp) {
        this.timestamp.set(timestamp);
    }
}