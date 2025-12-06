package courseWork.IRS.repository;

import courseWork.IRS.model.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomBookingRepository extends JpaRepository<RoomBooking, Integer> {
    List<RoomBooking> findByUserId(Integer userId);
    List<RoomBooking> findByRoomId(Integer roomId);
    List<RoomBooking> findByRoomIdAndSlotNumber(Integer roomId, Integer slotNumber);

    /**
     * Поиск пересекающихся бронирований.
     * Логика пересечения: (StartA < EndB) и (EndA > StartB).
     * Исключаем брони со статусом 'отменено' или 'завершено'.
     */
    @Query("SELECT b FROM RoomBooking b WHERE b.roomId = :roomId " +
            "AND b.slotNumber = :slotNumber " +
            "AND b.status NOT IN ('отменено', 'завершено') " +
            "AND (b.startTime < :endTime AND b.endTime > :startTime)")
    List<RoomBooking> findOverlappingBookings(@Param("roomId") Integer roomId,
                                              @Param("slotNumber") Integer slotNumber,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);
}