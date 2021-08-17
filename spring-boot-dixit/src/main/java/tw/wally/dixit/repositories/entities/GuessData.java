package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tw.wally.dixit.model.Guess;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class GuessData {
    private final PlayerData guesser;
    private final PlayCardData playCard;

    public static GuessData toData(Guess guess) {
        return new GuessData(PlayerData.toData(guess.getGuesser()), PlayCardData.toData(guess.getPlayCard()));
    }

    public Guess toEntity() {
        return new Guess(guesser.toEntity(), playCard.toEntity());
    }
}
