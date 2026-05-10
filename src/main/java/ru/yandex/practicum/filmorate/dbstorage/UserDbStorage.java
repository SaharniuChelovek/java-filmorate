package ru.yandex.practicum.filmorate.dbstorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.PreparedStatement;
import java.util.*;

@Slf4j
@Component
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, new UserMapper());
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User update(User newUser) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday(),
                newUser.getId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Пользователь с id " + newUser.getId() + " не найден");
        }
        return newUser;
    }

    @Override
    public User getUserById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new UserMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, id);
        if (rowsDeleted == 0) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }


    @Override
    public void addFriend(Long userId, Long friendId) {

        String checkReverseSql = "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?";
        try {
            String reverseStatus = jdbcTemplate.queryForObject(checkReverseSql, String.class, friendId, userId);


            if ("PENDING".equals(reverseStatus)) {

                String updateReverseSql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
                jdbcTemplate.update(updateReverseSql, FriendshipStatus.CONFIRMED.name(), friendId, userId);

                String insertSql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
                jdbcTemplate.update(insertSql, userId, friendId, FriendshipStatus.CONFIRMED.name());
                log.info("Пользователи {} и {} теперь взаимные друзья", userId, friendId);
            } else if ("CONFIRMED".equals(reverseStatus)) {
                log.info("Пользователи {} и {} уже друзья", userId, friendId);
            }
        } catch (EmptyResultDataAccessException e) {
            String insertSql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertSql, userId, friendId, FriendshipStatus.PENDING.name());
            log.info("Пользователь {} отправил заявку в друзья пользователю {}", userId, friendId);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {

        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, userId, friendId);

        if (rowsDeleted == 0) {
            throw new NotFoundException("Связь не найдена");
        }

        String updateReverseSql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ? AND status = ?";
        jdbcTemplate.update(updateReverseSql,
                FriendshipStatus.PENDING.name(), friendId, userId, FriendshipStatus.CONFIRMED.name());

        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {

        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON u.id = f.friend_id " +
                "WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, new UserMapper(), userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.id = f1.friend_id " +
                "JOIN friendships f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ? AND f1.status = ? AND f2.status = ?";
        return jdbcTemplate.query(sql, new UserMapper(), userId, otherId,
                FriendshipStatus.CONFIRMED.name(), FriendshipStatus.CONFIRMED.name());
    }
}