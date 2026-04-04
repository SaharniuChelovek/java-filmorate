package ru.yandex.practicum.filmorate.storage.user;


import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }


    @Override
    public User create(User user) {
        // проверка email
        log.info("Создается пользователь {}", user);

        if (emailExists(user.getEmail())) {
            log.error("попытка использовать имейл, который уже используется");
            throw new ValidationException("Этот email уже используется");
        }
        //проверка login
        if (user.getLogin().contains(" ")) {
            log.error("попытка ввода логина с пробелами");
            throw new ValidationException("В логине не должны быть пробелы");
        }
        //замена имени если оно пустое
        if (user.getName() == null || user.getName().isBlank()) {
            String login = user.getLogin();
            user.setName(login);
            log.info("Имя было пустым, поэтому вместо него используется логин");
        }

        // заполняем данные
        user.setId(getNextId());
        user.setFriends(new HashSet<>()); //новому юзеру создаем множемтво друзей

        log.info("Пользоатель {} успешно создан", user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        log.info("Обновление пользоввателя {}", newUser);
        // проверка id
        if (newUser.getId() == null) {
            log.error("id не указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.error("пользователя по заданному id не нашли");
            throw new NotFoundException("Пользователь не найден");
        }

        User oldUser = users.get(newUser.getId());

        // проверка email (если он меняется)
        if (newUser.getEmail() != null) {
            if (emailExists(newUser.getEmail()) &&
                    !newUser.getEmail().equals(oldUser.getEmail())) {
                log.error("Попытка использования уже использованной почты на этапе апдейта");
                throw new ValidationException("Этот email уже используется");
            }
            oldUser.setEmail(newUser.getEmail());
        }

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
            throw new NotFoundException("Пользователь по id " + id + " не найден");
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

    // проверка существования email
    private boolean emailExists(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
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


}
