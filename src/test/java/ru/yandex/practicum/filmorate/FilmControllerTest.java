package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.dbstorage.GenreDbStorage;
import ru.yandex.practicum.filmorate.dbstorage.MpaDbStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    public void setUp() {
        JdbcTemplate jdbcTemplate = null;
        GenreMapper genreMapper = null;
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        MpaStorage mpaStorage = new MpaDbStorage(jdbcTemplate);
        GenreStorage genreStorage = new GenreDbStorage(jdbcTemplate, genreMapper);

        FilmService filmService = new FilmService(userStorage, filmStorage, mpaStorage, genreStorage);

        filmController = new FilmController(filmService);
    }

    @Test
    void shouldCreateFilm() {
        Film film = new Film();
        film.setName("Star Wars new hope");
        film.setDescription("Давным давно в далекой галактике...");
        film.setDuration(199);
        film.setReleaseDate(LocalDate.now());

        assertNotNull(filmController.create(film));
    }

    @Test
    void shouldFailWhenDateIsAncient() {
        Film film = new Film();
        film.setName("Guardians of the galaxy 3");
        film.setDescription("Use your heart boy");
        film.setDuration(200);
        film.setReleaseDate(LocalDate.of(1894, 12, 28));

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldFailWhenDescriptionTooLong() {
        String description = "*".repeat(201);
        Film film = new Film();
        film.setName("Млечный путь");
        film.setDescription(description);
        film.setDuration(199);
        film.setReleaseDate(LocalDate.now());

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldNotFailWhenDescription200() {
        String description = "*".repeat(200);
        Film film = new Film();
        film.setName("Млечный путь");
        film.setDescription(description);
        film.setDuration(199);
        film.setReleaseDate(LocalDate.now());

        assertNotNull(filmController.create(film));
    }


    @Test
    void shouldNotFailWhenDurationIsZero() {
        Film film = new Film();
        film.setName("Мгновение");
        film.setDescription("Уже закончился");
        film.setDuration(0);
        film.setReleaseDate(LocalDate.now());

        assertNotNull(filmController.create(film));
    }


}
