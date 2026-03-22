package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    @Test
    void contextLoads() {
    }

    private final UserController userController = new UserController();
    private final FilmController filmController = new FilmController();


    @Test
    void shouldCreateUser() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("TheDoctor");
        user.setName("Harley");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.create(user);

        assertNotNull(createdUser.getId());
    }

    @Test
    void shouldCreateUserWithoutName() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.create(user);

        assertNotNull(createdUser.getId());
        assertEquals("login", createdUser.getName()); // имя подставилось
    }

    @Test
    void shouldFailWhenEmailInvalid() {
        User user = new User();
        user.setEmail("wrongEmail");
        user.setLogin("Herobrine");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void shouldFailWhenLoginEmpty() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin(" ");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void shouldFailWhenBirthdayInFuture() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("TechnoBladeNeverDies");
        user.setBirthday(LocalDate.of(2038, 12, 11));

        assertThrows(ValidationException.class, () -> userController.create(user));
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
    void shouldFailWhenNameIsEmpty() {
        Film film = new Film();
        film.setName(" ");
        film.setDescription("Этот фильм... А где название???");
        film.setDuration(199);
        film.setReleaseDate(LocalDate.now());

        assertThrows(ValidationException.class, () -> filmController.create(film));
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

    @Test
    void shouldFailWhenDurationIsSubZero() {
        Film film = new Film();
        film.setName("Star Wars new hope");
        film.setDescription("Давным давно в далекой галактике...");
        film.setDuration(-1);
        film.setReleaseDate(LocalDate.now());

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }
}


