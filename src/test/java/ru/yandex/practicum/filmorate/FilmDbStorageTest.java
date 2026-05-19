package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.dbstorage.FilmDbStorage;
import ru.yandex.practicum.filmorate.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;


import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmMapper.class, GenreMapper.class}) // ИМПОРТИРУЕМ ХРАНИЛИЩЕ И ВСЕ МАППЕРЫ
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate; // Нужен для подготовки тестовых данных (MPA, пользователи)

    private Film testFilm;
    private Mpa testMpa;
    private Genre testGenre;

    @BeforeEach
    void setUp() {
        //рейтинги и жанры уже есть в файле data.sql
        testMpa = Mpa.builder().id(1).name("G").build();
        testGenre = Genre.builder().id(1).name("Комедия").build();


        jdbcTemplate.update("INSERT INTO users (id, email, login, name, birthday) VALUES (1, 'test@mail.ru', 'login', 'name', '2000-01-01')");

        // 3. Собираем тестовый фильм
        testFilm = Film.builder()
                .name("Тестовый фильм")
                .description("Описание тестового фильма")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(testMpa)
                .genres(new HashSet<>(Set.of(testGenre)))
                .build();
    }

    @Test
    void shouldCreateFilmAndAssignId() {
        Film createdFilm = filmStorage.create(testFilm);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isPositive(); // ID сгенерирован
        assertThat(createdFilm.getName()).isEqualTo("Тестовый фильм");
        assertThat(createdFilm.getMpa()).isNotNull();
        assertThat(createdFilm.getMpa().getId()).isEqualTo(1);
        assertThat(createdFilm.getGenres()).hasSize(1);
    }

    @Test
    void shouldUpdateFilm() {
        Film createdFilm = filmStorage.create(testFilm);

        createdFilm.setName("Обновленное название");
        createdFilm.setDescription("Новое описание");
        createdFilm.setGenres(new HashSet<>()); // Убираем жанры

        Film updatedFilm = filmStorage.update(createdFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Обновленное название");
        assertThat(updatedFilm.getDescription()).isEqualTo("Новое описание");
        assertThat(updatedFilm.getGenres()).isEmpty(); // Жанры должны удалиться из film_genres
    }

    @Test
    void shouldFindFilmById() {
        Film createdFilm = filmStorage.create(testFilm);

        Film foundFilm = filmStorage.getFilmById(createdFilm.getId());

        // Глубокое сравнение объектов
        assertThat(foundFilm)
                .usingRecursiveComparison()
                .isEqualTo(createdFilm);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenFilmNotFound() {
        assertThatThrownBy(() -> filmStorage.getFilmById(9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReturnAllFilms() {
        filmStorage.create(testFilm);

        Film secondFilm = Film.builder()
                .name("Второй фильм")
                .description("Описание второго")
                .releaseDate(LocalDate.of(2021, 5, 10))
                .duration(90)
                .mpa(testMpa)
                .genres(new HashSet<>())
                .build();
        filmStorage.create(secondFilm);

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).isNotEmpty();
        assertThat(films).hasSize(2);
    }

    @Test
    void shouldDeleteFilm() {
        Film createdFilm = filmStorage.create(testFilm);

        filmStorage.delete(createdFilm.getId());

        assertThatThrownBy(() -> filmStorage.getFilmById(createdFilm.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldAddAndRemoveLike() {
        Film createdFilm = filmStorage.create(testFilm);

        // Ставим лайк (пользователь с id=1 был добавлен в @BeforeEach)
        filmStorage.addLike(createdFilm.getId(), 1L);

        Film likedFilm = filmStorage.getFilmById(createdFilm.getId());
        assertThat(likedFilm.getLikes()).hasSize(1);
        assertThat(likedFilm.getLikes()).contains(1L);

        // Убираем лайк
        filmStorage.removeLike(createdFilm.getId(), 1L);

        Film unlikedFilm = filmStorage.getFilmById(createdFilm.getId());
        assertThat(unlikedFilm.getLikes()).isEmpty();
    }

    @Test
    void shouldReturnPopularFilms() {
        // Создаем первый фильм и ставим лайк
        Film film1 = filmStorage.create(testFilm);
        filmStorage.addLike(film1.getId(), 1L); // 1 лайк

        // Создаем второй фильм (без лайков)
        Film film2 = Film.builder()
                .name("Непопулярный фильм")
                .description("Без лайков")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(100)
                .mpa(testMpa)
                .genres(new HashSet<>())
                .build();
        filmStorage.create(film2); // 0 лайков


        jdbcTemplate.update("INSERT INTO users (id, email, login, name, birthday) VALUES (2, 'test2@mail.ru', 'login2', 'name2', '2000-01-01')");

        Film film3 = Film.builder()
                .name("Очень популярный фильм")
                .description("Много лайков")
                .releaseDate(LocalDate.of(2022, 1, 1))
                .duration(110)
                .mpa(testMpa)
                .genres(new HashSet<>())
                .build();
        Film createdFilm3 = filmStorage.create(film3);
        filmStorage.addLike(createdFilm3.getId(), 1L);
        filmStorage.addLike(createdFilm3.getId(), 2L); // 2 лайка


        List<Film> popularFilms = filmStorage.getPopularFilms(2);

        assertThat(popularFilms).hasSize(2);

        assertThat(popularFilms.get(0).getId()).isEqualTo(createdFilm3.getId());
        assertThat(popularFilms.get(1).getId()).isEqualTo(film1.getId());
    }
}
