package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {

    // Без аннотаций - генерируется БД
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не указана")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность не указана")
    @Positive(message = "Продолжительность должна быть положительной")
    private Integer duration;

    @NotNull(message = "Рейтинг MPA не указан")
    private Mpa mpa;

    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    @Builder.Default
    private Set<Long> likes = new HashSet<>();
}
