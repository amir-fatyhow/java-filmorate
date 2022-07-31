package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.db.UserDbService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserDbService userDbService;

    @Autowired
    public UserController(UserDbService userDbService) {
        this.userDbService = userDbService;
    }

    @GetMapping
    public Collection<User> allUsers() {
        return userDbService.getAllUsers();
    }

    @GetMapping(value = "/{id}")
    public User getUser(@PathVariable Long id) {
        return userDbService.getUserById(id);
    }

    @GetMapping(value = "/{id}/friends")
    public List<User> getUserFriends(@PathVariable Long id) {
        return userDbService.getFriends(id);
    }

    @GetMapping(value = "/{id}/friends/common/{otherId}")
    public List<User> getCommonUserFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userDbService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userDbService.createUser(user);
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        return userDbService.updateUser(user);
    }

    @PutMapping(value = "/{id}/friends/{friendId}")
    public void makeFriend(@PathVariable Long id, @PathVariable Long friendId) {
         userDbService.addFriend(friendId, id);
    }

    @PutMapping(value = "{id}/confirm/{notConfirmFriendId}")
    public void confirmFriend(@PathVariable Long id, @PathVariable Long notConfirmFriendId) {
        userDbService.confirmFriend(id, notConfirmFriendId);
    }

    @DeleteMapping(value = "/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userDbService.removeFriend(friendId, id);
    }

    @DeleteMapping(value = "/{id}")
    public void removeUser(@PathVariable Long id) {
        userDbService.removeUser(id);
    }
}
