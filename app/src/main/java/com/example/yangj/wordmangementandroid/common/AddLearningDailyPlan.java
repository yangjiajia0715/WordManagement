package com.example.yangj.wordmangementandroid.common;

import java.util.List;

public class AddLearningDailyPlan {
    private int courseId;

    private List<Integer> words;

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public List<Integer> getWords() {
        return words;
    }

    public void setWords(List<Integer> words) {
        this.words = words;
    }
}
