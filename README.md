[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-24ddc0f5d75046c5622901739e7c5dd533143b0c8e959d652212380cedb1ea36.svg)](https://classroom.github.com/a/DC1SF4uZ)
# Homework 6 - CRUDdy Course Reviews

## Authors
1) Dan Tran
2) Ronit Reddy
3) David Truong
4) Kat Fong

## To Run

1. Provide the following VM arguments within IntelliJ to run the CRUDdy Course Reviews application:
    * **--module-path [Path to JavaFX folder] --add-modules javafx.controls,javafx.fxml**
2. Run the CourseReviewsApplication.java file to open and use the CRUDdy Course Reviews application.
3. To view a course, click on the course in the table and click the "View Reviews" button
4. To search a course, type in desired filters in fields and click "Search" button
5. To make all courses appear again after filters, clear all the fields and hit the "Search" button again
6. To add a course, click the "Add Course" button and fill in desired fields
7. To view all reviews created by your user, click the "My Profile" dropdown and then click "My Reviews"
8. To logout of application, click the "My Profile" dropdown and then click "Logout"

## Contributions

List the primary contributions of each author. It is recommended to update this with your contributions after each coding session.

### [Dan Tran]

* Created CourseReviewsApplication and added implementation and style to LoginScreenController
* Created CourseSearchScreen and added implementation and style to CourseSearchScreen to make courses show
* Created ReviewCourse page and added implementation and style to ReviewCourseController to make reviews show
* Created a lot of sqllite methods used in CourseSearchController, LoginScreenController, and ReviewCourseController


### [Ronit Reddy]

* Added the course search functionality in the Course Search Screen.
* Implemented Add Review, Edit Review, and Delete Review features in the Course Reviews Screen.
* Developed the My Reviews Screen to show a list of all the reviews written by the logged in user, and implemented the View Course Reviews feature.

### [David Truong]

* Created the database tables, ensured they were in 2nd Normal Form, and set up foreign keys to the Courses and Users tables for the Reviews table
* Created the Review class, incorporating it into the Courses class and table
* System testing and populating database

### [Kat Fong]

* Implemented the My Reviews Screen, Controller and design
* Created the Login Screen create new user button and worked on Login Screen style
* Added automatic closing for the popups (create new user, add course, add review)
* System Testing and populating database

## Issues

N/A
