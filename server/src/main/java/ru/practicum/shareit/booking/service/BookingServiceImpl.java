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
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with " + userId + " Id is not found"));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item with " + bookingDto.getItemId() + " Id is not found"));

        if (Objects.equals(booker.getId(), item.getOwner().getId())) {
            throw new NotFoundException("Booker is equals owner");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
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
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Not found booking"));

        if (!Objects.equals(booking.getItem().getOwner().getId(), ownerId)) {
            throw new ValidationException("id владельцев не одинаковые");
        }
        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new ValidationException("Статус бронирования " + bookingId + " отличен от " + Status.WAITING);
        }

        booking.setStatus(isApproved ? Status.APPROVED : Status.REJECTED);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Not found booking"));

        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();

        if (!Objects.equals(bookerId, userId) && !Objects.equals(ownerId, userId)) {
            throw new ValidationException("id владельцев не одинаковые");
        }
        return booking;
    }

    @Override
    public List<Booking> getAllBookingsOfUserByState(Long bookerId, State state, Integer from, Integer size) {
        if (size <= 0) throw new IllegalArgumentException("Размер должен быть больше нуля!");

        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        bookerId, now, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(
                        bookerId, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndBeforeOrderByStartDesc(
                        bookerId, now, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        bookerId, Status.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        bookerId, Status.REJECTED, pageable);
                break;
            default:
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId, pageable);
        }
        if (bookings.isEmpty()) throw new NotFoundException("Not found booking");
        return bookings;
    }

    @Override
    public List<Booking> getAllBookingsOfUserItems(Long ownerId, State state, Integer from, Integer size) {
        List<Long> itemIds = itemRepository.findByOwnerId(ownerId, Pageable.unpaged())
                .stream().map(Item::getId).collect(Collectors.toList());
        if (itemIds.isEmpty()) throw new NotFoundException("Not found");

        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findAllByItemIdInAndStartBeforeAndEndAfterOrderByStartDesc(
                        itemIds, now, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByItemIdInAndStartAfterOrderByStartDesc(
                        itemIds, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findAllByItemIdInAndStartBeforeAndEndBeforeOrderByStartDesc(
                        itemIds, now, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItemIdInAndStatusOrderByStartDesc(
                        itemIds, Status.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItemIdInAndStatusOrderByStartDesc(
                        itemIds, Status.REJECTED, pageable);
                break;
            default:
                bookings = bookingRepository.findAllByItemIdInOrderByStartDesc(itemIds, pageable);
        }
        return bookings;
    }

    private void bookingDateCheck(Booking booking) {
        LocalDateTime start = booking.getStart();
        LocalDateTime end = booking.getEnd();
        LocalDateTime now = LocalDateTime.now();

        if (start.isAfter(end))
            throw new ValidationException("Start date is after end date");
        if (start.isEqual(end))
            throw new ValidationException("Start date is equal end date");
        if (start.isBefore(now))
            throw new ValidationException("Can not start in the past");
        if (end.isBefore(now))
            throw new ValidationException("Can not end in the past");
    }
}

