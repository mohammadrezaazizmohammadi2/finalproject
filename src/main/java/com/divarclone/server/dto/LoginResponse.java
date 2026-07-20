package com.divarclone.server.dto;

public class LoginResponse {
    private String token;
    private String role;
    private int userId;

    public LoginResponse(String token, String role, int userId) {
        this.token = token;
        this.role = role;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public int getUserId() {
        return userId;
    }
}