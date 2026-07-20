package com.divarclone.server.storage;

import com.divarclone.server.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserStorage {

    private final File file = new File("data/users.json");
    private final ObjectMapper mapper = new ObjectMapper();

    // read all users from file
    public synchronized List<User> findAll() {
        try {
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }
            return mapper.readValue(file, new TypeReference<List<User>>() {});
        } catch (IOException e) {
            throw new RuntimeException("warning in reading users.json", e);
        }
    }
    private synchronized void saveAll(List<User> users) {
        try {
            file.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, users);
        } catch (IOException e) {
            throw new RuntimeException("warning in writing users.json", e);
        }
    }

    public synchronized Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public synchronized Optional<User> findById(int id) {
        return findAll().stream()
                .filter(u -> u.getId() == id)
                .findFirst();
    }

    // id will save automatically
    public synchronized User save(User user) {
        List<User> users = findAll();
        int newId = users.stream().mapToInt(User::getId).max().orElse(0) + 1;
        user.setId(newId);
        users.add(user);
        saveAll(users);
        return user;
    }

    // make a user an admin
    public synchronized boolean updateRole(int id, String newRole) {
        List<User> users = findAll();
        for (User u : users) {
            if (u.getId() == id) {
                u.setRole(newRole);
                saveAll(users);
                return true;
            }
        }
        return false; // there is no user with this id
    }

    // remove a user from file
    public synchronized boolean delete(int id) {
        List<User> users = findAll();
        boolean removed = users.removeIf(u -> u.getId() == id);
        if (removed) {
            saveAll(users);
        }
        return removed;
    }
}