package com.divarclone.server.controller;

import com.divarclone.server.auth.AuthHelper;
import com.divarclone.server.model.Ad;
import com.divarclone.server.model.User;
import com.divarclone.server.storage.AdStorage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@RestController
public class ImageController {

    private static final String IMAGES_DIR = "images";

    private final AdStorage adStorage;
    private final AuthHelper authHelper;

    public ImageController(AdStorage adStorage, AuthHelper authHelper) {
        this.adStorage = adStorage;
        this.authHelper = authHelper;
    }

    // آپلود عکس برای یک آگهی مشخص - فقط توسط مالک آگهی
    @PostMapping("/ads/{id}/image")
    public ResponseEntity<?> uploadImage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable int id,
            @RequestParam("file") MultipartFile file
    ) {
        Optional<User> userOpt = authHelper.getUserFromHeader(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("pleas login first");
        }

        Optional<Ad> adOpt = adStorage.findById(id);
        if (adOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Ad was not found");
        }

        Ad ad = adOpt.get();

        if (ad.getOwnerId() != userOpt.get().getId()) {
            return ResponseEntity.status(403).body("you are not the owner of this ad");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("file is empty");
        }

        try {
            Files.createDirectories(Paths.get(IMAGES_DIR));

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = "ad_" + id + "_" + UUID.randomUUID() + extension;
            Path targetPath = Paths.get(IMAGES_DIR, fileName);

            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath);
            }

            ad.setImageFileName(fileName);
            adStorage.update(ad);

            return ResponseEntity.ok(ad);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("could not save image: " + e.getMessage());
        }
    }

    // دریافت فایل عکس با اسمش - عمومی، نیاز به لاگین ندارد
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {

        Path filePath = Paths.get(IMAGES_DIR, filename);
        File file = filePath.toFile();

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
}