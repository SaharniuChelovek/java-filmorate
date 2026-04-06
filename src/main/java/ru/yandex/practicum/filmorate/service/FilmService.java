package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public void addLike(Long filmId, Long userId) {
        log.info("Пользователь пытается поставить лайк фильму");
        Film film = getFilmById(filmId);
        //просто проверяем что пользователь существует
        userStorage.getUserById(userId);
        log.info("Пользователь поставил лайк фильму");
        film.getLikes().add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Убираем лайк");
        Film film = getFilmById(filmId);
        //и тут тоже проверяем
        userStorage.getUserById(userId);
        film.getLikes().remove(userId);
        log.info("Пользователь убрал лайк с фильма");
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Получаем список {} популярных фильмов", count);
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {

        if (newFilm.getId() == null) {
            log.error("не указан id фильма");
            throw new ValidationException("Id должен быть указан");
        }

        return filmStorage.update(newFilm);
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public void delete(Long id) {
        filmStorage.delete(id);
    }
}
