package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {

    private Long id;

    @NotBlank
    @NotNull
    private String name;

    @NotBlank
    @NotNull
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @NotNull
    @Min(0)
    private Integer duration;

    @NotNull
    private String genre;

    @NotNull
    private String rating;

    private Set<Long> likes;
}
