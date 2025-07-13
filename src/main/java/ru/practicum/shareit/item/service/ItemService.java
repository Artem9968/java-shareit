package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto dto, Integer userId);

    ItemDto update(ItemDto dto, Integer itemId, Integer userId);

    ItemDto getById(Integer id);

    List<ItemDto> findAllByUser(Integer id);

    List<ItemDto> findFreeItemByKeyword(String text);
}
