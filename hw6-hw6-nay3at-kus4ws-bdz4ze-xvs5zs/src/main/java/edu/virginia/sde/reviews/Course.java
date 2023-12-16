package edu.virginia.sde.reviews;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Course {

    private final SimpleStringProperty subject;
    private final SimpleIntegerProperty number;
    private final SimpleStringProperty title;
    private SimpleStringProperty rating;
    private final SimpleStringProperty id;

    public Course(String subject, int number, String title) {
        this.subject = new SimpleStringProperty(subject);
        this.number = new SimpleIntegerProperty(number);
        this.title = new SimpleStringProperty(title);
        this.rating = new SimpleStringProperty("");
        this.id = new SimpleStringProperty(subject + " " + number + " " + title);
    }

    public Course(String subject, int number, String title, String rating) {
        this.subject = new SimpleStringProperty(subject);
        this.number = new SimpleIntegerProperty(number);
        this.title = new SimpleStringProperty(title);
        this.rating = new SimpleStringProperty(rating);
        this.id = new SimpleStringProperty(subject + " " + number + " " + title);
    }

    public String getID() { return id.get(); }

    public String getSubject() {
        return subject.get();
    }

    public int getNumber() {
        return number.get();
    }

    public String getTitle() {
        return title.get();
    }

    public String getRating() { return rating.get(); }

    public void setRating(String rating) {
        this.rating = new SimpleStringProperty("" + rating);
    }
}
