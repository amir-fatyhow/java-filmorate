package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;
import java.util.Map;

public interface FilmStorage {
    Map<Long, Film>  mapFilms();

    Film createFilm(Film film);

    Collection<Film> getFilms();

    Film putFilm(Film film);

    Film getFilm(long id);
}
