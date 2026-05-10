package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private static final LocalDate DATE_OF_MOVIE = LocalDate.of(1895, 12, 28);
    private static final int DURATION_MAX_LENGTH = 200;

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public FilmService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка фильму {} от пользователя {}", filmId, userId);
        // Проверяем существование
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка");
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Получение топ-{} популярных фильмов", count);
        return filmStorage.getPopularFilms(count);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        // Проверяем, существует ли фильм (выбросит NotFoundException, если нет)
        filmStorage.getFilmById(newFilm.getId());

        validateFilm(newFilm);
        return filmStorage.update(newFilm);
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public void delete(Long id) {
        filmStorage.delete(id);
    }

    // Валидация вынесена в сервис
    private void validateFilm(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(DATE_OF_MOVIE)) {
            throw new ValidationException("Дата релиза должна быть не раньше " + DATE_OF_MOVIE);
        }
        if (film.getDescription() != null && film.getDescription().length() > DURATION_MAX_LENGTH) {
            throw new ValidationException("Максимальная длина описания — " + DURATION_MAX_LENGTH + " символов");
        }
    }
}
