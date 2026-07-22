package com.divarclone.server.controller;

import com.divarclone.server.auth.AuthHelper;
import com.divarclone.server.dto.CategoryRequest;
import com.divarclone.server.model.Category;
import com.divarclone.server.model.User;
import com.divarclone.server.storage.AdStorage;
import com.divarclone.server.storage.CategoryStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryStorage categoryStorage;
    private final AuthHelper authHelper;
    private final AdStorage adStorage;
    public CategoryController(CategoryStorage categoryStorage, AuthHelper authHelper, AdStorage adStorage) {

        this.categoryStorage = categoryStorage;
        this.authHelper = authHelper;
        this.adStorage = adStorage;
    }
    // ads list
    @GetMapping
    public ResponseEntity<?> getCategories(@RequestHeader("Authorization") String authHeader) {
        Optional<User> userOpt = authHelper.getUserFromHeader(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("pleas login first");
        }

        List<Category> categories = categoryStorage.findAll();
        return ResponseEntity.ok(categories);
    }

    // add new category
    @PostMapping
    public ResponseEntity<?> createCategory(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CategoryRequest request
    ) {
        Optional<User> userOpt = authHelper.getUserFromHeader(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("pleas login first");
        }

        if (!userOpt.get().getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).body("only manager can add category");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().body("name is required");
        }

        Category category = new Category(0, request.getName());
        categoryStorage.save(category);

        return ResponseEntity.ok("category created");
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable int id) {

        Optional<User> userOpt = authHelper.getUserFromHeader(authHeader);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("please login first");
        }

        if (!userOpt.get().getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).body("only manager can delete category");
        }
        if (adStorage.hasCategory(id)) {
            return ResponseEntity.badRequest()
                    .body("This category contains ads and cannot be deleted.");
        }
        boolean deleted = categoryStorage.delete(id);

        if (!deleted) {
            return ResponseEntity.status(404).body("category not found");
        }

        return ResponseEntity.ok("category deleted");
    }
}