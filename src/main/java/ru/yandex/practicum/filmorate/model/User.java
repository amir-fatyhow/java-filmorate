package ru.yandex.practicum.filmorate.model;

import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private long id;
    private @NonNull String email;
    private @NonNull String login;
    private @NonNull LocalDate birthday;
    private @NonNull String name;
    private Set<Long> friends = new HashSet<>();

    // Для десереализация при GET-запросе
    public User() {}

    public User(@NonNull String email, @NonNull String login, @NonNull LocalDate birthday, @NonNull String name) {
        this.email = email;
        this.login = login;
        this.birthday = birthday;
        this.name = name;
    }

    public void addFriend(Long id) {
        this.friends.add(id);
    }

    public void removeFriend(Long id) {
        this.friends.remove(id);
    }
}
