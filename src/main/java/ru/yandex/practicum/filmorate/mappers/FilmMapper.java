package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date")
                        .toLocalDate())
                .duration(rs.getInt("duration"))
                .build();


        if (rs.getInt("rating_id") != 0) {
            Mpa mpa = Mpa.builder()
                    .id(rs.getInt("rating_id"))
                    .name(rs.getString("mpa_name"))
                    .build();
            film.setMpa(mpa);
        }
        return film;
    }
}
