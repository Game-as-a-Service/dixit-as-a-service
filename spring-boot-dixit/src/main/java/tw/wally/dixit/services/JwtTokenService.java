package tw.wally.dixit.services;

import lombok.AllArgsConstructor;

import java.util.List;
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

    private static final List<Entry<String, Object>> HEADERS = of(entry("alg", "HS256"), entry("typ", "JWT"));
    private final String secret;

    @Override
    public String createToken(Token token) {
        var key = hmacShaKeyFor(secret.getBytes(UTF_8));
        var jwt = builder();
        HEADERS.forEach(entry -> jwt.setHeaderParam(entry.getKey(), entry.getValue()));
//        claims.forEach(entry -> jwt.claim(entry.getKey(), entry.getValue()));
        return jwt.signWith(key).compact();
    }

    public static void main(String[] args) {
        var jwtIoToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkaXhpdElkIjoiZGl4aXRJZCIsInBocmFzZSI6InBocmFzZSIsInBsYXllcklkIjoiSWQ6MSIsImNhcmRJZCI6MX0.ZzjqS2n2jMdgEg1Yzp885lzyeKcy_s_QITXxUaou3_g";
        String secret = "GameAsAServiceDixitNoSecretAndItCreatedByWally";
        var tokenService = new JwtTokenService(secret);
        List<Entry<String, Object>> claims = of(entry("dixitId", "dixitId"),
                entry("phrase", "phrase"), entry("playerId", "Id:1"), entry("cardId", 1));
        var token = tokenService.createToken(new Token());
        System.out.println(jwtIoToken.equals(token));
    }


}
