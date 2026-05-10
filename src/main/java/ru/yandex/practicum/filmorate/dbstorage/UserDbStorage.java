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
        String sql = "INSERT INTO users (email, login, name, birthday) " +
                "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql,
                    new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, java.sql.Date.valueOf(user
                    .getBirthday()));
            return stmt;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User update(User newUser) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, " +
                "birthday = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday(),
                newUser.getId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Пользователь с id " + newUser.getId()
                    + " не найден");
        }
        return newUser;
    }

    @Override
    public User getUserById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new UserMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id " + id
                    + " не найден");
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, id);
        if (rowsDeleted == 0) {
            throw new NotFoundException("Пользователь с id " + id
                    + " не найден");
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        // 1. Проверяем, есть ли уже МОЯ заявка к нему
        String checkMySql = "SELECT status FROM friendships WHERE user_id = ?" +
                " AND friend_id = ?";
        try {
            String myStatus = jdbcTemplate.queryForObject(checkMySql,
                    String.class, userId, friendId);

            if ("PENDING".equals(myStatus)) {
                String updateSql = "UPDATE friendships SET status = ? WHERE " +
                        "user_id = ? AND friend_id = ?";
                jdbcTemplate.update(updateSql, FriendshipStatus.CONFIRMED
                        .name(), userId, friendId);
            }
            return;

        } catch (EmptyResultDataAccessException e) {
            log.debug("Заявка от пользователя {} к {} не найдена, идем дальше", userId, friendId);
        }

        String checkReverseSql = "SELECT status FROM friendships WHERE " +
                "user_id = ? AND friend_id = ?";
        try {
            String reverseStatus = jdbcTemplate.queryForObject(checkReverseSql,
                    String.class, friendId, userId);

            if ("PENDING".equals(reverseStatus)) {

                String updateReverseSql = "UPDATE friendships SET status = ? " +
                        "WHERE user_id = ? AND friend_id = ?";
                jdbcTemplate.update(updateReverseSql, FriendshipStatus
                        .CONFIRMED.name(), friendId, userId);

                String insertSql = "INSERT INTO friendships (user_id, " +
                        "friend_id, status) VALUES (?, ?, ?)";
                jdbcTemplate.update(insertSql, userId, friendId,
                        FriendshipStatus.CONFIRMED.name());
            }
        } catch (EmptyResultDataAccessException e) {

            String insertSql = "INSERT INTO friendships (user_id, " +
                    "friend_id, status) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertSql, userId, friendId,
                    FriendshipStatus.PENDING.name());
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND " +
                "friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);

        String updateReverseSql = "UPDATE friendships SET status = ? WHERE " +
                "user_id = ? AND friend_id = ? AND status = ?";
        jdbcTemplate.update(updateReverseSql,
                FriendshipStatus.PENDING.name(), friendId, userId,
                FriendshipStatus.CONFIRMED.name());
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
                "WHERE u.id IN (" +

                "SELECT friend_id FROM friendships WHERE user_id = ? " +
                "UNION " +

                "SELECT user_id FROM friendships WHERE friend_id = ?" +
                ") " +

                "AND u.id IN (" +
                "SELECT friend_id FROM friendships WHERE user_id = ? " +
                "UNION " +
                "SELECT user_id FROM friendships WHERE friend_id = ?" +
                ")";

        return jdbcTemplate.query(sql, new UserMapper(), userId, userId,
                otherId, otherId);
    }
}