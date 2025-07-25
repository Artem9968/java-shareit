package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingResponseShortDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithBookingInfoDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    private Boolean available;
    private Long requestId;
    private BookingResponseShortDto lastBooking;
    private BookingResponseShortDto nextBooking;
    private List<CommentDto> comments;

}