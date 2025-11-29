package courseWork.IRS.repository;

import courseWork.IRS.model.RoomSlot;
import courseWork.IRS.model.RoomSlotId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomSlotRepository extends JpaRepository<RoomSlot, RoomSlotId> {
}