package courseWork.IRS.repository;

import courseWork.IRS.model.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public interface AttractionRepository extends JpaRepository<Attraction, Integer> {
    List<Attraction> findByNameContainingIgnoreCase(String name);
    List<Attraction> findByPriceBetween(BigDecimal min, BigDecimal max);
    List<Attraction> findByStartTimeGreaterThanEqualAndEndTimeLessThanEqual(LocalTime start, LocalTime end);
}