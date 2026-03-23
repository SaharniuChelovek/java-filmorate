package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    @NotBlank
    @NotNull
    private String login;

    private String name;

    @NotNull
    @Email
    private String email;

    @NotNull
    @PastOrPresent
    private LocalDate birthday;
}
