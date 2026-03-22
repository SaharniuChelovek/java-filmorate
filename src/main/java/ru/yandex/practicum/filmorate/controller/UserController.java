package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import ru.yandex.practicum.filmorate.model.User;


import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        // проверка email
        log.info("Создается пользователь {}", user);

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("не указан имейл");
            throw new ValidationException("Должен быть указан email");
        }

        if (!user.getEmail().contains("@")) {
            log.error("в имейле отсутствует символ @");
            throw new ValidationException("В почте должен быть указан символ '@'");
        }

        if (user.getEmail().startsWith("@") || user.getEmail().endsWith("@")) {
            log.error("символ @ не на том месте");
            throw new ValidationException("Некорректный email");
        }

        if (emailExists(user.getEmail())) {
            log.error("попытка использовать имейл, который уже используется");
            throw new ValidationException("Этот email уже используется");
        }
        //проверка login
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("попытка использования пустого логина");
            throw new ValidationException("Логин не может быть пустым");
        }

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
        //проверка даты рождения
        if (user.getBirthday() == null) {
            log.error("Попытка использования пустой даты");
            throw new ValidationException("дата рождения должна быть запонена");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Попытка использования даты из будущего");
            throw new ValidationException("дата рождения не может быть в будущем");
        }

        // заполняем данные
        user.setId(getNextId());

        log.info("Пользоатель {} успешно создан", user);
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) throws ValidationException {
        log.info("Обновление пользоввателя {}", newUser);
        // проверка id
        if (newUser.getId() == null) {
            log.error("id не указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.error("пользователя по заданному id не нашли");
            throw new ValidationException("Пользователь не найден");
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