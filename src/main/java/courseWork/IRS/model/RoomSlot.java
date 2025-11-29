package courseWork.IRS.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "room_slots")
@Data
@IdClass(RoomSlotId.class)
public class RoomSlot {

    @Id
    @Column(name = "room_id")
    private Integer roomId;

    @Id
    @Column(name = "slot_number")
    private Integer slotNumber;

    @Column(nullable = false)
    private Integer capacity;

    @ManyToOne
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private Room room;
}