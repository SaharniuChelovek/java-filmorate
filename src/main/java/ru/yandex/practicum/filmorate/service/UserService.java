package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

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

        if (user.getFriends().containsKey(friendId)) {
            throw new ValidationException("Запрос уже отправлен");
        }

        user.getFriends().put(friendId, FriendshipStatus.PENDING);
        friend.getFriends().put(userId, FriendshipStatus.PENDING);
    }

    public void confirmFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
        friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);
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
        log.info("Получаем список друзей(подтвержденных)");
        User user = getUserById(userId);

        return user.getFriends().entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(entry -> userStorage.getUserById(entry.getKey()))
                .toList();
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Получаем список одинаковых с другим пользователем друзей");
        User user = userStorage.getUserById(userId);
        User otherUser = userStorage.getUserById(otherId);

        return user.getFriends().keySet().stream()
                .filter(id -> user.getFriends().get(id) == FriendshipStatus.CONFIRMED)
                .filter(id -> otherUser.getFriends().get(id) == FriendshipStatus.CONFIRMED)
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
