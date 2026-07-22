package com.divarclone.server.model;

public class Rating {
    private int id;
    private int raterId;      // کسی که رأی داده
    private int ratedUserId;  // کسی که بهش رأی داده شده
    private int value;        // 1 تا 5

    public Rating() {
    }

    public Rating(int id, int raterId, int ratedUserId, int value) {
        this.id = id;
        this.raterId = raterId;
        this.ratedUserId = ratedUserId;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRaterId() {
        return raterId;
    }

    public void setRaterId(int raterId) {
        this.raterId = raterId;
    }

    public int getRatedUserId() {
        return ratedUserId;
    }

    public void setRatedUserId(int ratedUserId) {
        this.ratedUserId = ratedUserId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}