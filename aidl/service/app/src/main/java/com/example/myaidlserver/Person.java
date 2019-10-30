package com.example.myaidlserver;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hi on 2019.10.30.
 */

public class Person implements Parcelable {
    private String name;
    private int age;
    private String IDCard;

    protected Person(Parcel in) {
        name=in.readString();
        age=in.readInt();
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
