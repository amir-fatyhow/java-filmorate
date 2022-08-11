package ru.yandex.practicum.filmorate.storage.mpa.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.ArrayList;
import java.util.List;

@Component("MpaDbStorage")
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
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

    @Override
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
}
