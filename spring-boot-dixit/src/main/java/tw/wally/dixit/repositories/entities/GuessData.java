package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tw.wally.dixit.model.Card;
import tw.wally.dixit.model.Guess;
import tw.wally.dixit.model.Player;

import java.util.Map;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class GuessData {
    private final String guesserId;
    private final PlayCardData playCard;

    public static GuessData toData(Guess guess) {
        return new GuessData(guess.getGuesserId(), PlayCardData.toData(guess.getPlayCard()));
    }

    public Guess toEntity(Map<String, Player> players, Map<Integer, Card> cards) {
        return new Guess(players.get(guesserId), playCard.toEntity(players, cards));
    }
}
