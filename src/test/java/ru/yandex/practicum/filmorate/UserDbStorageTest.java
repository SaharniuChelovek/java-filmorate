package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dbstorage.UserDbStorage;
import ru.yandex.practicum.filmorate.mappers.UserMapper;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserMapper.class})
class UserDbStorageTest {

    private final UserDbStorage userStorage;
    private User testUser;

    @BeforeEach
    void setUp() {

        testUser = User.builder()
                .email("test@mail.ru")
                .login("testLogin")
                .name("Test Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
    }

    @Test
    void shouldCreateUserAndAssignId() {
        User createdUser = userStorage.create(testUser);


        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isPositive(); // ID больше 0
        assertThat(createdUser.getEmail()).isEqualTo("test@mail.ru");
    }

    @Test
    void shouldUpdateUser() {
        User createdUser = userStorage.create(testUser);


        createdUser.setEmail("new@mail.ru");
        createdUser.setName("New Name");

        User updatedUser = userStorage.update(createdUser);

        assertThat(updatedUser.getEmail()).isEqualTo("new@mail.ru");
        assertThat(updatedUser.getName()).isEqualTo("New Name");
    }

    @Test
    void shouldFindUserById() {
        User createdUser = userStorage.create(testUser);

        User foundUser = userStorage.getUserById(createdUser.getId());


        assertThat(foundUser)
                .usingRecursiveComparison()
                .isEqualTo(createdUser);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotFound() {

        assertThatThrownBy(() -> userStorage.getUserById(9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReturnAllUsers() {
        userStorage.create(testUser);

        User secondUser = User.builder()
                .email("second@mail.ru")
                .login("secondLogin")
                .name("Second")
                .birthday(LocalDate.of(1995, 5, 15))
                .build();
        userStorage.create(secondUser);

        Collection<User> users = userStorage.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(2);
    }

    @Test
    void shouldDeleteUser() {
        User createdUser = userStorage.create(testUser);

        userStorage.delete(createdUser.getId());

        assertThatThrownBy(() -> userStorage.getUserById(createdUser.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}
