package tw.wally.dixit.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.wally.dixit.usecases.CreateDixitUseCase;
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

    @PostMapping
    public void createDixit(@RequestBody CreateDixitUseCase.Request request) {
        createDixitUseCase.execute(request);
    }

    @PostMapping("/{dixitId}/rounds/{rounds}/story")
    public void tellStory(@PathVariable String dixitId,
                          @RequestBody TellStoryUseCase.Request request) {
        request.gameId = dixitId;
        tellStoryUseCase.execute(request);
    }


}
