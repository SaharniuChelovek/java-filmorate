package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    LocalDate dateOfMovie = LocalDate.of(1895, 12, 28);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Создание фильма {}", film);
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Название фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getReleaseDate().isBefore(dateOfMovie)) {
            log.error("Дата фильма до дня кино");
            throw new ValidationException("Дата фильма должна быть после " + dateOfMovie.format(formatter));
        }

        if (film.getDescription().length() > 200) {
            log.error("Описание больше 200 символов");
            throw new ValidationException("Описание не должно превышать 200 симвлов");
        }

        if (film.getDuration() < 0) {
            log.error("Продолжительность фильма меньше нуля");
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }


        // заполняем данные
        film.setId(getNextId());


        films.put(film.getId(), film);
        log.info("Создан фильм {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) throws ValidationException {
        log.info("Апдейт фильма {}", newFilm);
        // проверка id
        if (newFilm.getId() == null) {
            log.error("не указан id фильма");
            throw new ValidationException("Id должен быть указан");
        }

        if (!films.containsKey(newFilm.getId())) {
            log.error("Не найден фильм");
            throw new ValidationException("Фильм не найден");
        }

        Film oldFilm = films.get(newFilm.getId());

        if (oldFilm.getName() == null || oldFilm.getName().isBlank()) {
            log.error("Пустое название фильма на этапе обновления");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (oldFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата фильма до дня кино");
            throw new ValidationException("Дата фильма " + dateOfMovie.format(formatter));
        }

        if (oldFilm.getDescription().length() > 200) {
            log.error("Описание больше 200 символов");
            throw new ValidationException("Описание не должно превышать 200 симвлов");
        }

        if (oldFilm.getDuration() < 0) {
            log.error("Продолжительность меньше нуля");
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }

        if (newFilm.getName() != null) {
            oldFilm.setName(newFilm.getName());
        }

        if (newFilm.getDescription() != null) {
            oldFilm.setDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() > 0) {
            oldFilm.setDuration(newFilm.getDuration());
        }


        log.info("Обновлен фильм {}", oldFilm);
        return oldFilm;
    }

    private Long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}