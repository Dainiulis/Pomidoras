package com.dmiesoft.fitpomodoro.events;

import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;

public class DeleteObjects {

    private int id;
    private String className;

    public DeleteObjects(int id, String className) {
        this.id = id;
        this.className = className;
    }

    public int getId() {
        return id;
    }

    public String getClassName() {
        return className;
    }

}
