package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable
    );

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndBeforeOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable
    );

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, Status bookingStatus, Pageable pageable);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findAllByItemIdInAndStartBeforeAndEndAfterOrderByStartDesc(
            List<Long> itemIds, LocalDateTime start, LocalDateTime end, Pageable pageable
    );

    List<Booking> findAllByItemIdInAndStartAfterOrderByStartDesc(List<Long> itemIds, LocalDateTime start, Pageable pageable);

    List<Booking> findAllByItemIdInAndStartBeforeAndEndBeforeOrderByStartDesc(
            List<Long> itemIds, LocalDateTime start, LocalDateTime end, Pageable pageable
    );

    List<Booking> findAllByItemIdInAndStatusOrderByStartDesc(List<Long> itemIds, Status bookingStatus, Pageable pageable);

    List<Booking> findAllByItemIdInOrderByStartDesc(List<Long> itemIds, Pageable pageable);

    @Query("SELECT new ru.practicum.shareit.booking.model.Booking(" +
            "b.id, MAX(b.start), b.end, b.item, b.booker, b.status) " +
            "FROM Booking b " +
            "WHERE b.item.id IN (?1) " +
            "AND b.item.owner.id = ?2 " +
            "AND b.start < ?3 " +
            "AND b.status NOT IN (?4) " +
            "GROUP BY b.id " +
            "ORDER BY b.item.id, MAX(b.start) DESC"
    )
    List<Booking> findLastByItemIdsAndItemOwnerIdAndStartIsBeforeAndStatusNotIn(
            List<Long> itemIds, Long ownerId, LocalDateTime start, List<Status> statuses
    );

    @Query("SELECT new ru.practicum.shareit.booking.model.Booking(" +
            "b.id, MIN(b.start), b.end, b.item, b.booker, b.status) " +
            "FROM Booking b " +
            "WHERE b.item.id IN (?1) " +
            "AND b.item.owner.id = ?2 " +
            "AND b.start >= ?3 " +
            "AND b.status NOT IN (?4) " +
            "GROUP BY b.id " +
            "ORDER BY b.item.id, MIN(b.start)"
    )
    List<Booking> findNextByItemIdsAndItemOwnerIdAndStartIsAfterAndStatusNotIn(
            List<Long> itemIds, Long ownerId, LocalDateTime start, List<Status> statuses
    );

    Optional<Booking> findFirstByItemIdAndBookerIdAndEndIsBefore(Long itemId, Long bookerId, LocalDateTime end);

}
