package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private ItemRequest itemRequest;
    private ItemDto itemDto;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        itemRequest = new ItemRequest();
        itemRequest.setId(2L);
        itemRequest.setDescription("Need something");
        itemRequest.setRequestor(owner);
        itemRequest.setCreated(LocalDateTime.now());

        itemDto = new ItemDto();
        itemDto.setName("Item Name");
        itemDto.setDescription("Item Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(2L);

        item = new Item();
        item.setId(10L);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        item.setRequest(itemRequest);
    }

    @Test
    void create_whenValidDto_thenReturnsDto() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(itemDto.getRequestId())).thenReturn(Optional.of(itemRequest));

        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.create(itemDto, owner.getId());

        assertThat(result).isNotNull();

        assertThat(result.getName()).isEqualTo(itemDto.getName());

        verify(userRepository).findById(owner.getId());
        verify(itemRequestRepository).findById(itemDto.getRequestId());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void create_whenUserNotFound_thenThrow() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService.create(itemDto, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Not found user");
    }

    @Test
    void createComment_whenBookingExists_thenReturnsComment() {
        Long itemId = 10L;
        Long userId = 1L;

        Comment comment = new Comment();
        comment.setText("Great item!");

        Booking booking = new Booking();
        booking.setId(100L);
        booking.setBooker(owner);
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().minusDays(5));
        booking.setEnd(LocalDateTime.now().minusDays(1));

        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setText("Great item!");
        savedComment.setAuthor(owner);
        savedComment.setItem(item);
        savedComment.setCreated(LocalDateTime.now());

        when(bookingRepository.findFirstByItemIdAndBookerIdAndEndIsBefore(eq(itemId), eq(userId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        Comment result = itemService.createComment(comment, itemId, userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedComment.getId());
        assertThat(result.getAuthor()).isEqualTo(savedComment.getAuthor());
        assertThat(result.getText()).isEqualTo(comment.getText());

        verify(bookingRepository).findFirstByItemIdAndBookerIdAndEndIsBefore(eq(itemId), eq(userId), any(LocalDateTime.class));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_whenNoBooking_thenThrow() {
        Long itemId = 10L;
        Long userId = 1L;
        Comment comment = new Comment();

        when(bookingRepository.findFirstByItemIdAndBookerIdAndEndIsBefore(eq(itemId), eq(userId), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createComment(comment, itemId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bad request");
    }
}

