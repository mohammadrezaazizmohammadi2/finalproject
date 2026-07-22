package com.divarclone.server.controller;

import com.divarclone.server.auth.AuthHelper;
import com.divarclone.server.model.Rating;
import com.divarclone.server.model.User;
import com.divarclone.server.storage.RatingStorage;
import com.divarclone.server.storage.UserStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class RatingController {

    private final RatingStorage ratingStorage;
    private final UserStorage userStorage;
    private final AuthHelper authHelper;

    public RatingController(RatingStorage ratingStorage, UserStorage userStorage, AuthHelper authHelper) {
        this.ratingStorage = ratingStorage;
        this.userStorage = userStorage;
        this.authHelper = authHelper;
    }

    // اطلاعات عمومی یک کاربر (فقط id و username، بدون اطلاعات حساس)
    @GetMapping("/{id}/public")
    public ResponseEntity<?> getPublicUser(@PathVariable int id) {

        Optional<User> userOpt = userStorage.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("user not found");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", userOpt.get().getId());
        result.put("username", userOpt.get().getUsername());

        return ResponseEntity.ok(result);
    }

    // میانگین امتیاز و تعداد رأی‌های یک کاربر
    @GetMapping("/{id}/rating-summary")
    public ResponseEntity<?> getRatingSummary(@PathVariable int id) {

        List<Rating> ratings = ratingStorage.findByRatedUser(id);

        double average = ratings.stream()
                .mapToInt(Rating::getValue)
                .average()
                .orElse(0.0);

        Map<String, Object> result = new HashMap<>();
        result.put("average", average);
        result.put("count", ratings.size());

        return ResponseEntity.ok(result);
    }

    // رأی قبلی کاربر لاگین‌شده به این فرد (اگه داده باشه)
    @GetMapping("/{id}/my-rating")
    public ResponseEntity<?> getMyRating(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable int id
    ) {
        Optional<User> userOpt = authHelper.getUserFromHeader(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("pleas login first");
        }

        Optional<Rating> ratingOpt = ratingStorage.findByRaterAndRated(userOpt.get().getId(), id);

        Map<String, Object> result = new HashMap<>();
        result.put("value", ratingOpt.map(Rating::getValue).orElse(0));

        return ResponseEntity.ok(result);
    }

    // ثبت یا آپدیت رأی
    @PostMapping("/{id}/rate")
    public ResponseEntity<?> rateUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable int id,
            @RequestBody Map<String, Integer> body
    ) {
        Optional<User> userOpt = authHelper.getUserFromHeader(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("pleas login first");
        }

        User rater = userOpt.get();

        if (rater.getId() == id) {
            return ResponseEntity.badRequest().body("you cannot rate yourself");
        }

        Integer value = body.get("value");
        if (value == null || value < 1 || value > 5) {
            return ResponseEntity.badRequest().body("value must be between 1 and 5");
        }

        Optional<Rating> existing = ratingStorage.findByRaterAndRated(rater.getId(), id);

        if (existing.isPresent()) {
            Rating rating = existing.get();
            rating.setValue(value);
            ratingStorage.update(rating);
        } else {
            Rating rating = new Rating(0, rater.getId(), id, value);
            ratingStorage.save(rating);
        }

        return ResponseEntity.ok("rating saved");
    }
}