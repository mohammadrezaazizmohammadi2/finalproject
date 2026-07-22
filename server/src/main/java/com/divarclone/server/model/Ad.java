package com.divarclone.server.model;

public class Ad {
    private int id;
    private String title;
    private String description;
    private double price;
    private String city;
    private int categoryId;
    private int ownerId;
    private String status; //  PENDING, APPROVED, REJECTED, DELETED, SOLD
    private String imageFileName; // اسم فایل عکس روی سرور، ممکنه null باشه

    public Ad() {
    }

    public Ad(int id, String title, String description, double price,
              String city, int categoryId, int ownerId, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.city = city;
        this.categoryId = categoryId;
        this.ownerId = ownerId;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }
}