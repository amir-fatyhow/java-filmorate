package ru.yandex.practicum.filmorate.service.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.genre.db.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.db.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.db.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;

@Service
public class FilmDbService {
    private final FilmStorage filmDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final MpaDbStorage mpaDbStorage;

    private final LikeDbStorage likeDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbService(@Qualifier("FilmDbStorage") FilmStorage filmDbStorage, JdbcTemplate jdbcTemplate
            , @Qualifier("GenreDbStorage") GenreDbStorage genreDbStorage
            , @Qualifier("MpaDbStorage") MpaDbStorage mpaDbStorage
            , @Qualifier("LikeDbStorage") LikeDbStorage likeDbStorage) {

        this.filmDbStorage = filmDbStorage;
        this.jdbcTemplate = jdbcTemplate;
        this.genreDbStorage = genreDbStorage;
        this.mpaDbStorage = mpaDbStorage;
        this.likeDbStorage = likeDbStorage;
    }

    public void addLike(long userId, long filmId) {
        likeDbStorage.addLike(userId, filmId);
    }

    public void removeLike(long userId, long filmId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Incorrect userId");
        }
        likeDbStorage.removeLike(userId, filmId);
    }

    public List<Genre> getGenres() {
        return genreDbStorage.getGenres();
    }

    public Genre getGenre(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Incorrect id");
        }
        return genreDbStorage.getGenre(id);
    }

    public List<MPA> getMPA() {
        return mpaDbStorage.getMPA();
    }

    public MPA getMPA(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Incorrect id");
        }
        return mpaDbStorage.getMPA(id);
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

    public void removeFilm(Long id) {
        filmDbStorage.removeFilm(id);
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
