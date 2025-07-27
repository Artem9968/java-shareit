package ru.practicum.shareit.request.service;


import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface RequestService {
    List<ItemRequest> getAllByUserId(Long id);

    ItemRequest add(ItemRequest itemRequest, Long requestorId);

    ItemRequestDto getById(Long requestId, Long requestorId);
}
