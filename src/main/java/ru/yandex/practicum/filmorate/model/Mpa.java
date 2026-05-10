package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mpa {
    private Integer id;
    private String name;
    // description можно не добавлять, обычно в API он не нужен
}