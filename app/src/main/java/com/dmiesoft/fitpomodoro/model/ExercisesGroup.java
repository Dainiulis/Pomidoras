package com.dmiesoft.fitpomodoro.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ExercisesGroup implements Parcelable{
    private long id;
    private String name;
    private String image;
    private List<Exercise> exercises;
    private long date;

    public ExercisesGroup(){}

    protected ExercisesGroup(Parcel in) {
        id = in.readLong();
        name = in.readString();
        image = in.readString();
        exercises = in.createTypedArrayList(Exercise.CREATOR);
        date = in.readLong();
    }

    public static final Creator<ExercisesGroup> CREATOR = new Creator<ExercisesGroup>() {
        @Override
        public ExercisesGroup createFromParcel(Parcel in) {
            return new ExercisesGroup(in);
        }

        @Override
        public ExercisesGroup[] newArray(int size) {
            return new ExercisesGroup[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(image);
        dest.writeTypedList(exercises);
        dest.writeLong(date);
    }
}
