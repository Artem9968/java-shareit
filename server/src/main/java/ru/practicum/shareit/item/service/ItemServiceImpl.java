package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingInfoDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.mapper.CommentMapper;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;


    @Override
    @Transactional
    public ItemDto create(ItemDto dto, Long userId) {
        Item item;
        User owner = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Not found user"));
        if (dto == null) {
            item = null;
        } else {
            item = ItemMapper.toItem(dto, owner, null);
        }
        assert dto != null;
        Long requestId = dto.getRequestId();
        assert item != null;
        item.setOwner(owner);
        if (requestId != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Not found item"));
            item.setRequest(itemRequest);
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(ItemDto dto, Long itemId, Long userId) {
        dto.setId(itemId);
        Long itemDtoId = dto.getId();
        User owner = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Not found user"));
        String name = dto.getName();
        String description = dto.getDescription();
        Boolean available = dto.getAvailable();
        Item itemToUpdate = itemRepository.findById(itemDtoId).orElseThrow(() -> new NotFoundException("Not found item"));
        checkOwner(itemToUpdate, userId);
        if (name != null) {
            itemToUpdate.setName(name);
        }
        if (description != null) {
            itemToUpdate.setDescription(description);
        }
        if (available != null) {
            itemToUpdate.setAvailable(available);
        }
        return ItemMapper.toItemDto(itemRepository.save(itemToUpdate));
    }

    @Override
    public ItemWithBookingInfoDto getById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found item"));
        List<CommentDto> comments = commentRepository.findAllByItemId(item.getId()).stream()
                .map(CommentMapper::toDtoResponse)
                .collect(Collectors.toList());
        return ItemMapper.toItemWithBookingInfoDto(item, comments);
    }

    @Override
    public List<ItemDto> findAllByUser(Long userId) {
        return itemRepository.findAll().stream()
                .filter(item -> Objects.equals(item.getOwner().getId(), userId))
                .toList().stream().map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text, Integer from, Integer size) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.searchAvailable(text, PageRequest.of(from / size, size)).stream()
                .filter(item -> item.getAvailable().equals(true))
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .toList().stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

    }

    private void checkOwner(Item item, Long ownerId) {
        if (!Objects.equals(item.getOwner().getId(), ownerId)) {
            throw new NotFoundException("The user not found");
        }
    }

    @Transactional
    @Override
    public Comment createComment(Comment comment, Long itemId, Long userId) {
        LocalDateTime nowDateTime = LocalDateTime.now();
        Booking booking = bookingRepository.findFirstByItemIdAndBookerIdAndEndIsBefore(itemId, userId, nowDateTime).orElseThrow(() -> new IllegalArgumentException("Bad request"));
        comment.setAuthor(booking.getBooker());
        comment.setItem(booking.getItem());
        comment.setCreated(nowDateTime);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> findAllByRequestId(Long requestId) {
        List<Item> allItems = itemRepository.findAllByRequestId(requestId);
        return allItems.stream()
                .map(ItemMapper::toItemDto)
                .collect(toList());
    }
}
