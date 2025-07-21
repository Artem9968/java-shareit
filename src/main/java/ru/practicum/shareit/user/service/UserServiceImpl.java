package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        validateFields(user);
        validateEmailUnique(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("User {} has been added", users.toString().toUpperCase());
        return user;
    }

    @Override
    public User update(User user, Integer id) {
        User oldUser = users.get(id);
        if (oldUser == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        // Обновляем только непустые поля
        if (user.getName() != null && !user.getName().isBlank()) {
            oldUser.setName(user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            validateEmailUnique(user);
            oldUser.setEmail(user.getEmail());
        }
        return oldUser;
    }

    @Override
    public User getById(Integer id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь не найден");
        }
        return users.get(id);
    }

    @Override
    public Boolean deleteById(Integer id) {
        if (users.containsKey(id)) {
            users.remove(id);
            return true;
        } else {
            return false;
        }
    }

    private void validateFields(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException("The user email must include '@', be non-blank and valid");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new ValidationException("The user name can't be empty or blank");
        }
    }

    private void validateEmailUnique(User user) {
        for (User u : users.values()) {
            if (u.getId() == user.getId()) {
                continue;
            }
            if (u.getEmail().equals(user.getEmail())) {
                throw new ConflictException("The user email already exists");
            }
        }
    }

    private int getNextId() {
        return users.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;
    }
}

