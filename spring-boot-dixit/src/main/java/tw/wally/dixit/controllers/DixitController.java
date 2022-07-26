package tw.wally.dixit.controllers;

import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.wally.dixit.model.*;
import tw.wally.dixit.usecases.*;
import tw.wally.dixit.views.*;

import java.util.Collection;

import static tw.wally.dixit.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@CrossOrigin
@RestController
@Api(tags = "Dixit")
@RequiredArgsConstructor
@RequestMapping("/api/dixit")
@Tag(name = "Dixit", description = "說書人遊戲操作")
public class DixitController {
    private final CreateDixitUseCase createDixitUseCase;
    private final TellStoryUseCase tellStoryUseCase;
    private final PlayCardUseCase playCardUseCase;
    private final GuessStoryUseCase guessStoryUseCase;
    private final GetDixitOverviewUseCase getDixitOverviewUseCase;

    @PostMapping("/{dixitId}")
    @ApiOperation(value = "創建 Dixit 遊戲", notes = "創建一局 2 ~ 4 人的 Dixit 遊戲", nickname = "CreateDixit")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "DixitId", value = "Dixit 代號", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "CreateDixitRequest", value = "Dixit 創建資訊", dataTypeClass = CreateDixitRequest.class, required = true)
    })
    public void createDixit(@PathVariable String dixitId,
                            @RequestBody CreateDixitRequest request) {
        request.gameId = dixitId;
        createDixitUseCase.execute(request.toRequest());
    }

    @PutMapping("/{dixitId}/rounds/{round}/players/{playerId}/story")
    @Operation(summary = "說書人說故事", description = "說書人以謎語描述一張打出的故事卡牌")
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
    @Operation(summary = "猜謎者出牌", description = "猜謎者出一張卡牌，混肴真正故事卡牌")
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
    @Operation(summary = "猜謎者猜故事", description = "猜謎者投票選擇真正故事卡牌")
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
    @Operation(summary = "取得 Dixit 遊戲內容", description = "取得當前階段 Dixit 的遊戲內容")
    public DixitOverview getDixitOverview(@PathVariable String dixitId,
                                          @PathVariable String playerId) {
        var presenter = new DixitOverviewPresenter();
        getDixitOverviewUseCase.execute(new AbstractDixitUseCase.Request(dixitId, playerId), presenter);
        return presenter.present();
    }
}

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "Dixit 創建資訊", description = "創建一局 Dixit")
class CreateDixitRequest {

    @ApiModelProperty(value = "房間代號", required = true)
    public String roomId;
    @ApiModelProperty(value = "遊戲代號", required = true)
    public String gameId;
    @ApiModelProperty(value = "房主代號", required = true)
    public String hostId;

    @ApiModelProperty(allowableValues = "(4, 6)", required = true)
    public Collection<PlayerRequest> players;

    @ApiModelProperty(allowableValues = "(2, 2)", required = true)
    public Collection<OptionRequest> options;

    @Getter
    @ApiModel(value = "玩家資訊", description = "創建一位玩家")
    private static final class PlayerRequest {

        @ApiModelProperty(value = "遊戲玩家代號", required = true)
        public String id;

        @ApiModelProperty(value = "遊戲玩家名稱", required = true)
        public String name;
    }

    @Getter
    @ApiModel(value = "遊戲設定選項", description = "設定遊戲")
    private static final class OptionRequest {

        @ApiModelProperty(value = "遊戲設定名稱", allowableValues = "winningScore, numberOfPlayers", required = true)
        public String name;

        @ApiModelProperty(value = "遊戲設定內容", required = true)
        public int value;
    }

    public CreateDixitUseCase.Request toRequest() {
        var players = mapToList(this.players, player -> new Player(player.id, player.name));
        var options = mapToList(this.options, this::toOption);
        return new CreateDixitUseCase.Request(roomId, gameId, hostId, players, options);
    }

    private CreateDixitUseCase.Option toOption(OptionRequest option) {
        return new CreateDixitUseCase.Option(option.name, option.value);
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
