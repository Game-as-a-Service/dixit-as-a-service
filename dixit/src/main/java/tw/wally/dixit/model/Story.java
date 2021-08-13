package tw.wally.dixit.model;

import lombok.Getter;

import static java.lang.String.format;

/**
 * @author - wally55077@gmail.com
 */
@Getter
public class Story {
    public static int MAX_LENGTH_OF_PHRASE = 20;
    private final String phrase;
    private final Player player;
    private final Card card;

    public Story(String phrase, Player player, Card card) {
        validatePhraseLength(phrase);
        this.phrase = phrase;
        this.player = player;
        this.card = card;
    }

    private void validatePhraseLength(String phrase) {
        if (phrase.length() > MAX_LENGTH_OF_PHRASE) {
            throw new IllegalArgumentException(format("Phrase: %s length can be higher than %d", phrase, MAX_LENGTH_OF_PHRASE));
        }
    }

    public int getCardId() {
        return card.getId();
    }
}
