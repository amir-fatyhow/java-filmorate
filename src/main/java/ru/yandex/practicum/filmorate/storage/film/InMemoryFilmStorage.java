package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    public static final LocalDate FILM_BIRTHDAY = LocalDate.of(1895, 12, 28);
    public static final String INCORRECT_FILM_ID = "Incorrect filmId";
    private final Map<Long, Film> films = new HashMap<>();
    private long id = 1;

    public  Map<Long, Film> mapFilms() {
        return films;
    }

    @Override
    public Film createFilm(Film film) {
        validation(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film putFilm(Film film) {
        validation(film);
        if (film.getId() <= 0) {
            throw new NullPointerException("Negative filmId");
        }

        boolean flag = true;
        for (long filmId : films.keySet()) {
            if (filmId == film.getId()) {
                films.put(film.getId(), film);
                flag = false;
            }
        }

        if (flag) {
            log.info(INCORRECT_FILM_ID);
            throw new ValidationException(INCORRECT_FILM_ID);
        }
        return film;
    }

    @Override
    public Film getFilm(long id) {
        if (films.containsKey(id)) {
            return films.get(id);
        }
        throw new NullPointerException(INCORRECT_FILM_ID);
    }

    private Long getNextId() {
        return id++;
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
}
