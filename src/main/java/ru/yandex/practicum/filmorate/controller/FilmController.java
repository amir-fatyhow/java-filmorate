package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.databaseH2.FilmDbService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
public class FilmController {
   private final FilmDbService filmDbService;

   @Autowired
   public FilmController(FilmDbService filmDbService) {
       this.filmDbService = filmDbService;
   }

    @GetMapping("/films")
    public Collection<Film> allFilms() {
        return filmDbService.getAllFilms();
    }

    @GetMapping(value = "/films/{id}")
    @ResponseBody
    public Film getFilm(@PathVariable Long id) {
        return filmDbService.getFilm(id);
    }

    @GetMapping(value = "/genres")
    @ResponseBody
    public List<Genre> getGenres() {
        return filmDbService.getGenres();
    }

    @GetMapping(value = "/genres/{id}")
    @ResponseBody
    public Genre getGenre(@PathVariable Long id) {
        return filmDbService.getGenre(id);
    }

    @GetMapping(value = "/mpa")
    @ResponseBody
    public List<MPA> getRates() {
        return filmDbService.getMPA();
    }

    @GetMapping(value = "/mpa/{id}")
    @ResponseBody
    public MPA getRate(@PathVariable Long id) {
        return filmDbService.getMPA(id);
    }

    @GetMapping("/films/popular")
    @ResponseBody
    public List<Film> getPopularFilms(@RequestParam(required = false) String count) {
        if (count == null) {
            return filmDbService.popularFilms(10);
        }
        if (Integer.parseInt(count) <= 0) {
            throw new RuntimeException("Incorrect count");
        }
        return filmDbService.popularFilms(Integer.parseInt(count));
    }

    @PostMapping(value = "/films")
    public Film createFilm(@RequestBody Film film) {
        return filmDbService.createFilm(film);
    }

    @PutMapping(value = "/films")
    public Film updateFilm(@RequestBody Film film) {
        return filmDbService.updateFilm(film);
    }

    @PutMapping(value = "/films/{id}/like/{userId}")
    public void putLike(@PathVariable Long id, @PathVariable Long userId) {
        filmDbService.addLike(userId, id);
    }

    @DeleteMapping(value = "/films/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmDbService.removeLike(userId, id);
    }
}

