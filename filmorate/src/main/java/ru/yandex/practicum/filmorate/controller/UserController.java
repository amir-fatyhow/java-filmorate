package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private Map<Long,User> users = new HashMap<>();
    private long id = 1;

    @GetMapping
    public Collection<User> allUsers() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail().isEmpty()) {
            log.info("Электронная почта пустая.");
            throw new ValidationException("Электронная почта не может быть пустой.");
        }
        if (!user.getEmail().contains("@")) {
            log.info("Электронная почта не содержит символ @");
            throw new ValidationException("Электронная почта должна содержать символ @");
        }
        if (user.getLogin().isBlank()) {
            log.info("Логин не может быть пустым.");
            throw new ValidationException("Логин не может быть пустым.");
        }
        if (user.getLogin().contains(" ")) {
            log.info("Логин содержит пробел.");
            throw new ValidationException("Логин не может содержать пробелы.");
        }
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())){
            log.info("Дата рождения в будущем.");
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }

        user.setId(id);
        id++;
        users.put(user.getId(),user);
        return user;
    }

    @PutMapping
    public User put(@RequestBody User user) {
        if (user.getEmail().isEmpty()) {
            log.info("Электронная почта пустая.");
            throw new ValidationException("Электронная почта не может быть пустой.");
        }
        if (!user.getEmail().contains("@")) {
            log.info("Электронная почта не содержит символ @");
            throw new ValidationException("Электронная почта должна содержать символ @");
        }
        if (user.getLogin().isBlank()) {
            log.info("Логин не может быть пустым.");
            throw new ValidationException("Логин не может быть пустым.");
        }
        if (user.getLogin().contains(" ")) {
            log.info("Логин содержит пробел.");
            throw new ValidationException("Логин не может содержать пробелы.");
        }
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())){
            log.info("Дата рождения в будущем.");
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }

        boolean flag = true;
        for (long l : users.keySet()) {
            if (l == user.getId()) {
                users.put(user.getId(), user);
                flag = false;
            }
        }

        if (flag) {
            log.info("неверный id");
            throw new ValidationException("неверный id");
        }

        return user;
    }
}
