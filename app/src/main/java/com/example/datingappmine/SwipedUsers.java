package com.example.datingappmine;

import android.annotation.SuppressLint;

import com.google.android.material.internal.ParcelableSparseArray;

@SuppressLint("RestrictedApi")
public class SwipedUsers extends ParcelableSparseArray {
    private Boolean isSwipedRight;
    private String UID;

    public SwipedUsers() {
    }

    public SwipedUsers(String UID, Boolean isSwipedRight) {
        this.isSwipedRight = isSwipedRight;
        this.UID = UID;
    }

    public Boolean getSwipedRight() {
        return isSwipedRight;
    }

    public void setSwipedRight(Boolean swipedRight) {
        isSwipedRight = swipedRight;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}