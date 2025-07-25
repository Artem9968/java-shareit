package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingInfoDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto dto, @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemService.create(dto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto dto, @PathVariable Long itemId, @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemService.update(dto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingInfoDto getById(@PathVariable Long itemId) {
        return itemService.getById(itemId);
    }

    @GetMapping
    public List<ItemDto> getAllByUser(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemService.findAllByUser(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(defaultValue = "") String text,
                                     @RequestParam(defaultValue = "0") Integer from,
                                     @RequestParam(defaultValue = "10") Integer size) {
        return itemService.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestBody @Valid CommentDto commentDto,
            @PathVariable Long itemId
    ) {
        Comment addedComment = itemService.createComment(CommentMapper.toComment(commentDto), itemId, ownerId);
        return CommentMapper.toDtoResponse(addedComment);
    }
}
