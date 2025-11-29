package courseWork.IRS.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "rooms")
@Data
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer location;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "price_per_slot_hour")
    private BigDecimal pricePerSlotHour;

    @Column(nullable = false)
    private String status = "открыто"; // открыто, техобслуживание, закрыто

    @Column(name = "slots_total", nullable = false)
    private Integer slotsTotal;

    private String description;
}