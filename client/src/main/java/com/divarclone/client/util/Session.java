package com.divarclone.client.util;

public class Session {

    private static String token;
    private static String role;
    private static int userId;

    // سازنده‌ی private تا کسی نتونه از این کلاس آبجکت بسازه (فقط static استفاده میشه)
    private Session() {
    }

    public static void setSession(String newToken, String newRole, int newUserId) {
        token = newToken;
        role = newRole;
        userId = newUserId;
    }

    public static void clear() {
        token = null;
        role = null;
        userId = 0;
    }

    public static boolean isLoggedIn() {
        return token != null;
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public static String getToken() {
        return token;
    }

    public static String getRole() {
        return role;
    }

    public static int getUserId() {
        return userId;
    }
}