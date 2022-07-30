package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@NoArgsConstructor
@Data
public class Film {
    private long id;
    private @NonNull String name;
    private @NonNull String description;
    private @NonNull LocalDate releaseDate;
    private @NonNull int duration;
    private long rate;

    public Film(@NonNull String name, @NonNull String description, @NonNull LocalDate releaseDate,
                @NonNull int duration, long rate, Set<Long> usersId, MPA mpa, List<Genre> genres) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.rate = rate;
        this.usersId = usersId;
        this.mpa = mpa;
        this.genres = genres;
    }
    private Set<Long> usersId = new HashSet<>();
    private MPA mpa;
    private List<Genre> genres;
    public void addLike() {
        this.rate++;
    }
    public void removeLike() {
        this.rate--;
    }
    public void addUserId(long userId) {
        usersId.add(userId);
    }
    public void removeUserId(long userId) {
        usersId.remove(userId);
    }
}
