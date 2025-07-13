package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final Map<Integer, Item> items = new HashMap<>();
    private final UserService userService;

    @Override
    public ItemDto create(ItemDto dto, Integer userId) {
        User owner = userService.getById(userId);
        Item item = ItemMapper.toItem(dto, owner, null);
        item.setId(getNextId());
        items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(ItemDto dto, Integer itemId, Integer userId) {
        dto.setId(itemId);
        Item item = items.get(itemId);
        checkItem(itemId);
        checkOwner(item, userId);

        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) item.setAvailable(dto.getAvailable());

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getById(Integer id) {
        checkItem(id);
        return ItemMapper.toItemDto(items.get(id));
    }

    @Override
    public List<ItemDto> findAllByUser(Integer userId) {
        userService.getById(userId);
        return items.values().stream()
                .filter(item -> item.getOwner().getId() == userId)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findFreeItemByKeyword(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return items.values().stream()
                .filter(item -> item.getAvailable().equals(true))
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList()).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

    }

    private int getNextId() {
        return items.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;
    }

    private void checkItem(Integer id) {
        if (!items.containsKey(id)) {
            throw new NotFoundException("Item not found");
        }
    }

    private void checkOwner(Item item, Integer ownerId) {
        if (item.getOwner().getId() != ownerId) {
            throw new NotFoundException("The user not found");
        }
    }
}
