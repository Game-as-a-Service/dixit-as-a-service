package tw.wally.dixit.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.wally.dixit.services.TokenService;
import tw.wally.dixit.usecases.CreateDixitUseCase;
import tw.wally.dixit.usecases.GuessStoryUseCase;
import tw.wally.dixit.usecases.PlayCardUseCase;
import tw.wally.dixit.usecases.TellStoryUseCase;

/**
 * @author - wally55077@gmail.com
 */
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/dixit")
public class DixitController {
    private final CreateDixitUseCase createDixitUseCase;
    private final TellStoryUseCase tellStoryUseCase;
    private final PlayCardUseCase playCardUseCase;
    private final GuessStoryUseCase guessStoryUseCase;
    private final TokenService tokenService;

    @PostMapping
    public void createDixit(@RequestBody CreateDixitUseCase.Request request) {
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
    public void playcard(@PathVariable String dixitId,
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

}
