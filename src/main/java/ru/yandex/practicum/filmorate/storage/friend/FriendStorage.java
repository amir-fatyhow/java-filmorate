package ru.yandex.practicum.filmorate.storage.friend;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;

public interface FriendStorage {
    void addFriend(Long friendId, Long userId);

    List<User> getFriends(Long userId);

    void confirmFriend(Long userId, Long notConfirmFriendId);

    List<User> getCommonFriends(Long userId, Long friendId);

    void removeFriend(Long friendId, Long userId);
}
