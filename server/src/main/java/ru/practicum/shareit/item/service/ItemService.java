package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingInfoDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto dto, Long userId);

    ItemDto update(ItemDto dto, Long itemId, Long userId);

    ItemWithBookingInfoDto getById(Long id);

    List<ItemDto> findAllByUser(Long id);

    List<ItemDto> searchItems(String text, Integer from, Integer size);

    Comment createComment(Comment comment, Long itemId, Long userId);
}
