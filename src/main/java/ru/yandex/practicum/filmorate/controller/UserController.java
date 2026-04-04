package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;


import java.util.Collection;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody User newUser) throws ValidationException {
        return userService.update(newUser);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @PutMapping("/{id}/friends/{friend_Id}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friend_Id) {
        userService.addFriend(id, friend_Id);
    }

    @DeleteMapping("/{id}/friends/{friend_Id}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friend_Id) {
        userService.removeFriend(id, friend_Id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{friend_Id}")
    public List<User> getCommonFriends(@PathVariable Long id,
                                       @PathVariable Long friend_Id) {
        return userService.getCommonFriends(id, friend_Id);
    }
}