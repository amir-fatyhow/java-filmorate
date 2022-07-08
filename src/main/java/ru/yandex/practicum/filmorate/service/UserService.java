package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.*;

@Service
public class UserService {
    private final UserStorage inMemoryUserStorage;
    public static final String INCORRECT_USER_ID = "Incorrect userId";
    public static final String INCORRECT_FRIEND_ID = "Incorrect friendId";

    @Autowired
    public UserService(UserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public void addFriend(Long friendId, Long userId) {
        if (friendId <= 0 || !inMemoryUserStorage.mapUsers().containsKey(friendId)) {
            throw new NullPointerException(INCORRECT_FRIEND_ID);
        }
       Map<Long,User> users = inMemoryUserStorage.mapUsers();

       for (Map.Entry<Long,User> entry : users.entrySet()) {
           if (Objects.equals(entry.getKey(), friendId)) {
               users.get(friendId).addFriend(userId);
           }
           if (Objects.equals(entry.getKey(), userId)) {
               users.get(userId).addFriend(friendId);
           }
       }
    }

    public List<User> getFriends(Long userId) {
        if (userId <= 0 || !inMemoryUserStorage.mapUsers().containsKey(userId)) {
            throw new NullPointerException(INCORRECT_USER_ID);
        }
        Map<Long,User> users = inMemoryUserStorage.mapUsers();

        for (Map.Entry<Long,User> entry : users.entrySet()) {
            if (Objects.equals(entry.getKey(), userId)) {
                List<User> friends = new ArrayList<>();
                for (Long id : users.get(userId).getFriends()) {
                    friends.add(users.get(id));
                }
               return friends;
            }
        }
        return Collections.emptyList();
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        Map<Long,User> users = inMemoryUserStorage.mapUsers();

        if (users.containsKey(userId) && users.containsKey(friendId)) {

            List<User> friends = new ArrayList<>();
            for (Long id : users.get(userId).getFriends()) {
                for (Long friendUserId : users.get(friendId).getFriends()) {
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
        if (friendId <= 0 || !inMemoryUserStorage.mapUsers().containsKey(friendId)) {
            throw new NullPointerException(INCORRECT_FRIEND_ID);
        }

        Map<Long,User> users = inMemoryUserStorage.mapUsers();

        for (Map.Entry<Long,User> entry : users.entrySet()) {
            if (Objects.equals(entry.getKey(), friendId)) {
                users.get(friendId).removeFriend(userId);
            }

            if (Objects.equals(entry.getKey(), userId)) {
                users.get(userId).removeFriend(friendId);
            }
        }
    }

    public UserStorage getInMemoryUserStorage() {
        return inMemoryUserStorage;
    }
}
