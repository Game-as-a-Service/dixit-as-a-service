package tw.wally.dixit.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.wally.dixit.model.*;
import tw.wally.dixit.services.TokenService;
import tw.wally.dixit.usecases.*;
import tw.wally.dixit.views.*;

import java.util.Collection;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/dixit")
public class DixitController {
    private final TokenService tokenService;
    private final CreateDixitUseCase createDixitUseCase;
    private final TellStoryUseCase tellStoryUseCase;
    private final PlayCardUseCase playCardUseCase;
    private final GuessStoryUseCase guessStoryUseCase;
    private final GetDixitOverviewUseCase getDixitOverviewUseCase;

    @PostMapping("/{dixitId}")
    public void createDixit(@PathVariable String dixitId,
                            @RequestBody CreateDixitUseCase.Request request) {
        request.gameId = dixitId;
        createDixitUseCase.execute(request);
    }

    @PutMapping("/{dixitId}/rounds/{round}/players/{playerId}/story")
    public void tellStory(@PathVariable String dixitId,
                          @PathVariable int round,
                          @PathVariable String playerId,
                          @RequestBody TellStoryUseCase.Request request) {
        request.gameId = dixitId;
        request.round = round;
        request.playerId = playerId;
        tellStoryUseCase.execute(request);
    }

    @PutMapping("/{dixitId}/rounds/{round}/players/{playerId}/playcard")
    public void playCard(@PathVariable String dixitId,
                         @PathVariable int round,
                         @PathVariable String playerId,
                         @RequestBody PlayCardUseCase.Request request) {
        request.gameId = dixitId;
        request.round = round;
        request.playerId = playerId;
        playCardUseCase.execute(request);
    }

    @PutMapping("/{dixitId}/rounds/{round}/players/{playerId}/guess")
    public void guessStory(@PathVariable String dixitId,
                           @PathVariable int round,
                           @PathVariable String playerId,
                           @RequestBody GuessStoryUseCase.Request request) {
        request.gameId = dixitId;
        request.round = round;
        request.playerId = playerId;
        guessStoryUseCase.execute(request);
    }

    @GetMapping("/{dixitId}/players/{playerId}/overview")
    public DixitOverview getDixitOverview(@PathVariable String dixitId,
                                          @PathVariable String playerId) {
        var presenter = new DixitOverviewPresenter();
        getDixitOverviewUseCase.execute(new AbstractDixitUseCase.Request(dixitId, playerId), presenter);
        return presenter.present();
    }
}

class DixitOverviewPresenter implements GetDixitOverviewUseCase.Presenter {

    private final DixitOverview.DixitOverviewBuilder dixitOverviewBuilder = DixitOverview.builder();

    @Override
    public void showGameState(GameState gameState) {
        dixitOverviewBuilder.gameState(gameState);
    }

    @Override
    public void showRoundState(RoundState roundState) {
        dixitOverviewBuilder.roundState(roundState);
    }

    @Override
    public void showRounds(int rounds) {
        dixitOverviewBuilder.rounds(rounds);
    }

    @Override
    public void showPlayers(Collection<Player> players) {
        dixitOverviewBuilder.players(mapToList(players, PlayerView::toViewModel));
    }

    @Override
    public void showStoryteller(Player storyteller) {
        dixitOverviewBuilder.storyteller(PlayerView.toViewModel(storyteller));
    }

    @Override
    public void showHandCards(Collection<Card> handCards) {
        dixitOverviewBuilder.handCards(mapToList(handCards, CardView::toViewModel));
    }

    @Override
    public void showStory(Story story) {
        dixitOverviewBuilder.story(StoryView.toViewModel(story));
    }

    @Override
    public void showPlayCards(Collection<PlayCard> playCards) {
        dixitOverviewBuilder.playCards(mapToList(playCards, PlayCardView::toViewModel));
    }

    @Override
    public void showGuesses(Collection<Guess> guesses) {
        dixitOverviewBuilder.guesses(mapToList(guesses, GuessView::toViewModel));
    }

    @Override
    public void showWinners(Collection<Player> winners) {
        dixitOverviewBuilder.winners(mapToList(winners, PlayerView::toViewModel));
    }

    public DixitOverview present() {
        return dixitOverviewBuilder.build();
    }
}
