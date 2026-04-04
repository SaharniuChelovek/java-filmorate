package ru.yandex.practicum.filmorate.storage.film;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private static final LocalDate DATE_OF_MOVIE = LocalDate.of(1895, 12, 28);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final int DURATION_MAX_LENGTH = 200;
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        log.info("Создание фильма {}", film);

        if (film.getReleaseDate().isBefore(DATE_OF_MOVIE)) {
            log.error("Дата фильма до дня кино");
            throw new ValidationException("Дата фильма должна быть после " + DATE_OF_MOVIE.format(FORMATTER));
        }

        if (film.getDescription().length() > 200) {
            log.error("Описание больше {} символов", DURATION_MAX_LENGTH);
            throw new ValidationException("Описание не должно превышать " + DURATION_MAX_LENGTH + " симвoлов");
        }

        // заполняем данные
        film.setId(getNextId());
        film.setLikes(new HashSet<>()); //при создании фильма создаем список лайков
        films.put(film.getId(), film);
        log.info("Создан фильм {}", film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        log.info("Апдейт фильма {}", newFilm);
        // проверка id
        if (newFilm.getId() == null) {
            log.error("не указан id фильма");
            throw new NotFoundException("Id должен быть указан");
        }

        if (!films.containsKey(newFilm.getId())) {
            log.error("Не найден фильм");
            throw new NotFoundException("Фильм не найден");
        }

        Film oldFilm = films.get(newFilm.getId());

        if (newFilm.getName() != null) {
            oldFilm.setName(newFilm.getName());
        }

        if (newFilm.getDescription() != null) {
            oldFilm.setDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() > 0) {
            oldFilm.setDuration(newFilm.getDuration());
        }


        log.info("Обновлен фильм {}", oldFilm);
        return oldFilm;
    }

    @Override
    public Film getFilmById(Long id) {
        log.info("Получение фильма по id {}", id);

        if (!films.containsKey(id)) {
            log.error("Фильм по id {} не найден", id);
            throw new NotFoundException("Фильм по id " + id + " не найден");
        }
        log.info("Фильм по id {} найден, возвращаем", id);
        return films.get(id);
    }

    @Override
    public void delete(Long id) {
        log.info("Удаление фильма с id {}", id);

        if (!films.containsKey(id)) {
            log.error("Фильм с id {} не найден", id);
            throw new NotFoundException("Фильм не найден");
        }

        films.remove(id);
        log.info("Фильм с id {} удален", id);
    }

    private Long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
