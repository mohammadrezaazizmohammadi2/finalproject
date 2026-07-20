package com.divarclone.server.controller;

import com.divarclone.server.auth.AuthHelper;
import com.divarclone.server.model.Ad;
import com.divarclone.server.model.User;
import com.divarclone.server.storage.AdStorage;
import com.divarclone.server.storage.UserStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdStorage adStorage;
    private final UserStorage userStorage;
    private final AuthHelper authHelper;

    public AdminController(AdStorage adStorage, UserStorage userStorage, AuthHelper authHelper) {
        this.adStorage = adStorage;
        this.userStorage = userStorage;
        this.authHelper = authHelper;
    }


    private ResponseEntity<?> requireAdmin(String authHeader, User[] outUser) {
        Optional<User> userOpt = authHelper.getUserFromHeader(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("pleas login first");
        }
        if (!userOpt.get().getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).body("only manager can access");
        }
        outUser[0] = userOpt.get();
        return null;
    }

    // ad manager

    @GetMapping("/ads/pending")
    public ResponseEntity<?> getPendingAds(@RequestHeader("Authorization") String authHeader) {
        User[] adminHolder = new User[1];
        ResponseEntity<?> error = requireAdmin(authHeader, adminHolder);
        if (error != null) return error;

        List<Ad> pending = adStorage.findAll().stream()
                .filter(a -> a.getStatus().equals("PENDING"))
                .collect(Collectors.toList());

        return ResponseEntity.ok(pending);
    }

    @PostMapping("/ads/{id}/approve")
    public ResponseEntity<?> approveAd(@RequestHeader("Authorization") String authHeader, @PathVariable int id) {
        User[] adminHolder = new User[1];
        ResponseEntity<?> error = requireAdmin(authHeader, adminHolder);
        if (error != null) return error;

        Optional<Ad> adOpt = adStorage.findById(id);
        if (adOpt.isEmpty()) return ResponseEntity.status(404).body("ad not found");

        Ad ad = adOpt.get();
        ad.setStatus("APPROVED");
        adStorage.update(ad);

        return ResponseEntity.ok("ad approved");
    }

    @PostMapping("/ads/{id}/reject")
    public ResponseEntity<?> rejectAd(@RequestHeader("Authorization") String authHeader, @PathVariable int id) {
        User[] adminHolder = new User[1];
        ResponseEntity<?> error = requireAdmin(authHeader, adminHolder);
        if (error != null) return error;

        Optional<Ad> adOpt = adStorage.findById(id);
        if (adOpt.isEmpty()) return ResponseEntity.status(404).body("ad not found");

        Ad ad = adOpt.get();
        ad.setStatus("REJECTED");
        adStorage.update(ad);

        return ResponseEntity.ok("ad was rejected");
    }

    // user management

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        User[] adminHolder = new User[1];
        ResponseEntity<?> error = requireAdmin(authHeader, adminHolder);
        if (error != null) return error;

        return ResponseEntity.ok(userStorage.findAll());
    }

    @PostMapping("/users/{id}/promote")
    public ResponseEntity<?> promoteUser(@RequestHeader("Authorization") String authHeader, @PathVariable int id) {
        User[] adminHolder = new User[1];
        ResponseEntity<?> error = requireAdmin(authHeader, adminHolder);
        if (error != null) return error;

        Optional<User> targetOpt = userStorage.findById(id);
        if (targetOpt.isEmpty()) return ResponseEntity.status(404).body("user not found");

        userStorage.updateRole(id, "ADMIN");
        return ResponseEntity.ok("user promoted");
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String authHeader, @PathVariable int id) {
        User[] adminHolder = new User[1];
        ResponseEntity<?> error = requireAdmin(authHeader, adminHolder);
        if (error != null) return error;

        User admin = adminHolder[0];

        if (admin.getId() == id) {
            return ResponseEntity.badRequest().body("you can't delete this user");
        }

        Optional<User> targetOpt = userStorage.findById(id);
        if (targetOpt.isEmpty()) return ResponseEntity.status(404).body("user not found");

        userStorage.delete(id);
        return ResponseEntity.ok("user deleted");
    }
}