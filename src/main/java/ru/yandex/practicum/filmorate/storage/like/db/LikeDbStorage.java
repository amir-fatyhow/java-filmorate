package ru.yandex.practicum.filmorate.storage.like.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;

@Component("LikeDbStorage")
public class LikeDbStorage implements LikeStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LikeDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addLike(long userId, long filmId) {
        SqlRowSet user = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_USERID_LIKED WHERE FILM_ID = ? " +
                "AND USER_ID = ?", filmId, userId);

        // проверяем ставил ли пользователь лайк указанному фильму
        boolean flag = false;
        while (user.next()) {
            flag = true;
        }
        if (flag) {
            return;
        }

        int temp = getRate(userId,filmId) + 1;
        String sqlQueryFilmsLike = "INSERT INTO FILM_USERID_LIKED(FILM_ID, USER_ID) VALUES (?, ?)";
        String sqlQueryFilmsCountLike = "UPDATE FILMS SET RATE = ? WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQueryFilmsCountLike, temp, filmId);
        jdbcTemplate.update(sqlQueryFilmsLike, filmId, userId);
    }

    @Override
    public void removeLike(long userId, long filmId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Incorrect userId");
        }

        int temp = getRate(userId,filmId) - 1;

        String deleteAllLikeFilm = "DELETE FROM FILM_USERID_LIKED WHERE FILM_ID = ? AND USER_ID = ?";
        String sqlQueryFilmsCountLike = "UPDATE FILMS SET RATE = ? WHERE FILM_ID = ?";

        jdbcTemplate.update(sqlQueryFilmsCountLike, temp, filmId);
        jdbcTemplate.update(deleteAllLikeFilm, filmId, userId);
    }

    private int getRate(long userId, long filmId) {
        String sqlQueryFilmRemoveLiked = "DELETE FROM FILM_USERID_LIKED WHERE USER_ID = ? AND FILM_ID = ?";
        jdbcTemplate.update(sqlQueryFilmRemoveLiked, userId, filmId);

        String sqlQueryFilm = "SELECT RATE FROM FILMS WHERE FILM_ID = ?";
        SqlRowSet sqlQueryFilmRow = jdbcTemplate.queryForRowSet(sqlQueryFilm,
                filmId);
        int rate = 0;
        if (sqlQueryFilmRow.next()) {
            rate = sqlQueryFilmRow.getInt("RATE");
        }
        return rate;
    }
}
