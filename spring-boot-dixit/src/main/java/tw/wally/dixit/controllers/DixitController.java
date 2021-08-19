package tw.wally.dixit.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.wally.dixit.services.TokenService;
import tw.wally.dixit.usecases.CreateDixitUseCase;
import tw.wally.dixit.usecases.GuessStoryUseCase;
import tw.wally.dixit.usecases.PlayCardUseCase;
import tw.wally.dixit.usecases.TellStoryUseCase;

import java.util.function.Consumer;

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

    @PutMapping("/{dixitId}/rounds/{round}/story")
    public void tellStory(@PathVariable String dixitId,
                          @PathVariable int round,
                          @RequestBody TellStoryUseCase.Request request) {
        request.gameId = dixitId;
        request.round = round;
        tellStoryUseCase.execute(request);
    }

    @PutMapping("/{dixitId}/rounds/{round}/playcard")
    public void playcard(@PathVariable String dixitId,
                         @PathVariable int round,
                         @RequestBody PlayCardUseCase.Request request) {
        request.gameId = dixitId;
        request.round = round;
        playCardUseCase.execute(request);
    }

    @PutMapping("/{dixitId}/rounds/{round}/guess")
    public void guessStory(@PathVariable String dixitId,
                           @PathVariable int round,
                           @RequestBody GuessStoryUseCase.Request request) {
        request.gameId = dixitId;
        request.round = round;
        guessStoryUseCase.execute(request);
    }
}
