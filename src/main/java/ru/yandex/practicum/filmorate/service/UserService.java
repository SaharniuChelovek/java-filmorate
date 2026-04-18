package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;


import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public void addFriend(Long userId, Long friendId) {
        log.info("Добавляем друга");
        User user = userStorage.getUserById(userId);
        //проверка на существование пользователя
        User friend = userStorage.getUserById(friendId);

        boolean added = user.getFriends().add(friendId);

        if (!added) {
            log.info("Пользователь уже в друзьях");
            throw new ValidationException("Пользователь уже в друзьях");
        }

        friend.getFriends().add(userId);
        log.info("Друг добавлен");
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Удаляем друга из списка друзей");
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Друг удален из списка друзей");
    }

    public List<User> getFriends(Long userId) {
        log.info("Получаем список друзей");
        User user = userStorage.getUserById(userId);
        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .toList();
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Получаем список одинаковых с другим пользователем друзей");
        User user = userStorage.getUserById(userId);
        User otherUser = userStorage.getUserById(otherId);

        return user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(userStorage::getUserById)
                .toList();
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {

        if (newUser.getId() == null) {
            log.error("id не указан");
            throw new ValidationException("Id должен быть указан");
        }

        return userStorage.update(newUser);
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public void delete(Long id) {
        userStorage.delete(id);
    }

}
