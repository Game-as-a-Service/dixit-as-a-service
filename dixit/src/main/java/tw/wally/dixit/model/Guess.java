package tw.wally.dixit.model;

import lombok.Getter;

/**
 * @author - wally55077@gmail.com
 */
@Getter
public class Guess {
    private final Player guesser;
    private final PlayCard playCard;

    public Guess(Player guesser, PlayCard playCard) {
        validateGuess(guesser, playCard);
        this.guesser = guesser;
        this.playCard = playCard;
    }

    private void validateGuess(Player guesser, PlayCard playCard) {
        if (guesser.equals(playCard.getPlayer())) {
            throw new IllegalArgumentException("Guesser can't guess self card");
        }
    }
}
