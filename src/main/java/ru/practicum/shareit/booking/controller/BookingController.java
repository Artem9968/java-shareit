package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.mapper.BookingMapper;

import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                             @RequestBody @Valid BookingDto bookingDto) {
        log.debug("POST /bookings by user ID: {}", userId);
        return BookingMapper.toBookingDto(bookingService.create(bookingDto, userId));
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                     @PathVariable Long bookingId,
                                     @RequestParam Boolean approved) {
        return BookingMapper.toBookingDto(
                bookingService.approveBooking(bookingId, ownerId, approved)
        );
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                 @PathVariable Long bookingId) {
        return BookingMapper.toBookingDto(bookingService.getBooking(bookingId, userId));
    }

    @GetMapping
    public List<BookingDto> getAllBookingsOfUserByState(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        return bookingService.getAllBookingsOfUserByState(userId, State.valueOf(state), from, size)
                .stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsOfUserItems(
            @RequestHeader(USER_ID_HEADER) Long ownerId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        return bookingService.getAllBookingsOfUserItems(ownerId, State.valueOf(state), from, size)
                .stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}
