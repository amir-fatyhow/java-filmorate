package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

@Data
public class User {
    private long id;
    private @NonNull String email;
    private @NonNull String login;
    private @NonNull LocalDate birthday;
    private @NonNull String name;
}
