package com.divarclone.server.storage;

import com.divarclone.server.model.Ad;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class AdStorage {

    private final File file = new File("data/ads.json");
    private final ObjectMapper mapper = new ObjectMapper();

    // read all ads
    public synchronized List<Ad> findAll() {
        try {
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }
            return mapper.readValue(file, new TypeReference<List<Ad>>() {});
        } catch (IOException e) {
            throw new RuntimeException("warning in reading ads.json", e);
        }
    }

    // نوشتن کل لیست روی فایل
    private synchronized void saveAll(List<Ad> ads) {
        try {
            file.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, ads);
        } catch (IOException e) {
            throw new RuntimeException("warning in writing ads.json", e);
        }
    }

    public synchronized Optional<Ad> findById(int id) {
        return findAll().stream()
                .filter(a -> a.getId() == id)
                .findFirst();
    }

    // id will save automatically
    public synchronized Ad save(Ad ad) {
        List<Ad> ads = findAll();
        int newId = ads.stream().mapToInt(Ad::getId).max().orElse(0) + 1;
        ad.setId(newId);
        ads.add(ad);
        saveAll(ads);
        return ad;
    }

    //update ad status
    public synchronized boolean update(Ad updatedAd) {
        List<Ad> ads = findAll();
        for (int i = 0; i < ads.size(); i++) {
            if (ads.get(i).getId() == updatedAd.getId()) {
                ads.set(i, updatedAd);
                saveAll(ads);
                return true;
            }
        }
        return false; // there is no ad with this id
    }
    public synchronized boolean hasCategory(int categoryId) {
        return findAll().stream()
                .anyMatch(ad -> ad.getCategoryId() == categoryId);
    }
}