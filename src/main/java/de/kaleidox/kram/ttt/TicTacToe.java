package de.kaleidox.kram.ttt;

import de.kaleidox.kram.chat.ChatConnection;
import de.kaleidox.kram.chat.ChatServer;
import org.comroid.api.BitmaskAttribute;
import org.comroid.api.UUIDContainer;
import org.comroid.uniform.SerializationAdapter;
import org.comroid.uniform.node.UniObjectNode;
import org.comroid.util.Bitmask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TicTacToe implements UUIDContainer {
    private static final Map<UUID, ChatConnection> waitingForStart = new ConcurrentHashMap<>();
    private static final Collection<TicTacToe> sessions = new HashSet<>();
    private static final String[] PLAYER = new String[]{null, "X", "0"};
    private final UUID session;
    private final ChatConnection player1;
    private final ChatConnection player2;
    private int[] board = new int[3*3];
    private int turn = 1;

    @Override
    public UUID getUUID() {
        return session;
    }

    public TicTacToe(UUID session, ChatConnection player1, ChatConnection player2) {
        this.session = session;
        this.player1 = player1;
        this.player2 = player2;
    }

    private void concludeGame(int winner) {
        UniObjectNode data = ChatServer.instance.requireFromContext(SerializationAdapter.class).createObjectNode();
        data.put("winner", winner);
        player1.sendCommand("ttt/conclude", data);
        player2.sendCommand("ttt/conclude", data);
    }

    private void publishBox(int boxIndex, int player) {
        UniObjectNode data = ChatServer.instance.requireFromContext(SerializationAdapter.class).createObjectNode();
        data.put("player", PLAYER[player]);
        data.put("index", boxIndex);
        board[boxIndex] = player;
        player1.sendCommand("ttt/receive", data);
        player2.sendCommand("ttt/receive", data);
    }

    public static TicTacToe connectGame(UUID session, ChatConnection connection) {
        if (waitingForStart.containsKey(session)) {
            // start game instance
            return new TicTacToe(session, waitingForStart.remove(session), connection);
        } else {
            // wait for other player
            waitingForStart.put(session, connection);
            return null;
        }
    }

    public static void callBox(ChatConnection connection, int index) {
        findSessionByConnection(connection).ifPresent(ttt -> ttt._callBox(connection, index));
    }

    private void _callBox(ChatConnection connection, int index) {
        int player = connection == player1 ? 1 : 2;
        if (player != turn)
            return;
        publishBox(index, player);
        checkWinConditions(player, index);
    }

    private enum Relation implements IntUnaryOperator, BitmaskAttribute<Relation> {
        UP(i -> i - 3) {
            @Override
            public Relation getOpposing() {
                return DOWN;
            }
        },
        DOWN(i -> i + 3) {
            @Override
            public Relation getOpposing() {
                return UP;
            }
        },
        LEFT(i -> i - 1) {
            @Override
            public Relation getOpposing() {
                return RIGHT;
            }
        },
        RIGHT(i -> i + 1) {
            @Override
            public Relation getOpposing() {
                return LEFT;
            }
        },
        UP_LEFT(UP, LEFT) {
            @Override
            public Relation getOpposing() {
                return DOWN_RIGHT;
            }
        },
        UP_RIGHT(UP, RIGHT) {
            @Override
            public Relation getOpposing() {
                return DOWN_LEFT;
            }
        },
        DOWN_LEFT(DOWN, LEFT) {
            @Override
            public Relation getOpposing() {
                return UP_RIGHT;
            }
        },
        DOWN_RIGHT(DOWN, RIGHT) {
            @Override
            public Relation getOpposing() {
                return UP_LEFT;
            }
        };

        private final IntUnaryOperator operator;
        private final int value;

        Relation(IntUnaryOperator operator) {
            this.operator = operator;
            this.value = Bitmask.nextFlag();
        }

        Relation(Relation... parents) {
            this.operator = collectOPs(parents);
            this.value = Bitmask.combine(parents);
        }

        private IntUnaryOperator collectOPs(final Relation[] ops) {
            return i -> {
                for (Relation op : ops)
                    i = op.applyAsInt(i);
                return i;
            };
        }

        @Override
        public @NotNull Integer getValue() {
            return value;
        }

        @Override
        public int applyAsInt(int operand) {
            return operator.applyAsInt(operand);
        }

        public Relation getOpposing() {
            throw new AbstractMethodError();
        }
    }

    private void checkWinConditions(int player, int boxIndex) {
        int x = boxIndex % 3;
        int y = boxIndex / 3;

        Map<Relation, Integer> lens = new HashMap<>();
        for (Relation relation : Relation.values())
            lens.put(relation, lineLen(player, boxIndex, relation, 0));
        for (Map.Entry<Relation, Integer> e : lens.entrySet()) {
            int t = e.getValue();
            if (t == 0)
                continue;
            Relation rel = e.getKey();
            t += lens.getOrDefault(rel, 0);
            if (t == 4) {
                concludeGame(player);
                return;
            }
        }

        turn = player == 1 ? 2 : 1;
    }

    private int lineLen(int player, int index, Relation relation, int iteration) {
        if (player != getNeighborValue(index, relation, iteration))
            return iteration;
        return lineLen(player, index, relation, iteration + 1);
    }

    private int getNeighborValue(int index, Relation relation, int iteration) {
        int newIndex = index;
        for (int i = 0; i < board.length && i > -1; i++)
            newIndex = relation.applyAsInt(newIndex);
        if (newIndex < 0 || newIndex > board.length)
            return -1;
        return board[newIndex];
    }

    private static Optional<TicTacToe> findSessionByConnection(ChatConnection connection) {
        return sessions.stream()
                .filter(it -> it.player1 == connection || it.player2 == connection)
                .findAny();
    }
}
