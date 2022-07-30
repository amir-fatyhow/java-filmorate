package ru.yandex.practicum.filmorate.service.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;

@Service
public class FilmDbService {
    private final FilmStorage filmDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbService(@Qualifier("FilmDbStorage") FilmStorage filmDbStorage, JdbcTemplate jdbcTemplate) {
        this.filmDbStorage = filmDbStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

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

        int temp = getMPA(userId,filmId) + 1;
        String sqlQueryFilmsLike = "INSERT INTO FILM_USERID_LIKED(FILM_ID, USER_ID) VALUES (?, ?)";
        String sqlQueryFilmsCountLike = "UPDATE FILMS SET RATE = ? WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQueryFilmsCountLike, temp, filmId);
        jdbcTemplate.update(sqlQueryFilmsLike, filmId, userId);
    }

    public void removeLike(long userId, long filmId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Incorrect userId");
        }

        int temp = getMPA(userId,filmId) - 1;

        String deleteAllLikeFilm = "DELETE FROM FILM_USERID_LIKED WHERE FILM_ID = ? AND USER_ID = ?";
        String sqlQueryFilmsCountLike = "UPDATE FILMS SET RATE = ? WHERE FILM_ID = ?";

        jdbcTemplate.update(sqlQueryFilmsCountLike, temp, filmId);
        jdbcTemplate.update(deleteAllLikeFilm, filmId, userId);
    }

    public List<Genre> getGenres() {
        SqlRowSet filmRowsGenres = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES");
        List<Genre> genres = new ArrayList<>();

        while (filmRowsGenres.next()) {
            Genre genre = new Genre();
            genre.setId(filmRowsGenres.getInt("GENRE_ID"));
            genre.setName(filmRowsGenres.getString("GENRE"));
            genres.add(genre);
        }
        return genres;
    }

    public Genre getGenre(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Incorrect id");
        }

        SqlRowSet filmRowsGenres = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE GENRE_ID = ?", id);
        Genre genre = new Genre();

        if (filmRowsGenres.next()) {
            genre.setId(filmRowsGenres.getInt("GENRE_ID"));
            genre.setName(filmRowsGenres.getString("GENRE"));
        }
        return genre;
    }

    public List<MPA> getMPA() {
        SqlRowSet filmRowsRates = jdbcTemplate.queryForRowSet("SELECT * FROM MPA");
        List<MPA> genres = new ArrayList<>();

        while (filmRowsRates.next()) {
            MPA MPA = new MPA();
            MPA.setId(filmRowsRates.getInt("MPA_ID"));
            MPA.setName(filmRowsRates.getString("MPA"));
            genres.add(MPA);
        }
        return genres;
    }

    public MPA getMPA(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Incorrect id");
        }
        SqlRowSet filmRowsRate = jdbcTemplate.queryForRowSet("SELECT * FROM MPA WHERE MPA_ID = ?", id);
        MPA MPA = new MPA();

        if (filmRowsRate.next()) {
            MPA.setId(filmRowsRate.getInt("MPA_ID"));
            MPA.setName(filmRowsRate.getString("MPA"));
        }
        return MPA;
    }

    public List<Film> popularFilms(int count) {
        SqlRowSet filmRowsFilms = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS ORDER BY RATE DESC LIMIT ?"
                                                                                                            , count);
        List<Film> films = new ArrayList<>();

        while (filmRowsFilms.next()) {
            Film film = new Film();
            long film_id = filmRowsFilms.getInt("FILM_ID");

            SqlRowSet filmRowsMpa = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_MPA WHERE FILM_ID = ?", film_id);

            SqlRowSet filmRowsGenre = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_GENRE " +
                                                                        "WHERE FILM_ID = ?", film_id);

            SqlRowSet filmRowsLiked = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_USERID_LIKED WHERE film_id = ?"
                                                                        ,film_id);

            makeRequestGetFilm(film,filmRowsFilms,filmRowsMpa,filmRowsLiked,filmRowsGenre);
            films.add(film);
        }
        return films;
    }

    public Collection<Film> getAllFilms() {
        return filmDbStorage.getFilms();
    }

    public Film getFilm(long id) {
        return filmDbStorage.getFilm(id);
    }

    public Film createFilm(Film film) {
        return filmDbStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmDbStorage.putFilm(film);
    }

    private int getMPA(long userId, long filmId) {
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

    private void updateRate(int temp, long filmId, long userId) {
        String sqlQueryFilmsLike = "INSERT INTO FILM_USERID_LIKED(FILM_ID, USER_ID) VALUES (?, ?)";
        String sqlQueryFilmsCountLike = "UPDATE FILMS SET RATE = ? WHERE FILM_ID = ?";

        jdbcTemplate.update(sqlQueryFilmsCountLike, temp, filmId);
        jdbcTemplate.update(sqlQueryFilmsLike, filmId, userId);
    }

    private void selectAllFromFilms(Film film, SqlRowSet filmRowsFilms ) {
        film.setId(filmRowsFilms.getInt("FILM_ID"));
        film.setName(Objects.requireNonNull(filmRowsFilms.getString("FILM_NAME")));
        film.setDescription(Objects.requireNonNull(filmRowsFilms.getString("DESCRIPTION")));
        film.setReleaseDate(Objects.requireNonNull(filmRowsFilms.getDate("RELEASE_DATE")).toLocalDate());
        film.setDuration(filmRowsFilms.getInt("DURATION"));
        film.setRate(filmRowsFilms.getInt("RATE"));
    }

    private List<Genre> getGenres(SqlRowSet filmRowsGenre) {
        List<Genre> genres = new ArrayList<>();
        while (filmRowsGenre.next()) {
            Genre genre = new Genre();
            genre.setId(filmRowsGenre.getInt("GENRE_ID"));
            genres.add(genre);
        }
        return genres;
    }

    private void makeRequestGetFilm(Film film, SqlRowSet filmRowsFilms, SqlRowSet filmRowsMpa, SqlRowSet filmRowsLiked
            ,SqlRowSet filmRowsGenre) {

        selectAllFromFilms(film, filmRowsFilms);

        if (filmRowsMpa.next()) {
            MPA mpa = new MPA();
            mpa.setId(filmRowsMpa.getInt("MPA_ID"));
            film.setMpa(mpa);
        }

        Set<Long> usersId = new HashSet<>();
        while (filmRowsLiked.next()) {
            usersId.add((long) filmRowsLiked.getInt("USER_ID"));
        }
        film.setUsersId(usersId);

        film.setGenres(getGenres(filmRowsGenre));

        for (int i = 0; i < film.getGenres().size(); i++) {
            SqlRowSet filmRowsGenres = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE GENRE_ID = ?",
                    film.getGenres().get(i).getId());

            if (filmRowsGenres.next()) {
                film.getGenres().get(i).setName(filmRowsGenres.getString("GENRE"));
            }
        }

        String sqlQueryMpa = "SELECT * FROM MPA WHERE MPA_ID = ?";
        SqlRowSet filmRowsMpaId = jdbcTemplate.queryForRowSet(sqlQueryMpa,
                film.getMpa().getId());

        if (filmRowsMpaId.next()) {
            film.getMpa().setName(filmRowsMpaId.getString("MPA"));
        }
    }
}
