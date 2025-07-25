package ru.practicum.shareit.item.model;

import io.micrometer.common.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ItemBookingInfo {
    @NonNull
    private Item item;
    private Booking lastBooking;
    private Booking nextBooking;
    private List<Comment> comments;
}
