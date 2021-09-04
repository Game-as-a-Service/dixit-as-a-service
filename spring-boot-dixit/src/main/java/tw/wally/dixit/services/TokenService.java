package tw.wally.dixit.services;

import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author - wally55077@gmail.com
 */
public interface TokenService {

    String createToken(Token token);

    Token parseAndValidateToken(String token);

    default void parseAndValidateTokenThenDo(String token, Consumer<Token> tokenConsumer) {
        tokenConsumer.accept(parseAndValidateToken(token));
    }

    @AllArgsConstructor
    class Token {
        protected static final String KEY_PLAYER_ID = "playerId";
        protected static final String KEY_PLAYER_NAME = "playerName";
        protected static final String KEY_CARD_ID = "cardId";
        protected final String playerId;
        protected final String playerName;
        protected final int cardId;

        public Map<String, Object> getClaimMap() {
            var claims = new LinkedHashMap<String, Object>();
            claims.put(KEY_PLAYER_ID, playerId);
            claims.put(KEY_PLAYER_NAME, playerName);
            claims.put(KEY_CARD_ID, cardId);
            return claims;
        }

    }

}
