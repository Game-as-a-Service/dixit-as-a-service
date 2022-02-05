package tw.wally.dixit.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tw.wally.dixit.model.Player;
import tw.wally.dixit.model.Round;
import tw.wally.dixit.model.RoundState;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static tw.wally.dixit.utils.StreamUtils.mapToList;
import static tw.wally.dixit.utils.StreamUtils.toMap;

/**
 * @author - wally55077@gmail.com
 */
@Getter
@Builder
@AllArgsConstructor
public class RoundData {
    private RoundState roundState;
    private String storytellerId;
    private Collection<String> guesserIds;
    private StoryData story;
    private final Collection<PlayCardData> playCards;
    private final Collection<GuessData> guesses;

    public static RoundData toData(Round round) {
        return RoundData.builder()
                .roundState(round.getRoundState())
                .storytellerId(round.getStoryteller().getId())
                .guesserIds(mapToList(round.getGuessers(), Player::getId))
                .story(round.mayHaveStory().map(StoryData::toData).orElse(null))
                .playCards(mapToList(round.getPlayCards(), PlayCardData::toData))
                .guesses(mapToList(round.getGuesses(), GuessData::toData))
                .build();
    }

    public Round toEntity(Map<String, Player> players) {
        Player storyteller = players.get(storytellerId);
        return Round.builder()
                .roundState(roundState)
                .numberOfGuessers(guesserIds.size())
                .storyteller(storyteller)
                .guessers(mapToList(guesserIds, players::get))
                .story(mayHaveStory().map(story -> story.toEntity(storyteller)).orElse(null))
                .playCards(toMap(playCards, PlayCardData::getCardId, playCard -> playCard.toEntity(players.get(playCard.getPlayerId()))))
                .guesses(toMap(guesses, GuessData::getGuesserId, guess -> guess.toEntity(players.get(guess.getGuesserId()), players.get(guess.getPlaycardPlayerId()))))
                .build();
    }

    private Optional<StoryData> mayHaveStory() {
        return ofNullable(story);
    }
}
