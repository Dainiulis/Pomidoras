package com.dmiesoft.fitpomodoro.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ExerciseHistory implements Parcelable{
    private long id;
    private String name;
    private int howMany;
    private long date;

    public ExerciseHistory() {}

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

    public int getHowMany() {
        return howMany;
    }

    public void setHowMany(int howMany) {
        this.howMany = howMany;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public ExerciseHistory(Parcel in) {
        id = in.readLong();
        name = in.readString();
        howMany = in.readInt();
        date = in.readLong();
    }

    public static final Creator<ExerciseHistory> CREATOR = new Creator<ExerciseHistory>() {
        @Override
        public ExerciseHistory createFromParcel(Parcel in) {
            return new ExerciseHistory(in);
        }

        @Override
        public ExerciseHistory[] newArray(int size) {
            return new ExerciseHistory[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeInt(howMany);
        dest.writeLong(date);
    }
}
