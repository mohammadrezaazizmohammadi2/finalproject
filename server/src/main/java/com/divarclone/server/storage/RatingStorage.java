package com.divarclone.server.storage;

import com.divarclone.server.model.Rating;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class RatingStorage {

    private final File file = new File("data/ratings.json");
    private final ObjectMapper mapper = new ObjectMapper();

    public synchronized List<Rating> findAll() {
        try {
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }
            return mapper.readValue(file, new TypeReference<List<Rating>>() {});
        } catch (IOException e) {
            throw new RuntimeException("warning in reading ratings.json", e);
        }
    }

    private synchronized void saveAll(List<Rating> ratings) {
        try {
            file.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, ratings);
        } catch (IOException e) {
            throw new RuntimeException("warning in writing ratings.json", e);
        }
    }

    public synchronized Optional<Rating> findByRaterAndRated(int raterId, int ratedUserId) {
        return findAll().stream()
                .filter(r -> r.getRaterId() == raterId && r.getRatedUserId() == ratedUserId)
                .findFirst();
    }

    public synchronized List<Rating> findByRatedUser(int ratedUserId) {
        return findAll().stream()
                .filter(r -> r.getRatedUserId() == ratedUserId)
                .toList();
    }

    public synchronized Rating save(Rating rating) {
        List<Rating> ratings = findAll();
        int newId = ratings.stream().mapToInt(Rating::getId).max().orElse(0) + 1;
        rating.setId(newId);
        ratings.add(rating);
        saveAll(ratings);
        return rating;
    }
//updating rating status

    public synchronized boolean update(Rating updatedRating) {
        List<Rating> ratings = findAll();
        for (int i = 0; i < ratings.size(); i++) {
            if (ratings.get(i).getId() == updatedRating.getId()) {
                ratings.set(i, updatedRating);
                saveAll(ratings);
                return true;
            }
        }
        return false;
    }
}