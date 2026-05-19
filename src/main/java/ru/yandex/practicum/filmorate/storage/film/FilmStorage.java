package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film newFilm);

    Film getFilmById(Long id);

    void delete(Long id);

    void addLike(Long userid, Long filmid);

    void removeLike(Long userid, Long filmid);

    List<Film> getPopularFilms(int count);
}
