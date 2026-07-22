package com.divarclone.server.auth;

import com.divarclone.server.model.User;
import com.divarclone.server.storage.UserStorage;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthHelper {

    private final SessionManager sessionManager;
    private final UserStorage userStorage;

    public AuthHelper(SessionManager sessionManager, UserStorage userStorage) {
        this.sessionManager = sessionManager;
        this.userStorage = userStorage;
    }

    public Optional<User> getUserFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = authHeader.substring("Bearer ".length());
        Integer userId = sessionManager.getUserId(token);

        if (userId == null) {
            return Optional.empty();
        }

        return userStorage.findById(userId);
    }
}