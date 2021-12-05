package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tw.wally.dixit.model.Guess;
import tw.wally.dixit.model.Player;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@AllArgsConstructor
public class GuessData {
    private final PlayerData guesser;
    private final PlayCardData playCard;

    public String getGuesserId() {
        return guesser.getId();
    }

    public String getPlaycardPlayerId() {
        return playCard.getPlayerId();
    }

    public static GuessData toData(Guess guess) {
        return new GuessData(PlayerData.toData(guess.getGuesser()), PlayCardData.toData(guess.getPlayCard()));
    }

    public Guess toEntity(Player guesser, Player playcardPlayer) {
        return new Guess(guesser, playCard.toEntity(playcardPlayer));
    }
}
