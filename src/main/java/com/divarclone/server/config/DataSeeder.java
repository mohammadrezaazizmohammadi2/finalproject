package com.divarclone.server.config;

import com.divarclone.server.model.User;
import com.divarclone.server.storage.UserStorage;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserStorage userStorage;

    public DataSeeder(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public void run(String... args) {
        List<User> existingUsers = userStorage.findAll();

        // for the first time it will make a manager
        if (existingUsers.isEmpty()) {
            String hashedPassword = BCrypt.hashpw("ali1385", BCrypt.gensalt());

            User manager = new User(
                    0, // id will save automatically
                    "Manager",
                    hashedPassword,
                    "09120000000",
                    "manager@example.com",
                    "ADMIN"
            );

            userStorage.save(manager);
            System.out.println("first manager login : Manager / ******");
        }

    }
}