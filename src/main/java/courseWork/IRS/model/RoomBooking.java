package courseWork.IRS.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "rooms_bookings")
@Data
public class RoomBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "room_id", nullable = false)
    private Integer roomId;

    @Column(name = "slot_number", nullable = false)
    private Integer slotNumber;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "booking_weight", nullable = false)
    private Integer bookingWeight;

    private String status = "подтверждено";

    @CreationTimestamp
    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private Room room;

    // ДОБАВЛЕНА СВЯЗЬ ДЛЯ ПОЛУЧЕНИЯ ИМЕНИ КЛИЕНТА
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserInfo userInfo;
}