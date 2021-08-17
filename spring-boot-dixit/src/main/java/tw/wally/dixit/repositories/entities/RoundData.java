package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tw.wally.dixit.model.PlayCard;
import tw.wally.dixit.model.Round;
import tw.wally.dixit.model.RoundState;

import java.util.List;
import java.util.Map;

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
    private final List<PlayerData> guessers;
    private final Map<Integer, PlayCardData> playCards;
    private final Map<PlayerData, GuessData> guesses;
    private final int numberOfGuessers;
    private RoundState roundState;
    private StoryData story;

    public static RoundData toData(Round round) {
        var roundData = RoundData.builder()
                .storyteller(PlayerData.toData(round.getStoryteller()))
                .guessers(mapToList(round.getGuessers(), PlayerData::toData))
                .playCards(toMap(round.getPlayCards().values(), PlayCard::getCardId, PlayCardData::toData))
                .guesses(toMap(round.getGuesses().values(), guess -> PlayerData.toData(guess.getGuesser()), GuessData::toData))
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
                .playCards(toMap(playCards.values(), PlayCardData::getCardId, PlayCardData::toEntity))
                .guesses(toMap(guesses.values(), guess -> guess.getGuesser().toEntity(), GuessData::toEntity))
                .numberOfGuessers(numberOfGuessers)
                .roundState(roundState);
        if (story != null) {
            round.story(story.toEntity());
        }
        return round.build();
    }

}
