package tw.wally.dixit;

import tw.wally.dixit.services.TokenService;

/**
 * @author - wally55077@gmail.com
 */
public class FakeTokenService implements TokenService {

    private static final Token FAKE_TOKEN = new Token("fakePlayerId", "fakePlayerName", -1);

    @Override
    public String createToken(Token token) {
        return FAKE_TOKEN.toString();
    }

    @Override
    public Token parseAndValidateToken(String token) {
        return FAKE_TOKEN;
    }
}
