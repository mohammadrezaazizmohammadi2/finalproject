package com.divarclone.server.storage;

import com.divarclone.server.model.Category;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CategoryStorage {

    private final File file = new File("data/categories.json");
    private final ObjectMapper mapper = new ObjectMapper();

    // read all from file
    public synchronized List<Category> findAll() {
        try {
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }
            return mapper.readValue(file, new TypeReference<List<Category>>() {});
        } catch (IOException e) {
            throw new RuntimeException("warning in reading categories.json", e);
        }
    }

    // write all of them
    private synchronized void saveAll(List<Category> categories) {
        try {
            file.getParentFile().mkdirs(); // اگه پوشه‌ی data نبود بسازش
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, categories);
        } catch (IOException e) {
            throw new RuntimeException("warning in writing categories.json", e);
        }
    }



    // id will save automatically
    public synchronized Category save(Category category) {
        List<Category> categories = findAll();
        int newId = categories.stream().mapToInt(Category::getId).max().orElse(0) + 1;
        category.setId(newId);
        categories.add(category);
        saveAll(categories);
        return category;
    }
    public synchronized boolean delete(int id) {

        List<Category> categories = findAll();

        boolean removed = categories.removeIf(c -> c.getId() == id);

        if (removed) {
            saveAll(categories);
        }

        return removed;
    }
}