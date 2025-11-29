package courseWork.IRS.repository;

import courseWork.IRS.model.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.ZonedDateTime;
import java.util.List;

public interface RoomBookingRepository extends JpaRepository<RoomBooking, Integer> {
    List<RoomBooking> findByUserId(Integer userId);
    List<RoomBooking> findByRoomIdAndSlotNumber(Integer roomId, Integer slotNumber);
    List<RoomBooking> findByStartTimeBetween(ZonedDateTime start, ZonedDateTime end);
}