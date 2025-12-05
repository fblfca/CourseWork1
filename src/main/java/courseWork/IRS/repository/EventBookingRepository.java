package courseWork.IRS.repository;

import courseWork.IRS.model.EventBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EventBookingRepository extends JpaRepository<EventBooking, Integer> {
    List<EventBooking> findByUserId(Integer userId);
    List<EventBooking> findByEventId(Integer eventId);

    /**
     * Ищет первое бронирование для пользователя и события, статус которого НЕ равен указанному.
     * @param userId ID пользователя.
     * @param eventId ID события.
     * @param statusToExclude Статус для исключения (например, "отменено").
     * @return Optional с бронированием, если найдено.
     */
    Optional<EventBooking> findFirstByUserIdAndEventIdAndStatusNot(Integer userId, Integer eventId, String statusToExclude);
}