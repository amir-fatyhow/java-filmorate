package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;

public interface UserStorage {
    User createUser(User user);

    Collection<User> getUsers();

    User putUser(User user);

    Map<Long,User> mapUsers();

    User getUser(long id);
}
