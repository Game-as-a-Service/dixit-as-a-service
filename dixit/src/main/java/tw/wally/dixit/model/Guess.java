package tw.wally.dixit.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.exceptions.InvalidGameOperationException;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class Guess {
    private Player guesser;
    private PlayCard playCard;

    public Guess(Player guesser, PlayCard playCard) {
        validateGuess(guesser, playCard);
        this.guesser = guesser;
        this.playCard = playCard;
    }

    private void validateGuess(Player guesser, PlayCard playCard) {
        if (guesser.equals(playCard.getPlayer())) {
            throw new InvalidGameOperationException("Guesser can't guess self card");
        }
    }

    public Player getPlayerWhoPlayedCard() {
        return playCard.getPlayer();
    }

    public Card getCard() {
        return playCard.getCard();
    }
}
