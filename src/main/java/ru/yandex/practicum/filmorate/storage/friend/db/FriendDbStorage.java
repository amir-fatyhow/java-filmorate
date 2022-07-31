package ru.yandex.practicum.filmorate.storage.friend.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;

import java.util.*;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static ru.yandex.practicum.filmorate.service.db.UserDbService.INCORRECT_FRIEND_ID;
import static ru.yandex.practicum.filmorate.storage.user.db.UserDbStorage.INCORRECT_USER_ID;

@Component("FriendDbStorage")
public class FriendDbStorage implements FriendStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FriendDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addFriend(Long friendId, Long userId) {
        if (friendId <= 0) {
            throw new IllegalArgumentException(INCORRECT_FRIEND_ID);
        }
        String sqlQueryFriends = "INSERT INTO FRIENDS(USER_ID, FRIEND_ID, FRIEND_STATUS) " +
                "VALUES (?, ?, ?)";
        jdbcTemplate.update(sqlQueryFriends, friendId, userId, FALSE);
        jdbcTemplate.update(sqlQueryFriends, userId, friendId, TRUE);
    }

    @Override
    public List<User> getFriends(Long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException(INCORRECT_USER_ID);
        }

        SqlRowSet userRowsFriends = jdbcTemplate.queryForRowSet("SELECT * FROM FRIENDS WHERE USER_ID = ? " +
                "AND FRIEND_STATUS = TRUE ", userId);

        List<User> friends = new ArrayList<>();
        while (userRowsFriends.next()) {
            int friendId = userRowsFriends.getInt("FRIEND_ID");
            User friend = new User();
            getUser(friend, friendId);
            friends.add(friend);
        }
        return friends;
    }

    @Override
    public void confirmFriend(Long userId, Long notConfirmFriendId) {
        String sqlQueryFriends = "UPDATE FRIENDS SET FRIEND_STATUS = TRUE WHERE USER_ID = ? AND FRIEND_ID = ?";
        jdbcTemplate.update(sqlQueryFriends, notConfirmFriendId, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long friendId) {
        SqlRowSet userRowsGetFriendsUserId = jdbcTemplate.queryForRowSet("SELECT FRIEND_ID FROM FRIENDS " +
                "WHERE USER_ID = ? ", userId);

        List<User> commonFriends = new ArrayList<>();
        while (userRowsGetFriendsUserId.next()) {
            int id = userRowsGetFriendsUserId.getInt("FRIEND_ID");

            SqlRowSet userRowsGetFriendsFriendId= jdbcTemplate.queryForRowSet("SELECT * FROM FRIENDS " +
                    "WHERE USER_ID = ? AND FRIEND_ID = ?" , friendId, id);

            if (userRowsGetFriendsFriendId.next()) {
                User user = new User();
                getUser(user, id);
                commonFriends.add(user);
            }
        }
        return commonFriends;
    }

    @Override
    public void removeFriend(Long friendId, Long userId) {
        String sqlQueryFriends = "DELETE FROM FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?";
        jdbcTemplate.update(sqlQueryFriends, friendId, userId);
        jdbcTemplate.update(sqlQueryFriends, userId, friendId);
    }

    private void getUser(User user, int id) {
        SqlRowSet userRowsUsers = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE USER_ID = ?", id);

        if (userRowsUsers.next()) {
            user.setId(userRowsUsers.getInt("USER_ID"));
            user.setName(Objects.requireNonNull(userRowsUsers.getString("USER_NAME")));
            user.setEmail(Objects.requireNonNull(userRowsUsers.getString("EMAIL")));
            user.setLogin(Objects.requireNonNull(userRowsUsers.getString("LOGIN")));
            user.setBirthday(Objects.requireNonNull(userRowsUsers.getDate("BIRTHDAY")).toLocalDate());
        }
        SqlRowSet userRowsFriends = jdbcTemplate.queryForRowSet("SELECT * FROM FRIENDS WHERE USER_ID = ? ", id);

        Map<Long,Boolean> friendsMap = new HashMap<>();
        while (userRowsFriends.next()) {
            friendsMap.put((long) userRowsFriends.getInt("FRIEND_ID"),
                    userRowsFriends.getBoolean("FRIEND_STATUS"));
        }
        user.setFriends(friendsMap);
    }
}
