package tw.wally.dixit.model;

import lombok.Getter;
import tw.wally.dixit.exceptions.InvalidGameOperationException;

import static java.lang.String.format;

/**
 * @author - wally55077@gmail.com
 */
@Getter
public class Story {
    public static int MAX_LENGTH_OF_PHRASE = 20;
    private final String phrase;
    private final PlayCard playCard;

    public Story(String phrase, PlayCard playCard) {
        validatePhraseLength(phrase);
        this.phrase = phrase;
        this.playCard = playCard;
    }

    private void validatePhraseLength(String phrase) {
        if (phrase.length() > MAX_LENGTH_OF_PHRASE) {
            throw new InvalidGameOperationException(format("Phrase: %s length cannot exceed the limit %d", phrase, MAX_LENGTH_OF_PHRASE));
        }
    }

    public Player getPlayer() {
        return playCard.getPlayer();
    }

    public Card getCard() {
        return playCard.getCard();
    }

    public int getCardId() {
        return playCard.getCardId();
    }
}
