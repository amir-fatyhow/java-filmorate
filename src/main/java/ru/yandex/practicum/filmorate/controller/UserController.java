package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final InMemoryUserStorage inMemoryUserStorage = new InMemoryUserStorage();
    private final UserService userService = new UserService(inMemoryUserStorage);

    @GetMapping
    public Collection<User> allUsers() {
        return userService.getInMemoryUserStorage().getUsers();
    }

    @GetMapping(value = "/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getInMemoryUserStorage().getUser(id);
    }

    @GetMapping(value = "/{id}/friends")
    public List<User> getUserFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    @GetMapping(value = "/{id}/friends/common/{otherId}")
    public List<User> getCommonUserFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.getInMemoryUserStorage().createUser(user);
    }

    @PutMapping
    public User putUser(@RequestBody User user) {
        return userService.getInMemoryUserStorage().putUser(user);
    }

    @PutMapping(value = "/{id}/friends/{friendId}")
    public void putFriend(@PathVariable Long id, @PathVariable Long friendId) {
         userService.addFriend(friendId, id);
    }

    @DeleteMapping(value = "/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.removeFriend(friendId, id);
    }
}
