package ru.yandex.practicum.filmorate.model;

import lombok.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
@NoArgsConstructor
@Data
public class User {
    private long id;
    private @NonNull String email;
    private @NonNull String login;
    private @NonNull LocalDate birthday;
    private @NonNull String name;
    private Map<Long,Boolean> friends = new HashMap<>();

    public User(@NonNull String email, @NonNull String login, @NonNull LocalDate birthday,
                @NonNull String name, Map<Long, Boolean> friends) {
        this.email = email;
        this.login = login;
        this.birthday = birthday;
        this.name = name;
        this.friends = friends;
    }
    public void removeFriend(Long id) {
        this.friends.remove(id);
    }
}
