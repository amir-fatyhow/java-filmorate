package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private Map<Long, Film> films = new HashMap<>();
    private long id = 1;

    @GetMapping
    public Collection<Film> allFilms() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        if (film.getName().isBlank()) {
            log.info("Неверно введено название фильма");
            throw new ValidationException("Название не может быть пустым.");
        }
        if (film.getDescription().length() > 200) {
            log.info("Описание фильма превышает 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28"))){
            log.info("Дата релиза раньше 28 декабря 1895 года.");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
        }
        if (film.getDuration() <= 0) {
            log.info("Продолжительность фильма должна быть отрицательная.");
            throw new ValidationException("Продолжительность фильма должна быть положительной.");
        }
        film.setId(id);
        id++;
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film put(@RequestBody Film film) {
        if (film.getName().isEmpty()) {
            log.info("Неверно введено название фильма");
            throw new ValidationException("Название не может быть пустым.");
        }
        if (film.getDescription().length() > 200) {
            log.info("Описание фильма превышает 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28"))){
            log.info("Дата релиза раньше 28 декабря 1895 года.");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
        }
        if (film.getDuration() <= 0) {
            log.info("Продолжительность фильма должна быть отрицательная.");
            throw new ValidationException("Продолжительность фильма должна быть положительной.");
        }

        boolean flag = true;
        for (long l : films.keySet()) {
            if (l == film.getId()) {
                films.put(film.getId(), film);
                flag = false;
            }
        }

        if (flag) {
            log.info("неверный id");
            throw new ValidationException("неверный id");
        }

        return film;
    }
}

