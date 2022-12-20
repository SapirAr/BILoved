package com.example.datingappmine;

import android.annotation.SuppressLint;
import com.google.android.material.internal.ParcelableSparseArray;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("RestrictedApi")
public class Person extends ParcelableSparseArray implements Serializable {
    private String UID;
    private String name;
    private int age;
    private String city;
    private String description;
    private String gender;
    private final List<Image> imageList = new ArrayList<>();

    public Person() {
    }

    public Person(String UID, String name, int age, String city, String description, String gender) {
        this.UID = UID;
        this.name = name;
        this.age = age;
        this.city = city;
        this.description = description;
        this.gender = gender;
    }

    public List<Image> getImageList() {
        return imageList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}