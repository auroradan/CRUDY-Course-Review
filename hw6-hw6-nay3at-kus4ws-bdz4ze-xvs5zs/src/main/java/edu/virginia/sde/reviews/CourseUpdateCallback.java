package edu.virginia.sde.reviews;

//AI Agent: ChatGPT
//Prompt: how to make it so after database.commit() the loadCourses() function is called from CourseSearchController
public interface CourseUpdateCallback {
    void onCourseUpdated();
}
