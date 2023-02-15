package de.kaleidox.kram.cards.dcb.repo;

import de.kaleidox.kram.cards.dcb.entity.Room;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Optional;

@Table(name = "rooms")
public interface RoomRepository extends CrudRepository<Room, String> {
    Optional<Room> findByPlayersUserId(long userId);

    @Query("select r from Room r where r.open")
    Collection<Room> findOpenRooms();
}
