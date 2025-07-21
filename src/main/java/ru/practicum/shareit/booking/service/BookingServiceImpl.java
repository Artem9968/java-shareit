package ru.practicum.shareit.booking.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public Booking create(BookingDto bookingDto, Long userId) {
        User booker = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Booking with " + userId + " Id is not found"));
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(() -> new NotFoundException("Booking with " + userId + " Id is not found"));
        if (Objects.equals(booker.getId(), item.getOwner().getId())) {
            throw new NotFoundException("Booker is equals owner");
        }
        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }
        bookingDto.setStatus(Status.WAITING);
        bookingDto.setItem(ItemMapper.toItemDto(item));
        bookingDto.setBooker(UserMapper.toUserDto(booker));

        Booking booking = BookingMapper.toBooking(bookingDto);
        bookingDateCheck(booking);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking approveBooking(Long bookingId, Long ownerId, Boolean isApproved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Not found booking"));
        if (!Objects.equals(booking.getItem().getOwner().getId(), ownerId)) {
            throw new ValidationException("id владельцев не одинаковые");
        }
        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new ValidationException("Статус бронирования " + bookingId + " отличен от " + Status.WAITING);
        }
        Status status;
        if (isApproved) {
            status = Status.APPROVED;
        } else {
            status = Status.REJECTED;
        }
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Not found booking"));
        if (!Objects.equals(booking.getBooker().getId(), userId) && !Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new ValidationException("id владельцев не одинаковые");
        }
        return booking;
    }

    @Override
    public List<Booking> getAllBookingsOfUserByState(Long bookerId, State state, Integer from, Integer size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Размер должен быть больше нуля!");
        }
        List<Booking> allBookings;
        LocalDateTime currentLocalDateTime = LocalDateTime.now();
        Pageable pageable = PageRequest.of(from / size, size);
        switch (state) {
            case CURRENT:
                allBookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        bookerId, currentLocalDateTime, currentLocalDateTime, pageable);
                break;
            case FUTURE:
                allBookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(
                        bookerId, currentLocalDateTime, pageable);
                break;
            case PAST:
                allBookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndBeforeOrderByStartDesc(
                        bookerId, currentLocalDateTime, currentLocalDateTime, pageable);
                break;
            case WAITING:
                allBookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        bookerId, Status.WAITING, pageable);
                break;
            case REJECTED:
                allBookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        bookerId, Status.REJECTED, pageable);
                break;
            default:
                allBookings = bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId, pageable);
        }
        if (allBookings.isEmpty()) {
            throw new NotFoundException("Not found booking");
        }
        return allBookings;
    }

    @Override
    public List<Booking> getAllBookingsOfUserItems(Long ownerId, State state, Integer from, Integer size) {
        List<Booking> allBookings;
        LocalDateTime currentLocalDateTime = LocalDateTime.now();
        List<Long> itemIds = itemRepository.findByOwnerId(ownerId, Pageable.unpaged())
                .stream().map(Item::getId).collect(Collectors.toList());
        if (itemIds.isEmpty()) {
            throw new NotFoundException("Not found");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        switch (state) {
            case CURRENT:
                allBookings = bookingRepository.findAllByItemIdInAndStartBeforeAndEndAfterOrderByStartDesc(
                        itemIds, currentLocalDateTime, currentLocalDateTime, pageable);
                break;
            case FUTURE:
                allBookings = bookingRepository.findAllByItemIdInAndStartAfterOrderByStartDesc(
                        itemIds, currentLocalDateTime, pageable);
                break;
            case PAST:
                allBookings = bookingRepository.findAllByItemIdInAndStartBeforeAndEndBeforeOrderByStartDesc(
                        itemIds, currentLocalDateTime, currentLocalDateTime, pageable);
                break;
            case WAITING:
                allBookings = bookingRepository.findAllByItemIdInAndStatusOrderByStartDesc(
                        itemIds, Status.WAITING, pageable);
                break;
            case REJECTED:
                allBookings = bookingRepository.findAllByItemIdInAndStatusOrderByStartDesc(
                        itemIds, Status.REJECTED, pageable);
                break;
            default:
                allBookings = bookingRepository.findAllByItemIdInOrderByStartDesc(itemIds, pageable);
        }
        return allBookings;
    }

    private void bookingDateCheck(Booking booking) {
        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new ValidationException("Start date is after end date");
        }
        if (booking.getStart().isEqual(booking.getEnd())) {
            throw new ValidationException("Start date is equal end date");
        }
        if (booking.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Can not start in the past");
        }
        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Can not end in the past");
        }
    }
}
