package courseWork.IRS.repository;

import courseWork.IRS.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByNameContainingIgnoreCase(String name);
    List<Room> findByPricePerSlotHourBetween(BigDecimal min, BigDecimal max);
    List<Room> findByStatus(String status);
}