package com.divarclone.server.controller;

import com.divarclone.server.auth.SessionManager;
import com.divarclone.server.dto.LoginRequest;
import com.divarclone.server.dto.LoginResponse;
import com.divarclone.server.dto.RegisterRequest;
import com.divarclone.server.model.User;
import com.divarclone.server.storage.UserStorage;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class AuthController {

    private final UserStorage userStorage;
    private final SessionManager sessionManager;

    public AuthController(UserStorage userStorage, SessionManager sessionManager) {
        this.userStorage = userStorage;
        this.sessionManager = sessionManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (isBlank(request.getUsername()) || isBlank(request.getPassword())
                || isBlank(request.getPhone()) || isBlank(request.getEmail())) {
            return ResponseEntity.badRequest().body("user name or email address is invalid");
        }

        if (userStorage.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("username already exists");
        }

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());

        User newUser = new User(
                0, // id will save automatically
                request.getUsername(),
                hashedPassword,
                request.getPhone(),
                request.getEmail(),
                "USER"
        );

        userStorage.save(newUser);

        return ResponseEntity.ok("login successful");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userStorage.findByUsername(request.getUsername());

        if (userOpt.isEmpty() || !BCrypt.checkpw(request.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(401).body("password or username is invalid");
        }

        User user = userOpt.get();
        String token = sessionManager.createSession(user.getId());

        return ResponseEntity.ok(new LoginResponse(token, user.getRole(), user.getId()));
    }

    // it will check to string dont be null
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}