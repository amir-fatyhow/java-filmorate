package ru.yandex.practicum.filmorate.storage.user.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component("UserDbStorage")
public class UserDbStorage implements UserStorage {
    public static final String INCORRECT_USER_ID = "Incorrect userId";
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }
    @Override
    public User createUser(User user) {
        validation(user);

        String sqlQueryUsers = "INSERT INTO USERS(EMAIL, LOGIN, BIRTHDAY, USER_NAME) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQueryUsers, new String[]{"USER_ID"});

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setDate(3, Date.valueOf(user.getBirthday()));
            stmt.setString(4, user.getName());
            return stmt;
        }, keyHolder);

        long user_id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(user_id);

        return user;
    }

    @Override
    public Collection<User> getUsers() {
        SqlRowSet userRowsUsers = jdbcTemplate.queryForRowSet("SELECT * FROM USERS");
        Map<Long, User> users = new HashMap<>();

        while (userRowsUsers.next()) {
            User user = new User();
            long user_id = userRowsUsers.getInt("USER_ID");

            SqlRowSet userRowsFriends = jdbcTemplate.queryForRowSet("SELECT * FROM FRIENDS WHERE USER_ID = ? "
                    , user_id);
            makeRequestGetUser(user, userRowsUsers, userRowsFriends);
            users.put(user_id, user);
        }
        return users.values();
    }

    @Override
    public User putUser(User user) {
        validation(user);

        long user_id = user.getId();
        if (user_id <= 0) {
            throw new NullPointerException("Negative userId");
        }

        String sqlQueryUsers = "UPDATE USERS SET USER_NAME = ?, LOGIN = ?, BIRTHDAY = ?, EMAIL = ? WHERE USER_ID = ?";

        jdbcTemplate.update(sqlQueryUsers
                , user.getName()
                , user.getLogin()
                , Date.valueOf(user.getBirthday())
                , user.getEmail()
                , user_id);
        return user;
    }

    @Override
    public User getUserById(long id) {
        if (id <= 0) {
            throw new NullPointerException(INCORRECT_USER_ID);
        }
        SqlRowSet userRowsUsers = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE USER_ID = ?", id);
        SqlRowSet userRowsFriends = jdbcTemplate.queryForRowSet("SELECT * FROM FRIENDS WHERE USER_ID = ? ", id);

        User user = new User();
        if (userRowsUsers.next()) {
            makeRequestGetUser(user, userRowsUsers, userRowsFriends/*, userRowsNotConfirmFriends*/);
        }
        return user;
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

    private void selectAllFromUsers(User user, SqlRowSet userRowsUsers) {
        user.setId(userRowsUsers.getInt("USER_ID"));
        user.setName(Objects.requireNonNull(userRowsUsers.getString("USER_NAME")));
        user.setEmail(Objects.requireNonNull(userRowsUsers.getString("EMAIL")));
        user.setLogin(Objects.requireNonNull(userRowsUsers.getString("LOGIN")));
        user.setBirthday(Objects.requireNonNull(userRowsUsers.getDate("BIRTHDAY")).toLocalDate());
    }

    private void makeRequestGetUser(User user, SqlRowSet userRowsUsers, SqlRowSet userRowsFriends) {
        selectAllFromUsers(user, userRowsUsers);

        Map<Long,Boolean> friendsMap = new HashMap<>();
        while (userRowsFriends.next()) {
            friendsMap.put((long) userRowsFriends.getInt("FRIEND_ID"),
                    userRowsFriends.getBoolean("FRIEND_STATUS"));
        }
        user.setFriends(friendsMap);
    }
}
