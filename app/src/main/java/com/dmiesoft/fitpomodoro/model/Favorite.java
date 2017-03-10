package com.dmiesoft.fitpomodoro.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Dainius on 2017-03-10.
 */

public class Favorite implements Parcelable {
    private long id;
    private String name;
    private long date;

    public Favorite() {
    }

    protected Favorite(Parcel in) {
        id = in.readLong();
        name = in.readString();
        date = in.readLong();
    }

    public static final Creator<Favorite> CREATOR = new Creator<Favorite>() {
        @Override
        public Favorite createFromParcel(Parcel in) {
            return new Favorite(in);
        }

        @Override
        public Favorite[] newArray(int size) {
            return new Favorite[size];
        }
    };

    @Override
    public String toString() {
        return name;
    }

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
        dest.writeLong(date);
    }
}
