package com.divarclone.server.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    // token for userid
    private final Map<String, Integer> sessions = new ConcurrentHashMap<>();

    // if login be successful return token
    public String createSession(int userId) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, userId);
        return token;
    }

    // return the user id whit token in case of fail it will return null
    public Integer getUserId(String token) {
        return sessions.get(token);
    }

    // delete the token if user logout
    public void invalidate(String token) {
        sessions.remove(token);
    }
}