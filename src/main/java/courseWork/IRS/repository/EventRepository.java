package courseWork.IRS.repository;

import courseWork.IRS.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByTitleContainingIgnoreCase(String title);
    List<Event> findByPriceBetween(BigDecimal min, BigDecimal max);
    List<Event> findByStartTimeBetween(ZonedDateTime from, ZonedDateTime to);
}