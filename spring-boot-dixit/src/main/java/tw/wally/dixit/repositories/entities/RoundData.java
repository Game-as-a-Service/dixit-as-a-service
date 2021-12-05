package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.model.Round;
import tw.wally.dixit.model.RoundState;

import java.util.Collection;
import java.util.Map;

import static tw.wally.dixit.utils.StreamUtils.*;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@Builder
@AllArgsConstructor
public class RoundData {
    private RoundState roundState;
    private PlayerData storyteller;
    private Collection<PlayerData> guessers;
    private StoryData story;
    private final Collection<PlayCardData> playCards;
    private final Collection<GuessData> guesses;

    public void setStoryteller(PlayerData storyteller) {
        this.storyteller = storyteller;
    }

    public void setGuessers(Collection<PlayerData> guessers) {
        this.guessers = guessers;
    }

    public static RoundData toData(Round round) {
        var roundData = RoundData.builder()
                .playCards(mapToList(round.getPlayCards(), PlayCardData::toData))
                .guesses(mapToList(round.getGuesses(), GuessData::toData))
                .roundState(round.getRoundState());
        var story = round.getStory();
        if (story != null) {
            roundData.story(StoryData.toData(story));
        }
        return roundData.build();
    }

    public Round toEntity(Map<String, Player> players) {
        Player storyteller = players.get(this.storyteller.getId());
        var round = Round.builder()
                .storyteller(storyteller)
                .guessers(filterToList(players.values(), player -> !player.getId().equals(storyteller.getId())))
                .playCards(toMap(playCards, PlayCardData::getCardId, playCard -> playCard.toEntity(players.get(playCard.getPlayerId()))))
                .guesses(toMap(guesses, guess -> players.get(guess.getGuesserId()), guess -> guess.toEntity(players.get(guess.getGuesserId()), players.get(guess.getPlaycardPlayerId()))))
                .numberOfGuessers(guessers.size())
                .roundState(roundState);
        if (story != null) {
            round.story(story.toEntity(players.get(story.getStorytellerId())));
        }
        return round.build();
    }

}
