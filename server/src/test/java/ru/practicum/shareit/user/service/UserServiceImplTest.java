package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Максим")
                .email("maxim@example.com")
                .build();
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = (List<User>) userService.findAll();

        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).isEqualTo(user);
        verify(userRepository).findAll();
    }

    @Test
    void create_whenValidUser_thenSavesUser() {
        when(userRepository.findAll()).thenReturn(List.of());
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.create(user);

        assertThat(result).isEqualTo(user);
        verify(userRepository).findAll();
        verify(userRepository).save(user);
    }

    @Test
    void create_whenInvalidEmail_thenThrowValidationException() {
        user.setEmail("invalidemail");

        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_whenDuplicateEmail_thenThrowConflictException() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        User newUser = User.builder()
                .id(2L)
                .name("Другой пользователь")
                .email(user.getEmail())
                .build();

        assertThatThrownBy(() -> userService.create(newUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("email");

        verify(userRepository).findAll();
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_whenUserExists_thenUpdatesFields() {
        User updateUser = User.builder()
                .name("Обновлённый")
                .email("updated@example.com")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(List.of(user)); // for email unique check
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(updateUser, user.getId());

        assertThat(result.getName()).isEqualTo(updateUser.getName());
        assertThat(result.getEmail()).isEqualTo(updateUser.getEmail());

        verify(userRepository).findById(user.getId());
        verify(userRepository).findAll();
        verify(userRepository).save(any());
    }

    @Test
    void update_whenUserDoesNotExist_thenThrowNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(user, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_whenEmailDuplicate_thenThrowConflict() {
        User anotherUser = User.builder()
                .id(2L)
                .name("Другой")
                .email("other@example.com")
                .build();

        User updateUser = User.builder()
                .email(anotherUser.getEmail())
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(List.of(user, anotherUser)); // will detect conflict

        assertThatThrownBy(() -> userService.update(updateUser, user.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("email");

        verify(userRepository).findById(user.getId());
        verify(userRepository).findAll();
        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_whenExists_thenReturnsUser() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User result = userService.getById(user.getId());

        assertThat(result).isEqualTo(user);
        verify(userRepository).findById(user.getId());
    }

    @Test
    void getById_whenNotFound_thenThrowNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");

        verify(userRepository).findById(999L);
    }

    @Test
    void deleteById_whenUserExists_thenDeletesUser() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.deleteById(user.getId());

        verify(userRepository).findById(user.getId());
        verify(userRepository).delete(user);
    }

    @Test
    void deleteById_whenUserNotFound_thenThrowNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).delete(any());
    }
}

