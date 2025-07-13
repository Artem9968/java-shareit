package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Data
@AllArgsConstructor
public class Item {
    private Integer id;
    @NonNull
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private ItemRequest request;
}
