package ru.yandex.practicum.filmorate.dbstorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
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
        String sql = "SELECT f.*, r.name AS mpa_name FROM films f LEFT JOIN " +
                "ratings r ON f.rating_id = r.id";
        List<Film> films = jdbcTemplate.query(sql, new FilmMapper());
        loadFilmDetailsBatch(films); // Используем пакетную загрузку
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

        return newFilm;
    }

    @Override
    public Film getFilmById(Long id) {
        String sql = "SELECT f.*, r.name AS mpa_name FROM films f LEFT JOIN ratings r ON f.rating_id = r.id WHERE f.id = ?";

        // ПО КОММЕНТАРИЮ РЕВЬЮЕРА: используем query() без try-catch
        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        Film film = films.get(0);
        loadFilmDetailsBatch(List.of(film)); // Для одного фильма тоже используем батч-метод, чтобы не дублировать код
        return film;
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
        String sql = "SELECT f.*, r.name AS mpa_name, COUNT(fl.user_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN ratings r ON f.rating_id = r.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, new FilmMapper(), count);
        loadFilmDetailsBatch(films); // ПО КОММЕНТАРИЮ РЕВЬЮЕРА: пакетная загрузка вместо цикла
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

    private void loadFilmDetailsBatch(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        // Собираем все ID фильмов
        List<Long> filmIds = films.stream().map(Film::getId).toList();

        // Формируем строку с плейсхолдерами (?, ?, ?) для IN-запроса
        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));

        // 1. Пакетная загрузка жанров
        Map<Long, Set<Genre>> filmGenresMap = new HashMap<>();
        String genresSql = "SELECT fg.film_id, g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id IN (" + inSql + ")";

        jdbcTemplate.query(genresSql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = Genre.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .build();
            filmGenresMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
        }, filmIds.toArray());

        // 2. Пакетная загрузка лайков
        Map<Long, Set<Long>> filmLikesMap = new HashMap<>();
        String likesSql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" + inSql + ")";

        jdbcTemplate.query(likesSql, rs -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            filmLikesMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }, filmIds.toArray());

        for (Film film : films) {
            film.setGenres(filmGenresMap.getOrDefault(film.getId(), new HashSet<>()));
            film.setLikes(filmLikesMap.getOrDefault(film.getId(), new HashSet<>()));
        }
    }
}
