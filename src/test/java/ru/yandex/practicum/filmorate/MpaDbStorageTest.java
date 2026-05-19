package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.dbstorage.MpaDbStorage;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(MpaDbStorage.class)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void shouldFindAllRatings() {
        Collection<Mpa> ratings = mpaStorage.findAll();

        assertThat(ratings).isNotEmpty();
        assertThat(ratings).hasSize(5);
    }

    @Test
    void shouldFindRatingById() {
        Mpa mpa = mpaStorage.getById(1);

        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(1);
        assertThat(mpa.getName()).isEqualTo("G");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRatingNotFound() {

        assertThatThrownBy(() -> mpaStorage.getById(999))
                .isInstanceOf(NotFoundException.class);
    }
}