package de.kaleidox.kram.cards.dcb.repo;

import de.kaleidox.kram.cards.dcb.entity.GamePlayer;
import jakarta.persistence.Table;
import org.springframework.data.repository.CrudRepository;

@Table(name = "players")
public interface PlayerRepository extends CrudRepository<GamePlayer, Long> {
}