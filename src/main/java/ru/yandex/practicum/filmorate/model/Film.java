package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;
import java.time.LocalDate;

@Data
public class Film {
    private long id = 0;
    private @NonNull String name;
    private @NonNull String description;
    private @NonNull LocalDate releaseDate;
    private @NonNull int duration;
}
