package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingService bookingService;

    private BookingDto bookingDto;
    private Booking booking;
    private final Long userId = 1L;
    private final Long ownerId = 2L;
    private final Long bookingId = 1L;
    private final Long itemId = 1L;

    @BeforeEach
    void setUp() {
        User user = new User(userId, "User", "user@email.com");
        User owner = new User(ownerId, "Owner", "owner@email.com");

        Item item = Item.builder()
                .id(itemId)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .request(null) // или создайте ItemRequest если нужно
                .build();

        bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        booking = new Booking();
        booking.setId(bookingId);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.WAITING);
    }

    @Test
    @SneakyThrows
    void createBooking_InvalidDates_ReturnsBadRequest() {
        bookingDto.setStart(LocalDateTime.now().minusDays(1));

        when(bookingService.create(any(BookingDto.class), eq(userId)))
                .thenThrow(new ValidationException("Invalid dates"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void createBooking_ItemNotAvailable_ReturnsBadRequest() {
        when(bookingService.create(any(BookingDto.class), eq(userId)))
                .thenThrow(new ValidationException("Item is not available"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void approveBooking_ValidApproval_ReturnsOk() {
        when(bookingService.approveBooking(eq(bookingId), eq(ownerId), eq(true)))
                .thenReturn(booking);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void getBooking_ValidRequest_ReturnsBooking() {
        when(bookingService.getBooking(eq(bookingId), eq(userId)))
                .thenReturn(booking);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void getBooking_Unauthorized_ReturnsForbidden() {
        when(bookingService.getBooking(eq(bookingId), eq(3L)))
                .thenThrow(new ValidationException("Access denied"));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void getAllBookingsOfUserByState_ReturnsList() {
        when(bookingService.getAllBookingsOfUserByState(eq(userId), any(), eq(0), eq(10)))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    @SneakyThrows
    void getAllBookingsOfUserItems_ReturnsList() {
        when(bookingService.getAllBookingsOfUserItems(eq(ownerId), any(), eq(0), eq(10)))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }
}