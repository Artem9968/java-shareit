package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.dto.BookingDto;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.booking.repository.BookingRepository;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private BookingDto bookingDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        item = new Item();
        item.setId(10L);
        item.setName("Item");
        item.setDescription("Item Desc");
        item.setAvailable(true);
        item.setOwner(owner);

        bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        booking = new Booking();
        booking.setId(100L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(Status.WAITING);
    }

    @Test
    void create_whenValid_thenReturnsBooking() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        Booking created = bookingService.create(bookingDto, booker.getId());

        assertThat(created).isNotNull();
        assertThat(created.getItem().getId()).isEqualTo(item.getId());
        assertThat(created.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(created.getStatus()).isEqualTo(Status.WAITING);

        verify(userRepository).findById(booker.getId());
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository).save(any());
    }

    @Test
    void create_whenBookerIsOwner_thenThrowNotFound() {
        bookingDto.setItemId(item.getId());
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.create(bookingDto, owner.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booker is equals owner");

        verify(userRepository).findById(owner.getId());
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_whenItemNotAvailable_thenThrowValidation() {
        item.setAvailable(false);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.create(bookingDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("not available");

        verify(userRepository).findById(booker.getId());
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_whenInvalidDates_thenThrowValidation() {
        // start after end
        bookingDto.setStart(LocalDateTime.now().plusDays(3));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.create(bookingDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Start date is after end date");
    }

    @Test
    void approveBooking_whenNotOwner_thenThrowValidation() {
        booking.setStatus(Status.WAITING);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Long wrongOwnerId = 999L;
        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), wrongOwnerId, true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("id владельцев не одинаковые");
    }

    @Test
    void approveBooking_whenStatusNotWaiting_thenThrowValidation() {
        booking.setStatus(Status.APPROVED);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), owner.getId(), true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Статус бронирования");
    }

    @Test
    void getBooking_whenBookerOrOwner_thenReturnsBooking() {
        booking.setStatus(Status.APPROVED);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Booking foundByBooker = bookingService.getBooking(booking.getId(), booker.getId());
        assertThat(foundByBooker).isNotNull();

        Booking foundByOwner = bookingService.getBooking(booking.getId(), owner.getId());
        assertThat(foundByOwner).isNotNull();
    }

    @Test
    void getBooking_whenUserNotBookerOrOwner_thenThrowValidation() {
        booking.setStatus(Status.APPROVED);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Long strangerUserId = 999L;
        assertThatThrownBy(() -> bookingService.getBooking(booking.getId(), strangerUserId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("id владельцев не одинаковые");
    }

    @Test
    void getAllBookingsOfUserByState_whenSizeZero_thenThrow() {
        assertThatThrownBy(() -> bookingService.getAllBookingsOfUserByState(booker.getId(), null, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Размер должен быть больше нуля!");
    }
}
