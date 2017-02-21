package com.dmiesoft.fitpomodoro.model;


import android.os.Parcel;
import android.os.Parcelable;

public class Exercise implements Parcelable{
    private long id;
    private String name;
    private String type;
    private String description;
    private long exerciseGroupId;
    private String image;
    private long date;
    private boolean isChecked;

    public Exercise() {}

    public Exercise(Parcel in) {
        id = in.readLong();
        name = in.readString();
        type = in.readString();
        description = in.readString();
        exerciseGroupId = in.readLong();
        image = in.readString();
        date = in.readLong();
    }

    public static final Creator<Exercise> CREATOR = new Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getExerciseGroupId() {
        return exerciseGroupId;
    }

    public void setExerciseGroupId(long exerciseGroupId) {
        this.exerciseGroupId = exerciseGroupId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(description);
        dest.writeLong(exerciseGroupId);
        dest.writeString(image);
        dest.writeLong(date);
    }
}
