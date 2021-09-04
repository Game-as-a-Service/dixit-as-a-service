package tw.wally.dixit.services;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import tw.wally.dixit.exceptions.InvalidTokenException;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Map.Entry;

import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.List.of;
import static java.util.Map.entry;

/**
 * @author - wally55077@gmail.com
 */
@AllArgsConstructor
public class JwtTokenService implements TokenService {

    private static final Collection<Entry<String, Object>> HEADERS = of(entry("alg", "HS256"), entry("typ", "JWT"));
    private final SecretKey key;

    public JwtTokenService(String secret) {
        this.key = hmacShaKeyFor(secret.getBytes(UTF_8));
    }

    @Override
    public String createToken(Token token) {
        var jwt = builder();
        HEADERS.forEach(entry -> jwt.setHeaderParam(entry.getKey(), entry.getValue()));
        token.getClaimMap().forEach(jwt::claim);
        return jwt.signWith(key).compact();
    }

    @Override
    public Token parseAndValidateToken(String token) {
        try {
            var claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            var playerId = claims.get(Token.KEY_PLAYER_ID, String.class);
            var playerName = claims.get(Token.KEY_PLAYER_NAME, String.class);
            var cardId = claims.get(Token.KEY_CARD_ID, Integer.class);
            return new Token(playerId, playerName, cardId);
        } catch (JwtException e) {
            throw new InvalidTokenException(e);
        }
    }

}
