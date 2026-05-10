package ru.yandex.practicum.filmorate.dbstorage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;

@Component
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Mpa> findAll() {
        String sql = "SELECT * FROM ratings ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                Mpa.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .build()
        );
    }

    @Override
    public Mpa getById(int id) {
        String sql = "SELECT * FROM ratings WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    Mpa.builder()
                            .id(rs.getInt("id"))
                            .name(rs.getString("name"))
                            .build(), id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new NotFoundException("Рейтинг MPA с id " + id
                    + " не найден");
        }
    }
}
