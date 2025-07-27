package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.MapperRequestItem;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemService itemService;

    @Override
    public List<ItemRequest> getAllByUserId(Long id) {
        userRepository.findById(id).orElseThrow(() -> new NotFoundException(""));
        return itemRequestRepository.findByRequestorIdOrderByCreatedDesc(id);
    }

    @Override
    @Transactional
    public ItemRequest add(ItemRequest itemRequest, Long requestorId) {
        User requestor = userRepository.findById(requestorId).orElseThrow(() -> new NotFoundException(""));
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequestRepository.save(itemRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemRequestDto getById(Long requestId, Long requestorId) {
        userRepository.findById(requestorId).orElseThrow(() -> new NotFoundException(""));
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException(""));
        ItemRequestDto itemRequestDto = MapperRequestItem.toItemRequestDto(itemRequest);
        itemRequestDto.setItems(itemService.findAllByRequestId(itemRequestDto.getId()));
        return itemRequestDto;
    }
}
