package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private long id;
    private String email;
    private String login;
    private LocalDate birthday;
    private String name;

    public User(String email, String login, LocalDate birthday, String name) {
        this.email = email;
        this.login = login;
        this.birthday = birthday;
        this.name = name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
