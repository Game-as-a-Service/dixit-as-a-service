package tw.wally.dixit.model;

/**
 * @author - wally55077@gmail.com
 */
public enum GameState {
    PREPARING, STARTED, OVER;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
