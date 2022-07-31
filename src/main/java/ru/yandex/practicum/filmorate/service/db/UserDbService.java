package ru.yandex.practicum.filmorate.service.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.db.FriendDbStorage;
import ru.yandex.practicum.filmorate.storage.user.db.UserDbStorage;
import java.util.*;

import static ru.yandex.practicum.filmorate.storage.user.db.UserDbStorage.INCORRECT_USER_ID;

@Service
public class UserDbService {
    private final UserDbStorage userDbStorage;
    private final FriendDbStorage friendDbStorage;
    private final JdbcTemplate jdbcTemplate;

    public static final String INCORRECT_FRIEND_ID = "Incorrect friendId";

    @Autowired
    public UserDbService(@Qualifier("UserDbStorage") UserDbStorage userDbStorage, JdbcTemplate jdbcTemplate
            , @Qualifier("FriendDbStorage") FriendDbStorage friendDbStorage) {
        this.userDbStorage = userDbStorage;
        this.friendDbStorage = friendDbStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addFriend(Long friendId, Long userId) {
        if (friendId <= 0) {
            throw new IllegalArgumentException(INCORRECT_FRIEND_ID);
        }
        friendDbStorage.addFriend(friendId, userId);
    }

    public List<User> getFriends(Long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException(INCORRECT_USER_ID);
        }
        return friendDbStorage.getFriends(userId);
    }

    public void confirmFriend(Long userId, Long notConfirmFriendId) {
        friendDbStorage.confirmFriend(userId, notConfirmFriendId);
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        return friendDbStorage.getCommonFriends(userId, friendId);
    }

    public void removeFriend(Long friendId, Long userId) {
        friendDbStorage.removeFriend(friendId, userId);
    }

    public Collection<User> getAllUsers() {
        return userDbStorage.getUsers();
    }

    public User getUserById(long id) {
        return userDbStorage.getUserById(id);
    }

    public User createUser(User user) {
        return userDbStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userDbStorage.putUser(user);
    }

    public void removeUser(Long id) {
        userDbStorage.removeUser(id);
    }
}
