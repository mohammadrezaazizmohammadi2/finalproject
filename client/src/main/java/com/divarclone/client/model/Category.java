package com.divarclone.client.model;

public class Category {
    private int id;
    private String name;

    // سازنده‌ی خالی — برای تبدیل خودکار JSON به آبجکت (Jackson) لازم است
    public Category() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}