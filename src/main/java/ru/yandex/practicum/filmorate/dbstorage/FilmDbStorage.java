package ru.yandex.practicum.filmorate.dbstorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.PreparedStatement;
import java.util.*;

@Slf4j
@Component
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Film> findAll() {

        String sql = "SELECT f.*, r.name AS mpa_name FROM films f LEFT" +
                " JOIN ratings r ON f.rating_id = r.id";
        List<Film> films = jdbcTemplate.query(sql, new FilmMapper());


        films.forEach(this::loadFilmDetails);
        return films;
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, " +
                "duration, rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql,
                    new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, java.sql.Date
                    .valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setObject(5, film.getMpa() != null
                    ? film.getMpa().getId() : null);
            return stmt;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());

        saveGenres(film);

        return film;
    }

    @Override
    public Film update(Film newFilm) {
        String sql = "UPDATE films SET name = ?, description = ?, " +
                "release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa() != null ? newFilm.getMpa().getId() : null,
                newFilm.getId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Фильм с id " + newFilm.getId()
                    + " не найден");
        }

        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, newFilm.getId());
        saveGenres(newFilm);

        return getFilmById(newFilm.getId());
    }

    @Override
    public Film getFilmById(Long id) {
        String sql = "SELECT f.*, r.name AS mpa_name FROM films f LEFT JOIN " +
                "ratings r ON f.rating_id = r.id WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, new FilmMapper(), id);
            loadFilmDetails(film);
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, r.name AS mpa_name, COUNT(fl.user_id) AS " +
                "likes_count " +
                "FROM films f " +
                "LEFT JOIN ratings r ON f.rating_id = r.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), count);
        films.forEach(this::loadFilmDetails);
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) " +
                    "VALUES (?, ?)";
            jdbcTemplate.batchUpdate(sql, film.getGenres(),
                    film.getGenres().size(),
                    (ps, genre) -> {
                        ps.setLong(1, film.getId());
                        ps.setInt(2, genre.getId());
                    });
        }
    }

    private void loadFilmDetails(Film film) {
        if (film == null) return;

        String genresSql = "SELECT g.* FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";

        List<Genre> genreList = jdbcTemplate.query(genresSql, new GenreMapper(),
                film.getId());
        Set<Genre> genres = new HashSet<>(genreList);
        film.setGenres(genres);

        String likesSql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.queryForList(likesSql, Long.class,
                film.getId());
        film.setLikes(new HashSet<>(likes));
    }
}
