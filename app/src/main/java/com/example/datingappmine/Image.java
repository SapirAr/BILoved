package com.example.datingappmine;

import com.google.firebase.database.Exclude;

public class Image {
    private String imgUrl;
    private String key;

    public Image() {
    }

    public Image(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}