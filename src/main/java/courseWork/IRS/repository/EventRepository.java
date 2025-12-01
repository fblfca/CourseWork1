package courseWork.IRS.repository;

import courseWork.IRS.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime; // ИЗМЕНЕНО
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    // ИЗМЕНЕНИЕ: Объединенный метод для фильтрации на уровне БД
    List<Event> findByTitleContainingIgnoreCaseAndPriceBetweenAndStartTimeBetween(
            String title,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDateTime fromTime,
            LocalDateTime toTime
    );

    // Обновлен тип времени
    List<Event> findByTitleContainingIgnoreCase(String title);
    List<Event> findByPriceBetween(BigDecimal min, BigDecimal max);
    List<Event> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);
}