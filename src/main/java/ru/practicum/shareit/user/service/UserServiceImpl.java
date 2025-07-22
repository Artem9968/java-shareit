package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User create(User user) {
        validateFields(user);
        validateEmailUnique(user);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(User user, Long id) {
        user.setId(id);
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (user.getName() != null && !user.getName().isBlank()) {
            oldUser.setName(user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            validateEmailUnique(user);
            oldUser.setEmail(user.getEmail());
        }
        return userRepository.save(oldUser);
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        userRepository.delete(user);
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
        userRepository.findAll().forEach(u -> {
            if (!Objects.equals(u.getId(), user.getId()) && u.getEmail().equals(user.getEmail())) {
                throw new ConflictException("The user email already exists");
            }
        });
    }
}

