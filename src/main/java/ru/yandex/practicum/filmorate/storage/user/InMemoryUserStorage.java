package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    public static final String INCORRECT_USER_ID = "Incorrect userId";
    private final Map<Long,User> users = new HashMap<>();
    private long id = 1;

    public  Map<Long, User> mapUsers() {
        return users;
    }

    @Override
    public User getUserById(long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        }

        throw new NullPointerException(INCORRECT_USER_ID);
    }

    @Override
    public void removeUser(Long id) {
        users.remove(id);
    }

    @Override
    public User createUser(User user) {
        validation(user);
        user.setId(getNextId());
        users.put(user.getId(),user);
        return user;
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User putUser(User user) {
        validation(user);
        if (user.getId() <= 0) {
            throw new NullPointerException("Negative userId");
        }

        boolean flag = true;
        for (long userId : users.keySet()) {
            if (userId == user.getId()) {
                users.put(user.getId(), user);
                flag = false;
            }
        }

        if (flag) {
            log.info(INCORRECT_USER_ID);
            throw new ValidationException(INCORRECT_USER_ID);
        }
        return user;
    }

    private Long getNextId() {
        return id++;
    }

    private void validation(User user) {
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
    }
}
