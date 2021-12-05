package tw.wally.dixit.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.exceptions.InvalidGameOperationException;
import tw.wally.dixit.exceptions.NotFoundException;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.String.format;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.function.Function.identity;
import static tw.wally.dixit.model.Dixit.NUMBER_OF_DEALT_CARD;
import static tw.wally.dixit.model.Dixit.NUMBER_OF_PLAYER_HAND_CARDS;
import static tw.wally.dixit.model.Round.BONUS_SCORE;
import static tw.wally.dixit.model.Round.GUESS_CORRECTLY_SCORE;
import static tw.wally.dixit.utils.StreamUtils.toMap;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String id;
    private String name;
    private Color color;
    private Map<Integer, Card> handCards;
    private int score;

    public Player(String id, String name) {
        this(id, name, 0);
    }

    public Player(String id, String name, Collection<Card> handCards, int score) {
        this(id, name, toMap(requireNonNullElseGet(handCards, List::of), Card::getId, identity()), score);
    }

    public Player(String id, String name, int score) {
        this(id, name, new HashMap<>(NUMBER_OF_PLAYER_HAND_CARDS), score);
    }

    public Player(String id, String name, Map<Integer, Card> handCards, int score) {
        this.id = id;
        this.name = name;
        this.handCards = requireNonNullElseGet(handCards, HashMap::new);
        this.score = max(0, score);
    }

    public Player(String id, String name, Color color, int score) {
        this(id, name, color, new HashMap<>(NUMBER_OF_PLAYER_HAND_CARDS), score);
    }

    public Player(String id, String name, Color color, Collection<Card> handCards, int score) {
        this(id, name, color, toMap(requireNonNullElseGet(handCards, List::of), Card::getId, identity()), score);
    }

    public void addHandCard(Card card) {
        int numberOfHandCards = handCards.size();
        if (numberOfHandCards >= NUMBER_OF_PLAYER_HAND_CARDS) {
            throw new InvalidGameOperationException(format("Number of hand cards can not higher than %d.", NUMBER_OF_PLAYER_HAND_CARDS));
        }
        handCards.put(card.getId(), card);
    }

    public void addHandCards(Collection<Card> cards) {
        int numberOfCards = cards.size();
        if (numberOfCards != NUMBER_OF_DEALT_CARD
                && numberOfCards != NUMBER_OF_PLAYER_HAND_CARDS) {
            throw new InvalidGameOperationException(format("Number of cards should be %d or %d.", NUMBER_OF_DEALT_CARD, NUMBER_OF_PLAYER_HAND_CARDS));
        }
        cards.forEach(this::addHandCard);
    }

    public Card playCard(int cardId) {
        if (!handCards.containsKey(cardId)) {
            throw new NotFoundException(format("CardId: %d does not exist", cardId));
        }
        return handCards.remove(cardId);
    }

    public void addScore(int score) {
        if (score < BONUS_SCORE || score > GUESS_CORRECTLY_SCORE) {
            throw new InvalidGameOperationException(format("Score can't be lower than %d or higher than %d.", BONUS_SCORE, GUESS_CORRECTLY_SCORE));
        }
        this.score += score;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List<Card> getHandCards() {
        return new ArrayList<>(handCards.values());
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var player = (Player) o;
        return Objects.equals(id, player.id) && Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return hash(id, name);
    }
}
