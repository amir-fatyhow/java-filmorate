package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private long id;
    private @NonNull String name;
    private @NonNull String description;
    private @NonNull LocalDate releaseDate;
    private @NonNull int duration;
    private long countLike;
    private Set<Long> usersId = new HashSet<>();

    // Для десереализация при GET-запросе
    public Film() {}

    public Film(@NonNull String name, @NonNull String description, @NonNull LocalDate releaseDate,
                @NonNull int duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }

    public void addLike() {
        this.countLike++;
    }

    public void removeLike() {
        this.countLike--;
    }

    public void addUserId(long userId) {
        usersId.add(userId);
    }

    public void removeUserId(long userId) {
        usersId.remove(userId);
    }
}
