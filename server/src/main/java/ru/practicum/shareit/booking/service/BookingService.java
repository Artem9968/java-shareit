package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {

    Booking approveBooking(Long bookingId, Long ownerId, Boolean isApproved);

    Booking getBooking(Long bookingId, Long userId);

    List<Booking> getAllBookingsOfUserByState(Long bookerId, State state, Integer from, Integer size);

    List<Booking> getAllBookingsOfUserItems(Long ownerId, State state, Integer from, Integer size);

    Booking create(BookingDto bookingDto, Long userId);

}
