package de.kaleidox.kram.cards.dcb.entity;

import de.kaleidox.kram.cards.CardGame;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Room {
    @Id
    public String code;
    @Column
    public boolean open;
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL)
    public List<GamePlayer> players;
    @Transient
    public CardGame game;
}
