package courseWork.IRS.repository;

import courseWork.IRS.model.EventBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventBookingRepository extends JpaRepository<EventBooking, Integer> {
    List<EventBooking> findByUserId(Integer userId);
    List<EventBooking> findByEventId(Integer eventId);
}