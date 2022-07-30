package ru.yandex.practicum.filmorate.service.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.db.UserDbStorage;
import java.util.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static ru.yandex.practicum.filmorate.storage.user.db.UserDbStorage.INCORRECT_USER_ID;

@Service
public class UserDbService {
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;

    public static final String INCORRECT_FRIEND_ID = "Incorrect friendId";

    @Autowired
    public UserDbService(@Qualifier("UserDbStorage") UserDbStorage userDbStorage, JdbcTemplate jdbcTemplate) {
        this.userDbStorage = userDbStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addFriend(Long friendId, Long userId) {
        if (friendId <= 0) {
            throw new IllegalArgumentException(INCORRECT_FRIEND_ID);
        }
        String sqlQueryFriends = "INSERT INTO FRIENDS(USER_ID, FRIEND_ID, FRIEND_STATUS) " +
                "VALUES (?, ?, ?)";
        jdbcTemplate.update(sqlQueryFriends, friendId, userId, FALSE);
        jdbcTemplate.update(sqlQueryFriends, userId, friendId, TRUE);
    }

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

    public void confirmFriend(Long userId, Long notConfirmFriendId) {
        String sqlQueryFriends = "UPDATE FRIENDS SET FRIEND_STATUS = TRUE WHERE USER_ID = ? AND FRIEND_ID = ?";
        jdbcTemplate.update(sqlQueryFriends, notConfirmFriendId, userId);
    }

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

    public void removeFriend(Long friendId, Long userId) {
        String sqlQueryFriends = "DELETE FROM FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?";
        jdbcTemplate.update(sqlQueryFriends, friendId, userId);
        jdbcTemplate.update(sqlQueryFriends, userId, friendId);
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
