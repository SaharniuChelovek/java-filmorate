package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Добавляем в друзья пользователя {} к {}", userId, friendId);
        // Проверка на существование пользователей
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Удаляем друга");
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        log.info("Получаем список друзей");
        userStorage.getUserById(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Получаем список общих друзей");
        userStorage.getUserById(userId);
        userStorage.getUserById(otherId);

        return userStorage.getCommonFriends(userId, otherId);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        // Замена имени если оно пустое
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя было пустым, вместо него используется логин");
        }

        try {
            return userStorage.create(user);
        } catch (DataIntegrityViolationException e) {

            log.error("Попытка использовать уже существующий email");
            throw new ValidationException("Этот email уже используется");
        }
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.error("id не указан");
            throw new ValidationException("Id должен быть указан");
        }

        // Проверка, что пользователь существует
        userStorage.getUserById(newUser.getId());

        // Если имя пришло пустым, подставляем логин
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }

        try {
            return userStorage.update(newUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Попытка использовать уже существующий email " +
                    "при обновлении");
            throw new ValidationException("Этот email уже используется");
        }
    }

    public User getUserById(Long id) {
        User user = userStorage.getUserById(id);
        return user;
    }

    public void delete(Long id) {
        userStorage.delete(id);
    }
}
