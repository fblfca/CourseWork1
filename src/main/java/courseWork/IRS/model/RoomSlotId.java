package courseWork.IRS.model;

import java.io.Serializable;
import java.util.Objects;

public class RoomSlotId implements Serializable {

    private Integer roomId;
    private Integer slotNumber;

    // Конструкторы
    public RoomSlotId() {}

    public RoomSlotId(Integer roomId, Integer slotNumber) {
        this.roomId = roomId;
        this.slotNumber = slotNumber;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomSlotId that = (RoomSlotId) o;
        return Objects.equals(roomId, that.roomId) &&
                Objects.equals(slotNumber, that.slotNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, slotNumber);
    }
}