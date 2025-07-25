package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private RequestServiceImpl requestService;

    private User user;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        itemRequest = new ItemRequest();
        itemRequest.setId(10L);
        itemRequest.setDescription("Request description");
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());
    }

    @Test
    void getAllByUserId_whenUserExists_thenReturnsRequests() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(user.getId())).thenReturn(List.of(itemRequest));

        List<ItemRequest> requests = requestService.getAllByUserId(user.getId());

        assertThat(requests).isNotEmpty();
        assertThat(requests.get(0)).isEqualTo(itemRequest);

        verify(userRepository).findById(user.getId());
        verify(itemRequestRepository).findByRequestorIdOrderByCreatedDesc(user.getId());
    }

    @Test
    void getAllByUserId_whenUserNotFound_thenThrowNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getAllByUserId(999L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).findById(999L);
        verify(itemRequestRepository, never()).findByRequestorIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void add_whenUserExists_thenReturnsSavedRequest() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenAnswer(i -> {
            ItemRequest ir = i.getArgument(0);
            ir.setId(20L);
            return ir;
        });

        ItemRequest inputRequest = new ItemRequest();
        inputRequest.setDescription("New request");

        ItemRequest savedRequest = requestService.add(inputRequest, user.getId());

        assertThat(savedRequest).isNotNull();
        assertThat(savedRequest.getId()).isEqualTo(20L);
        assertThat(savedRequest.getRequestor()).isEqualTo(user);
        assertThat(savedRequest.getCreated()).isNotNull();

        verify(userRepository).findById(user.getId());
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void add_whenUserNotFound_thenThrowNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.add(new ItemRequest(), 999L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).findById(999L);
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void getById_whenRequestAndUserExist_thenReturnsDtoWithItems() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));

        ItemDto itemDto = new ItemDto();
        itemDto.setId(100L);
        itemDto.setName("Item for request");
        when(itemService.findAllByRequestId(itemRequest.getId())).thenReturn(List.of(itemDto));

        ItemRequestDto dto = requestService.getById(itemRequest.getId(), user.getId());

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(itemRequest.getId());
        assertThat(dto.getItems()).isNotEmpty();
        assertThat(dto.getItems().get(0).getId()).isEqualTo(itemDto.getId());

        verify(userRepository).findById(user.getId());
        verify(itemRequestRepository).findById(itemRequest.getId());
        verify(itemService).findAllByRequestId(itemRequest.getId());
    }

    @Test
    void getById_whenUserNotFound_thenThrowNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getById(10L, 999L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).findById(999L);
        verify(itemRequestRepository, never()).findById(anyLong());
        verify(itemService, never()).findAllByRequestId(anyLong());
    }

    @Test
    void getById_whenRequestNotFound_thenThrowNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getById(999L, user.getId()))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).findById(user.getId());
        verify(itemRequestRepository).findById(999L);
        verify(itemService, never()).findAllByRequestId(anyLong());
    }
}
