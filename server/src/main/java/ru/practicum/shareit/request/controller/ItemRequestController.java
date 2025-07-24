package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.MapperRequestItem;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private static final String USER_ID = "X-Sharer-User-Id";
    private final RequestService requestService;

    @GetMapping
    public List<ItemRequest> getAllItemRequestsByUser(@RequestHeader(USER_ID) Long userId) {
        return requestService.getAllByUserId(userId);
    }

    @PostMapping
    public ItemRequestDto addRequest(
            @RequestHeader(USER_ID) Long id,
            @RequestBody @Valid ItemRequestDto itemRequestDto
    ) {
        ItemRequest addedItemRequest = requestService.add(MapperRequestItem.toItemRequest(itemRequestDto), id);
        return MapperRequestItem.toItemRequestDto(addedItemRequest);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(
            @RequestHeader(USER_ID) Long requestorId, @PathVariable Long requestId
    ) {
        return requestService.getById(requestId, requestorId);
    }

}
