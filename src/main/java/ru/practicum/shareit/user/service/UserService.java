package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {

    Collection<User> findAll();

    User create(User user);

    User update(User user, Long id);

    User getById(Long id);

    void deleteById(Long id);
}

