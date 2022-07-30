package ru.yandex.practicum.filmorate.storage.film.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    public static final String INCORRECT_FILM_ID = "Incorrect filmId";
    public static final LocalDate FILM_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film createFilm(Film film) {
        validation(film);
        if (film.getMpa() == null){
            throw new ValidationException("INCORRECT MPA");
        }

        long film_id;
        String sqlQueryFilms = "INSERT INTO FILMS(FILM_NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATE) " +
                "VALUES (?, ?, ?, ?, ?)";

        String sqlQueryFilmMpa = "INSERT INTO FILM_MPA(FILM_ID, MPA_ID) VALUES (?, ?)";

        String sqlQueryFilmGenre = "INSERT INTO FILM_GENRE(FILM_ID, GENRE_ID) VALUES (?, ?)";

        String sqlQueryMpa = "SELECT * FROM MPA WHERE MPA_ID = ?";

        SqlRowSet filmRowsMpa = jdbcTemplate.queryForRowSet(sqlQueryMpa,
                film.getMpa().getId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQueryFilms, new String[]{"FILM_ID"});

            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, (int) film.getRate());
            return stmt;
        }, keyHolder);

        film_id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        jdbcTemplate.update(sqlQueryFilmMpa, film_id, film.getMpa().getId());

        if (film.getGenres() != null) {
            for (int i = 0; i < film.getGenres().size(); i++) {
                jdbcTemplate.update(sqlQueryFilmGenre, film_id, film.getGenres().get(i).getId());
            }

            for (int i = 0; i < film.getGenres().size(); i++) {
                SqlRowSet filmRowsGenres = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE GENRE_ID = ?",
                        film.getGenres().get(i).getId());

                if (filmRowsGenres.next()) {
                    film.getGenres().get(i).setName(filmRowsGenres.getString("GENRE"));
                }
            }
        }
        if (filmRowsMpa.next()) {
            film.getMpa().setName(filmRowsMpa.getString("MPA"));
        }
        film.setId(film_id);
        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        SqlRowSet filmRowsFilms = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS");

        Map<Long, Film> films = new HashMap<>();

        while (filmRowsFilms.next()) {
            Film film = new Film();
            long film_id = filmRowsFilms.getInt("FILM_ID");

            SqlRowSet filmRowsMpa = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_MPA WHERE FILM_ID = ?", film_id);

            SqlRowSet filmRowsGenre = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_GENRE " +
                    "WHERE FILM_ID = ?", film_id);

            SqlRowSet filmRowsLiked = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_USERID_LIKED WHERE FILM_ID = ?"
                    ,film_id);

            makeRequestGetFilm(film,filmRowsFilms,filmRowsMpa,filmRowsLiked,filmRowsGenre);

            films.put(film_id, film);
        }
        return films.values();
    }

    @Override
    public Film putFilm(Film film) {
        validation(film);

        long film_id = film.getId();
        if (film_id <= 0) {
            throw new NullPointerException("Negative filmId");
        }

        String sqlQueryFilms = "UPDATE FILMS SET FILM_NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?" +
                ", RATE = ? WHERE FILM_ID = ?";

        String sqlQueryFilmMpa = "UPDATE FILM_MPA SET MPA_ID = ? WHERE FILM_ID = ?";

        String deleteAllRecordsFilmGenreOfFilm = "DELETE FROM FILM_GENRE WHERE FILM_ID = ?";

        String sqlQueryFilmGenre = "INSERT INTO FILM_GENRE(FILM_ID, GENRE_ID) VALUES (?, ?)";

        String sqlQueryMpa = "SELECT * FROM MPA WHERE MPA_ID = ?";
        SqlRowSet filmRowsMpaId = jdbcTemplate.queryForRowSet(sqlQueryMpa,
                film.getMpa().getId());

        if (filmRowsMpaId.next()) {
            film.getMpa().setName(filmRowsMpaId.getString("MPA"));
        }

        jdbcTemplate.update(sqlQueryFilms
                , film.getName()
                , film.getDescription()
                , Date.valueOf(film.getReleaseDate())
                , film.getDuration()
                , film.getRate()
                , film_id);

        jdbcTemplate.update(sqlQueryFilmMpa, film.getMpa().getId(), film_id);
        jdbcTemplate.update(deleteAllRecordsFilmGenreOfFilm, film_id);

        if (film.getGenres() != null) {
            List<Genre> genres = new ArrayList<>();

            for (int i = 0; i < film.getGenres().size() ; i++) {
                boolean flag = false;
                for (int k = i + 1; k < film.getGenres().size() ; k++){
                    if (film.getGenres().get(i).getId() == film.getGenres().get(k).getId()) {
                        flag = true;
                    }
                }
                if (!flag) {
                    Genre genre = new Genre();
                    genre.setId(film.getGenres().get(i).getId());
                    genres.add(genre);
                }
            }

            Comparator<Genre> genreComparator =
                    Comparator.comparingLong(Genre::getId);
            genres.sort(genreComparator);

            film.setGenres(genres);

            for (int i = 0; i < film.getGenres().size(); i++) {
                jdbcTemplate.update(sqlQueryFilmGenre, film_id, film.getGenres().get(i).getId());
            }

            for (int i = 0; i < film.getGenres().size(); i++) {
                SqlRowSet filmRowsGenres = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE GENRE_ID = ?",
                        film.getGenres().get(i).getId());

                if (filmRowsGenres.next()) {
                    film.getGenres().get(i).setName(filmRowsGenres.getString("GENRE"));
                }
            }
        }
        return film;
    }

    @Override
    public Film getFilm(long id) {
        if (id <= 0) {
            throw new NullPointerException(INCORRECT_FILM_ID);
        }

        SqlRowSet filmRowsFilms = jdbcTemplate.queryForRowSet("SELECT * FROM FILMS WHERE FILM_ID = ?", id);
        SqlRowSet filmRowsMpa = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_MPA WHERE FILM_ID = ?", id);
        SqlRowSet filmRowsGenre = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_GENRE WHERE FILM_ID = ?", id);
        SqlRowSet filmRowsLiked = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_USERID_LIKED WHERE FILM_ID = ?", id);

        Film film = new Film();

        if(filmRowsFilms.next()) {
            makeRequestGetFilm(film,filmRowsFilms,filmRowsMpa,filmRowsLiked,filmRowsGenre);
        }
            return film;
    }

    private void validation(Film film) {
        if (film.getName().isBlank()) {
            log.info("Неверно введено название фильма");
            throw new ValidationException("Название не может быть пустым.");
        }
        if (film.getDescription().length() > 200) {
            log.info("Описание фильма превышает 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов.");
        }
        if (film.getReleaseDate().isBefore(FILM_BIRTHDAY) || film.getReleaseDate().equals(FILM_BIRTHDAY)){
            log.info("Дата релиза раньше 28 декабря 1895 года.");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
        }
        if (film.getDuration() <= 0) {
            log.info("Продолжительность фильма должна быть положительной.");
            throw new ValidationException("Продолжительность фильма должна быть положительной.");
        }
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
