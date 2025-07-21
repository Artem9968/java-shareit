package ru.practicum.shareit.booking.controller;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {
    private static final String USER_ID = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID) Long userId,
                             @RequestBody BookingDto bookingDto) {
        log.debug("received POST /booking by user Id/{}", userId);
        return BookingMapper.toBookingDto(bookingService.create(bookingDto, userId));
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader(USER_ID) Long ownerId, @PathVariable("bookingId") Long bookingId,
                                     @RequestParam("approved") Boolean isApproved) {
        Booking booking = bookingService.approveBooking(bookingId, ownerId, isApproved);
        return BookingMapper.toBookingDto(booking);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader(USER_ID) Long ownerId, @PathVariable("bookingId") Long bookingId) {
        Booking booking = bookingService.getBooking(bookingId, ownerId);
        return BookingMapper.toBookingDto(booking);
    }

    @GetMapping
    public List<BookingDto> getAllBookingsOfUserByState(
            @RequestHeader(USER_ID) Long ownerId,
            @RequestParam(value = "state", required = false, defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        List<Booking> bookings = bookingService.getAllBookingsOfUserByState(ownerId, State.valueOf(state), from, size);
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsOfUserItems(
            @RequestHeader(USER_ID) Long ownerId,
            @RequestParam(value = "state", required = false, defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        List<Booking> bookings = bookingService.getAllBookingsOfUserItems(ownerId, State.valueOf(state), from, size);
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

}
