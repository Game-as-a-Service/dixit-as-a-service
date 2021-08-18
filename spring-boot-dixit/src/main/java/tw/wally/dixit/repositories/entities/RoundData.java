package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tw.wally.dixit.model.Round;
import tw.wally.dixit.model.RoundState;

import java.util.Collection;

import static tw.wally.dixit.utils.StreamUtils.mapToList;
import static tw.wally.dixit.utils.StreamUtils.toMap;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@Builder
@AllArgsConstructor
public class RoundData {
    private final PlayerData storyteller;
    private final Collection<PlayerData> guessers;
    private final Collection<PlayCardData> playCards;
    private final Collection<GuessData> guesses;
    private final int numberOfGuessers;
    private RoundState roundState;
    private StoryData story;

    public static RoundData toData(Round round) {
        var roundData = RoundData.builder()
                .storyteller(PlayerData.toData(round.getStoryteller()))
                .guessers(mapToList(round.getGuessers(), PlayerData::toData))
                .playCards(mapToList(round.getPlayCards().values(), PlayCardData::toData))
                .guesses(mapToList(round.getGuesses().values(), GuessData::toData))
                .numberOfGuessers(round.getNumberOfGuessers())
                .roundState(round.getRoundState());
        var story = round.getStory();
        if (story != null) {
            roundData.story(StoryData.toData(story));
        }
        return roundData.build();
    }

    public Round toEntity() {
        var round = Round.builder()
                .storyteller(storyteller.toEntity())
                .guessers(mapToList(guessers, PlayerData::toEntity))
                .playCards(toMap(playCards, PlayCardData::getCardId, PlayCardData::toEntity))
                .guesses(toMap(guesses, guess -> guess.getGuesser().toEntity(), GuessData::toEntity))
                .numberOfGuessers(numberOfGuessers)
                .roundState(roundState);
        if (story != null) {
            round.story(story.toEntity());
        }
        return round.build();
    }

}
