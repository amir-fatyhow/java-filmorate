package ru.yandex.practicum.filmorate.storage.genre.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.ArrayList;
import java.util.List;

@Component("GenreDbStorage")
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
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

    @Override
    public Genre getGenre(long id) {
        SqlRowSet filmRowsGenres = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE GENRE_ID = ?", id);
        Genre genre = new Genre();

        if (filmRowsGenres.next()) {
            genre.setId(filmRowsGenres.getInt("GENRE_ID"));
            genre.setName(filmRowsGenres.getString("GENRE"));
        }
        return genre;
    }
}
