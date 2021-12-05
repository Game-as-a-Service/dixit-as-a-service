package tw.wally.dixit.views;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.wally.dixit.model.GameState;
import tw.wally.dixit.model.RoundState;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DixitOverview {

    @Builder.Default
    public GameState gameState = GameState.PREPARING;

    public RoundState roundState;
    public int rounds;

    @Builder.Default
    public Collection<PlayerView> players = new LinkedList<>();

    public PlayerView storyteller;

    @Builder.Default
    public Collection<CardView> handCards = new LinkedList<>();

    public StoryView story;

    @Builder.Default
    public Collection<PlayCardView> playCards = new LinkedList<>();

    @Builder.Default
    public Collection<GuessView> guesses = new LinkedList<>();

    @Builder.Default
    public Collection<PlayerView> winners = new LinkedList<>();
}
