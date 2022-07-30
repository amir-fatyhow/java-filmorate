package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.util.*;

@Service
public class FilmService {
    private static final int DEFAULT_AMOUNT_OF_FILMS = 10;
    private final InMemoryFilmStorage inMemoryFilmStorage;

    @Autowired
    public FilmService(@Qualifier("InMemoryFilmStorage") InMemoryFilmStorage inMemoryFilmStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
    }

    public void addLike(long userId, long filmId) {
        Map<Long, Film> films = inMemoryFilmStorage.mapFilms();

        for (Map.Entry<Long,Film> entry : films.entrySet()) {
            if (Objects.equals(entry.getKey(), filmId) && !films.get(filmId).getUsersId().contains(userId)) {
               films.get(filmId).addLike();
               films.get(filmId).addUserId(userId);
            }
        }
    }

    public void removeLike(long userId, long filmId) {
        if (userId <= 0) {
            throw new NullPointerException("Incorrect userId");
        }

        Map<Long, Film> films = inMemoryFilmStorage.mapFilms();

        for (Map.Entry<Long,Film> entry : films.entrySet()) {
            if (Objects.equals(entry.getKey(), filmId) && films.get(filmId).getUsersId().contains(userId)) {
                films.get(filmId).removeLike();
                films.get(filmId).removeUserId(userId);
            }
        }
    }

    public List<Film> popularFilms(int count) {
        Comparator<Film> filmComparator =
                Comparator.comparingLong(Film::getRate).reversed();

        List<Film> films = new ArrayList<>(inMemoryFilmStorage.getFilms());

        films.sort(filmComparator);

        int filmsSize = films.size();

        if (count == 0) {
            if (filmsSize < DEFAULT_AMOUNT_OF_FILMS) {
                films = films.subList(0,filmsSize);
            } else {
                films = films.subList(0, DEFAULT_AMOUNT_OF_FILMS);
            }
        } else {
            if (count > filmsSize) {
                films = films.subList(0,filmsSize);
            } else {
                films = films.subList(0,count);
            }
        }
        return films;
    }

    public Collection<Film> getAllFilms() {
        return inMemoryFilmStorage.getFilms();
    }

    public Film getFilm(long id) {
        return inMemoryFilmStorage.getFilm(id);
    }

    public Film createFilm(Film film) {
        return inMemoryFilmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return inMemoryFilmStorage.putFilm(film);
    }
}
