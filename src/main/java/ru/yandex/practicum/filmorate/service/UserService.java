package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.*;

@Service
public class UserService {
    private final InMemoryUserStorage userStorage;
    public static final String INCORRECT_USER_ID = "Incorrect userId";
    public static final String INCORRECT_FRIEND_ID = "Incorrect friendId";

    @Autowired
    public UserService(@Qualifier("inMemoryUserStorage") InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long friendId, Long userId) {
        if (friendId <= 0 || !userStorage.mapUsers().containsKey(friendId)) {
            throw new NullPointerException(INCORRECT_FRIEND_ID);
        }
       Map<Long,User> users = userStorage.mapUsers();

       for (Map.Entry<Long,User> entry : users.entrySet()) {
           if (Objects.equals(entry.getKey(), friendId)) {
               users.get(friendId).getFriends().put(userId,true);
           }
           if (Objects.equals(entry.getKey(), userId)) {
               users.get(friendId).getFriends().put(userId,true);
           }
       }
    }

    public List<User> getFriends(Long userId) {
        if (userId <= 0 || !userStorage.mapUsers().containsKey(userId)) {
            throw new NullPointerException(INCORRECT_USER_ID);
        }
        Map<Long,User> users = userStorage.mapUsers();

        for (Map.Entry<Long,User> entry : users.entrySet()) {
            if (Objects.equals(entry.getKey(), userId)) {
                List<User> friends = new ArrayList<>();
                for (Long id : users.get(userId).getFriends().keySet()) {
                    friends.add(users.get(id));
                }
               return friends;
            }
        }
        return Collections.emptyList();
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        Map<Long,User> users = userStorage.mapUsers();

        if (users.containsKey(userId) && users.containsKey(friendId)) {

            List<User> friends = new ArrayList<>();
            for (Long id : users.get(userId).getFriends().keySet()) {
                for (Long friendUserId : users.get(friendId).getFriends().keySet()) {
                    if (Objects.equals(id, friendUserId)) {
                        friends.add(users.get(id));
                    }
                }
            }
            return friends;
        }
        return Collections.emptyList();
    }

    public void removeFriend(Long friendId, Long userId) {
        if (friendId <= 0 || !userStorage.mapUsers().containsKey(friendId)) {
            throw new NullPointerException(INCORRECT_FRIEND_ID);
        }

        Map<Long,User> users = userStorage.mapUsers();

        for (Map.Entry<Long,User> entry : users.entrySet()) {
            if (Objects.equals(entry.getKey(), friendId)) {
                users.get(friendId).removeFriend(userId);
            }

            if (Objects.equals(entry.getKey(), userId)) {
                users.get(userId).removeFriend(friendId);
            }
        }
    }

    public Collection<User> getAllUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(long id) {
        return userStorage.getUserById(id);
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.putUser(user);
    }
}
