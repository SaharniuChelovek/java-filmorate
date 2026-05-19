package ru.yandex.practicum.filmorate.storage.user;


import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {

        log.info("Создается пользователь {}", user);

        //проверка login
        if (user.getLogin().contains(" ")) {
            log.error("попытка ввода логина с пробелами");
            throw new ValidationException("В логине не должны быть пробелы");
        }

        // заполняем данные
        user.setId(getNextId());
        user.setFriends(new HashMap<>());

        log.info("Пользоатель {} успешно создан", user);
        users.put(user.getId(), user);
        return user;

    }

    @Override
    public User update(User newUser) {
        log.info("Обновление пользоввателя {}", newUser);

        if (!users.containsKey(newUser.getId())) {
            log.error("пользователя по заданному id не нашли");
            throw new NotFoundException("Пользователь не найден");
        }

        User oldUser = users.get(newUser.getId());

        if (newUser.getLogin() != null && newUser.getLogin().contains(" ")) {
            log.error("пробелы в логине на этапе апдейта");
            throw new ValidationException("В логине не должны быть пробелы");
        }

        // обновление имени
        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName());
        }

        if (newUser.getLogin() != null) {
            oldUser.setLogin(newUser.getLogin());
        }

        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }

        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }

        log.info("Обновлен пользователь {}", oldUser);
        return oldUser;
    }

    @Override
    public User getUserById(Long id) {
        log.info("Получение пользователя по id {}", id);

        if (!users.containsKey(id)) {
            log.error("Пользователь по id {} не найден", id);
            throw new NotFoundException("Пользователь по id " + id
                    + " не найден");
        }
        log.info("Пользователь по id {} найден, возвращаем", id);
        return users.get(id);
    }

    @Override
    public void delete(Long id) {

        log.info("Удаление пользователя с id {}", id);

        if (!users.containsKey(id)) {
            log.error("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь не найден");
        }

        users.remove(id);
        log.info("Пользователь с id {} удален", id);
    }

    // генерация id
    private Long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public void addFriend(Long userId, Long friendId) {
        User user = getUserById(userId);

        User friend = getUserById(friendId);

        if (user.getFriends().containsKey(friendId)) {
            throw new ValidationException("Запрос уже отправлен");
        }

        user.getFriends().put(friendId, FriendshipStatus.PENDING);
        friend.getFriends().put(userId, FriendshipStatus.PENDING);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public List<User> getFriends(Long userId) {
        User user = getUserById(userId);

        return user.getFriends().entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(entry -> getUserById(entry.getKey()))
                .toList();
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = getUserById(userId);
        User otherUser = getUserById(otherId);

        return user.getFriends().entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .filter(id -> otherUser.getFriends().get(id)
                        == FriendshipStatus.CONFIRMED)
                .map(this::getUserById)
                .toList();
    }

}
