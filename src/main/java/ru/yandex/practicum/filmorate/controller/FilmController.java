package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
   private final InMemoryFilmStorage inMemoryFilmStorage = new InMemoryFilmStorage();
   private final FilmService filmService = new FilmService(inMemoryFilmStorage);

    @GetMapping
    public Collection<Film> allFilms() {
        return filmService.getInMemoryFilmStorage().getFilms();
    }

    @GetMapping(value = "/{id}")
    @ResponseBody
    public Film getFilm(@PathVariable Long id) {
        return filmService.getInMemoryFilmStorage().getFilm(id);
    }

    @GetMapping("/popular")
    @ResponseBody
    public List<Film> getPopularFilms(@RequestParam(required = false) String count) {
        if (count == null) {
            return filmService.popularFilms(10);
        }
        if (Integer.parseInt(count) <= 0) {
            throw new RuntimeException("Incorrect count");
        }
        return filmService.popularFilms(Integer.parseInt(count));
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        return filmService.getInMemoryFilmStorage().createFilm(film);
    }

    @PutMapping
    public Film putFilm(@RequestBody Film film) {
        return filmService.getInMemoryFilmStorage().putFilm(film);
    }

    @PutMapping(value = "/{id}/like/{userId}")
    public void putLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(userId, id);
    }

    @DeleteMapping(value = "{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(userId, id);
    }
}

