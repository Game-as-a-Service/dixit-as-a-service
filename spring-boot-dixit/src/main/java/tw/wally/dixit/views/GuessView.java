package tw.wally.dixit.views;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.wally.dixit.model.Guess;

/**
 * @author - wally55077@gmail.com
 */
@NoArgsConstructor
@AllArgsConstructor
public class GuessView {
    public PlayerView guesser;
    public PlayCardView playCard;

    public static GuessView toViewModel(Guess guess) {
        var guesser = PlayerView.toViewModel(guess.getGuesser());
        var playCard = PlayCardView.toViewModel(guess.getPlayCard());
        return new GuessView(guesser, playCard);
    }

    public Guess toEntity() {
        return new Guess(guesser.toEntity(), playCard.toEntity());
    }
}
