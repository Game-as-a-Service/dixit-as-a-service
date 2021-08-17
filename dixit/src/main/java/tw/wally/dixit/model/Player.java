package tw.wally.dixit.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tw.wally.dixit.exceptions.InvalidGameOperationException;

import java.util.*;

import static java.lang.String.format;
import static java.util.Objects.hash;
import static tw.wally.dixit.model.Dixit.NUMBER_OF_DEALT_CARD;
import static tw.wally.dixit.model.Dixit.NUMBER_OF_PLAYER_HAND_CARDS;
import static tw.wally.dixit.model.Round.BONUS_SCORE;
import static tw.wally.dixit.model.Round.GUESS_CORRECTLY_SCORE;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class Player {
    private final String id;
    private final String name;
    private final Map<Integer, Card> handCards;
    private int score = 0;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.handCards = new HashMap<>(NUMBER_OF_PLAYER_HAND_CARDS);
    }

    public void addHandCards(Collection<Card> cards) {
        int numberOfCards = cards.size();
        if (numberOfCards != NUMBER_OF_DEALT_CARD
                && numberOfCards != NUMBER_OF_PLAYER_HAND_CARDS) {
            throw new InvalidGameOperationException(format("Number of cards should be %d or %d.", NUMBER_OF_DEALT_CARD, NUMBER_OF_PLAYER_HAND_CARDS));
        }
        cards.forEach(card -> handCards.put(card.getId(), card));
    }

    public Card playCard(int cardId) {
        if (!handCards.containsKey(cardId)) {
            throw new InvalidGameOperationException(format("CardId: %d does not exist", cardId));
        }
        return handCards.remove(cardId);
    }

    public void addScore(int score) {
        if (score < BONUS_SCORE || score > GUESS_CORRECTLY_SCORE) {
            throw new InvalidGameOperationException(format("Score can't be lower than %d or higher than %d.", BONUS_SCORE, GUESS_CORRECTLY_SCORE));
        }
        this.score += score;
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
