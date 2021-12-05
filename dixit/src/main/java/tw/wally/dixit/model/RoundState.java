package tw.wally.dixit.model;

/**
 * @author - wally55077@gmail.com
 */
public enum RoundState {
    STORY_TELLING, CARD_PLAYING, PLAYER_GUESSING, SCORING;

    @Override
    public String toString() {
        return super.toString().replace("_", "-").toLowerCase();
    }
}
