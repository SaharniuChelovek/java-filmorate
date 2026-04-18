package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTest {

    private UserController userController;

    @BeforeEach
    public void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage);

        userController = new UserController(userService);
    }

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
    void shouldFailWhenLoginEmpty() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin(" ");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

}
