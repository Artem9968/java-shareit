package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    private Collection<UserDto> getAllUsers() {
        return userService.findAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    private UserDto getUser(@PathVariable("id") Long id) {
        return UserMapper.toUserDto(userService.getById(id));
    }

    @PostMapping
    private UserDto addUser(@RequestBody UserDto user) {
        var s = userService.create(UserMapper.toUser(user));
        return UserMapper.toUserDto(s);
    }

    @PatchMapping("/{id}")
    private UserDto updateUser(@RequestBody UserDto user, @PathVariable Long id) {
        var u = UserMapper.toUser(user);
        userService.update(u, id);
        return UserMapper.toUserDto(userService.getById(id));
    }

    @DeleteMapping("/{id}")
    private void deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
    }
}
