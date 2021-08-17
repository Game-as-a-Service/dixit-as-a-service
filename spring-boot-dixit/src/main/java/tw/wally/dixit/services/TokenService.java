package tw.wally.dixit.services;

/**
 * @author - wally55077@gmail.com
 */
public interface TokenService {

    String createToken(Token token);


    class Token {
        public static final String KEY_PLAYER_ID = "playerId";
        public static final String KEY_CARD_ID = "cardId";
    }

}
