package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
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

    private static final LocalDate DATE_OF_MOVIE = LocalDate.of(1895, 12, 28);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final int DURATION_MAX_LENGTH = 200;
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Создание фильма {}", film);

        if (film.getReleaseDate().isBefore(DATE_OF_MOVIE)) {
            log.error("Дата фильма до дня кино");
            throw new ValidationException("Дата фильма должна быть после " + DATE_OF_MOVIE.format(FORMATTER));
        }

        if (film.getDescription().length() > 200) {
            log.error("Описание больше {} символов", DURATION_MAX_LENGTH);
            throw new ValidationException("Описание не должно превышать " + DURATION_MAX_LENGTH + " симвoлов");
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