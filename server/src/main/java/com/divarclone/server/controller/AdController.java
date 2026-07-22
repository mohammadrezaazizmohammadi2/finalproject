package com.divarclone.server.controller;

import com.divarclone.server.auth.AuthHelper;
import com.divarclone.server.dto.AdRequest;
import com.divarclone.server.model.Ad;
import com.divarclone.server.model.User;
import com.divarclone.server.storage.AdStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ads")
public class AdController {

    private final AdStorage adStorage;
    private final AuthHelper authHelper;

    public AdController(AdStorage adStorage, AuthHelper authHelper) {
        this.adStorage = adStorage;
        this.authHelper = authHelper;
    }

    // list of ad login requires. always APPROVED only, for everyone
    @GetMapping
    public ResponseEntity<?> getAds(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sortBy
    ) {
        Optional<User> userOpt = authHelper.getUserFromHeader(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("pleas login first");
        }

        List<Ad> ads = adStorage.findAll().stream()
                .filter(a -> a.getStatus().equals("APPROVED"))
                .filter(a -> categoryId == null || a.getCategoryId() == categoryId)
                .filter(a -> city == null || a.getCity().equalsIgnoreCase(city))
                .filter(a -> minPrice == null || a.getPrice() >= minPrice)
                .filter(a -> maxPrice == null || a.getPrice() <= maxPrice)
                .collect(Collectors.toList());

        sortAds(ads, sortBy);

        return ResponseEntity.ok(ads);
    }

    // list of ads belonging to the logged-in user, any status
    @GetMapping("/my")
    public ResponseEntity<?> getMyAds(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sortBy
    ) {
        Optional<User> userOpt = authHelper.getUserFromHeader(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("pleas login first");
        }

        User user = userOpt.get();

        List<Ad> ads = adStorage.findAll().stream()
                .filter(a -> a.getOwnerId() == user.getId())
                .filter(a -> categoryId == null || a.getCategoryId() == categoryId)
                .filter(a -> city == null || a.getCity().equalsIgnoreCase(city))
                .filter(a -> minPrice == null || a.getPrice() >= minPrice)
                .filter(a -> maxPrice == null || a.getPrice() <= maxPrice)
                .collect(Collectors.toList());

        sortAds(ads, sortBy);

        return ResponseEntity.ok(ads);
    }

    // list of all ads, any status - admin only
    @GetMapping("/all")
    public ResponseEntity<?> getAllAds(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sortBy
    ) {
        Optional<User> userOpt = authHelper.getUserFromHeader(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("pleas login first");
        }

        User user = userOpt.get();
        if (!user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).body("only admin can see all ads");
        }

        List<Ad> ads = adStorage.findAll().stream()
                .filter(a -> categoryId == null || a.getCategoryId() == categoryId)
                .filter(a -> city == null || a.getCity().equalsIgnoreCase(city))
                .filter(a -> minPrice == null || a.getPrice() >= minPrice)
                .filter(a -> maxPrice == null || a.getPrice() <= maxPrice)
                .collect(Collectors.toList());

        sortAds(ads, sortBy);

        return ResponseEntity.ok(ads);
    }

    private void sortAds(List<Ad> ads, String sortBy) {
        if ("price_asc".equals(sortBy)) {
            ads.sort(Comparator.comparingDouble(Ad::getPrice));
        } else if ("price_desc".equals(sortBy)) {
            ads.sort(Comparator.comparingDouble(Ad::getPrice).reversed());
        }
    }


    // add one Ad
    // add one Ad
    @PostMapping
    public ResponseEntity<?> createAd(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AdRequest request
    ) {
        Optional<User> userOpt = authHelper.getUserFromHeader(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("pleas login first");
        }

        Ad ad = new Ad(
                0,
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getCity(),
                request.getCategoryId(),
                userOpt.get().getId(),
                "PENDING"
        );

        adStorage.save(ad);
        return ResponseEntity.ok(ad);
    }

    // delete the AD
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAd(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable int id
    ) {
        Optional<User> userOpt = authHelper.getUserFromHeader(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("pleas login first");
        }

        Optional<Ad> adOpt = adStorage.findById(id);
        if (adOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Ad was not found");
        }

        User user = userOpt.get();
        Ad ad = adOpt.get();

        boolean isOwner = ad.getOwnerId() == user.getId();
        boolean isAdmin = user.getRole().equals("ADMIN");

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).body("you can not delete this ad");
        }

        ad.setStatus("DELETED");
        adStorage.update(ad);

        return ResponseEntity.ok("Ad was removed successfully");
    }

    // remark the ad as sold
    @PostMapping("/{id}/mark-sold")
    public ResponseEntity<?> markAsSold(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable int id
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

        if (!ad.getStatus().equals("APPROVED")) {
            return ResponseEntity.badRequest().body("this ad is not approved");
        }

        ad.setStatus("SOLD");
        adStorage.update(ad);

        return ResponseEntity.ok("Ad is sold");
    }
}