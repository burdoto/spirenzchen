package de.kaleidox.kram.cards.dcb.entity;

import de.kaleidox.kram.cards.Player;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

@Entity
public class GamePlayer extends Player {
    @Id
    public long userId;

    public GamePlayer() {
        super(0);
    }

    public GamePlayer(long userId) {
        super(0);
        this.userId = userId;
    }

    public User asUser(JDA jda) {
        return jda.getUserById(userId);
    }
}
