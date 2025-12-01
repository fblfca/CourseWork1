package courseWork.IRS.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime; // ИЗМЕНЕНО с ZonedDateTime

@Entity
@Table(name = "events")
@Data
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer location;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // ИЗМЕНЕНО

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime; // ИЗМЕНЕНО

    private Integer capacity;

    private BigDecimal price;

    private String description;
}